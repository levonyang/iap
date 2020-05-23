package com.haizhi.iap.mobile.util;

import com.haizhi.iap.mobile.conf.ArangoEdgeConf;
import com.haizhi.iap.mobile.conf.GraphEdgeLabel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thomas on 18/3/27.
 */
@Component
public class GraphEdgeLabelUtil
{
    @Autowired
    private DefaultLabelMaker defaultLabelMaker;

    @Autowired
    private InvestLabelMaker investLabelMaker;

    @Autowired
    private TradableShareLabelMaker tradableShareLabelMaker;

    @Autowired
    private ActualControllerLabelMaker actualControllerLabelMaker;

    @Autowired
    private ControlShareHolderLabelMaker controlShareHolderLabelMaker;

    private final Map<String, EdgeLabelMaker> EDGE_LABEL_MAKER_MAP = new HashMap<>();

    @PostConstruct
    public void init()
    {
        EDGE_LABEL_MAKER_MAP.put(ArangoEdgeConf.INVEST.getTableName(), investLabelMaker);
        EDGE_LABEL_MAKER_MAP.put(ArangoEdgeConf.TRADABLE_SHARE.getTableName(), tradableShareLabelMaker);
        EDGE_LABEL_MAKER_MAP.put(ArangoEdgeConf.ACTUAL_CONTROLLER.getTableName(), actualControllerLabelMaker);
        EDGE_LABEL_MAKER_MAP.put(ArangoEdgeConf.CONTROL_SHAREHOLDER.getTableName(), controlShareHolderLabelMaker);
    }

    public EdgeLabelMaker getEdgeLabelMaker(String relation)
    {
        return EDGE_LABEL_MAKER_MAP.getOrDefault(relation, defaultLabelMaker);
    }

    public void makeEdgeLabel(Collection<Map<String, Object>> edges, Map<String, Map<String, Object>> idVertexMap)
    {
        edges.stream().filter(edge -> !CollectionUtils.isEmpty(edge)).forEach(edge -> {
            String table = StringUtils.substringBefore(edge.get("_id").toString(), "/");
            String label = getEdgeLabelMaker(table).makeLabel(edge, idVertexMap);
            edge.putIfAbsent("label", label);
        });
    }

    /**
     * 计算任意一条边的投资占比
     *
     * @param edge 要计算投资占比的边
     * @param idVertexMap 所有顶点的ID与顶点自身组成的映射
     * @return 若边不是invest, tradable_share, control_shareholder等关系，则返回null
     */
    public Double getInvestPercentage(Map<String, Object> edge, Map<String, Map<String, Object>> idVertexMap)
    {
        String relation = StringUtils.substringBefore(edge.get("_id").toString(), "/");
        return getEdgeLabelMaker(relation).getInvestPercentage(edge, idVertexMap);
    }

    public static abstract class EdgeLabelMaker
    {
        public static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
        static {
            NUMBER_FORMAT.setMaximumFractionDigits(2);
            NUMBER_FORMAT.setRoundingMode(RoundingMode.UP);
        }

        public static String format(Double percentage)
        {
            if(percentage == null) return "";
            if(percentage > 1) return NUMBER_FORMAT.format(percentage);
            return new BigDecimal(percentage.toString()).toPlainString();
        }

        public static final Double ZERO = 0.;
        public static final Double ONE = 1.;
        public static final String ZERO_PERCENTAGE = "0%";
        public static final Double HUNDRED = 100.;

        private ArangoEdgeConf arangoEdgeConf;

        public EdgeLabelMaker(ArangoEdgeConf arangoEdgeConf)
        {
            this.arangoEdgeConf = arangoEdgeConf;
        }

        public String getEdgeCnName()
        {
            if(GraphEdgeLabel.EDGE_LABEL_CONF_MAP.containsKey(arangoEdgeConf.getTableName()))
                return GraphEdgeLabel.EDGE_LABEL_CONF_MAP.get(arangoEdgeConf.getTableName()).getTableCnName();
            return ArangoEdgeConf.INVEST.getTableName();
        }

        /**
         * 为指定边生成一个label，用于前端展示
         *
         * @param edge 要生成label的边
         * @param idVertexMap 顶点ID与顶点的映射
         * @return
         */
        public abstract String makeLabel(Map<String, Object> edge, Map<String, Map<String, Object>> idVertexMap);

        /**
         * 计算投资占比（对于invest, tradable_share, control_shareholder等关系才需要实现该逻辑）
         *
         * @param edge 要生成label的边
         * @param idVertexMap 顶点ID与顶点的映射
         * @return
         */
        public abstract Double getInvestPercentage(Map<String, Object> edge, Map<String, Map<String, Object>> idVertexMap);
    }

    @Component
    public static class InvestLabelMaker extends EdgeLabelMaker
    {
        public static final String INVEST_AMOUNT = "invest_amount";
        public static final String CAPITAL = "capital";

        public InvestLabelMaker()
        {
            super(ArangoEdgeConf.INVEST);
        }

        @Override
        public String makeLabel(Map<String, Object> edge, Map<String, Map<String, Object>> idVertexMap)
        {
            Double percentage = getInvestPercentage(edge, idVertexMap);
            return percentage == 0 ? getEdgeCnName() : String.format("%s%%出资占比", format(percentage));
        }

        @Override
        public Double getInvestPercentage(Map<String, Object> edge, Map<String, Map<String, Object>> idVertexMap)
        {
            String investAmountStr = edge.getOrDefault(INVEST_AMOUNT, ZERO).toString();
            double investAmount = StringUtils.isBlank(investAmountStr) ? ZERO : Double.parseDouble(investAmountStr);
            Map<String, Object> toVertex = idVertexMap.get(edge.get("_to").toString());
            double percentage = ZERO;
            if(!CollectionUtils.isEmpty(toVertex))
            {
                String capitalStr = toVertex.getOrDefault(CAPITAL, ZERO).toString();
                double capital = StringUtils.isBlank(capitalStr) ? ZERO : Double.parseDouble(capitalStr);
                percentage = capital == 0 ? ZERO : (investAmount / capital > ONE ? ONE : investAmount / capital) * HUNDRED;
            }
            return percentage;
        }
    }

    @Component
    public static class TradableShareLabelMaker extends EdgeLabelMaker
    {
        public static final String TOTAL_STAKE_DISTRIBUTION = "total_stake_distribution";

        public TradableShareLabelMaker()
        {
            super(ArangoEdgeConf.TRADABLE_SHARE);
        }

        @Override
        public String makeLabel(Map<String, Object> edge, Map<String, Map<String, Object>> idVertexMap)
        {
            Double percentage = getInvestPercentage(edge, idVertexMap);
            return ZERO.equals(percentage) ? getEdgeCnName() : String.format("%s出资占比", format(percentage));
        }

        @Override
        public Double getInvestPercentage(Map<String, Object> edge, Map<String, Map<String, Object>> idVertexMap)
        {
            String percentage = edge.getOrDefault(TOTAL_STAKE_DISTRIBUTION, ZERO_PERCENTAGE).toString();
            return StringUtils.isBlank(percentage)? ZERO : Double.parseDouble(StringUtils.substringBefore(percentage, "%"));
        }
    }

    @Component
    public static class ActualControllerLabelMaker extends EdgeLabelMaker
    {
        public static final String RATIO = "ratio";

        public ActualControllerLabelMaker()
        {
            super(ArangoEdgeConf.ACTUAL_CONTROLLER);
        }

        @Override
        public String makeLabel(Map<String, Object> edge, Map<String, Map<String, Object>> idVertexMap)
        {
            Double percentage = getInvestPercentage(edge, idVertexMap);
            return ZERO.equals(percentage) ? getEdgeCnName() : String.format("%s%%间接控制", format(percentage));
        }

        @Override
        public Double getInvestPercentage(Map<String, Object> edge, Map<String, Map<String, Object>> idVertexMap)
        {
            String ratio = edge.getOrDefault(RATIO, ZERO).toString();
            return StringUtils.isBlank(ratio) ? ZERO : (Double.parseDouble(ratio) > ONE ? ONE : Double.parseDouble(ratio)) * HUNDRED;
        }
    }

    @Component
    public static class ControlShareHolderLabelMaker extends EdgeLabelMaker
    {
        public static final String RATIO = "ratio";

        public ControlShareHolderLabelMaker()
        {
            super(ArangoEdgeConf.CONTROL_SHAREHOLDER);
        }

        @Override
        public String makeLabel(Map<String, Object> edge, Map<String, Map<String, Object>> idVertexMap)
        {
            Double percentage = getInvestPercentage(edge, idVertexMap);
            return ZERO.equals(percentage) ? getEdgeCnName() : String.format("%s%%控股", format(percentage));
        }

        @Override
        public Double getInvestPercentage(Map<String, Object> edge, Map<String, Map<String, Object>> idVertexMap)
        {
            String ratio = edge.getOrDefault(RATIO, ZERO).toString();
            return StringUtils.isBlank(ratio) ? ZERO : (Double.parseDouble(ratio) > ONE ? ONE : Double.parseDouble(ratio)) * HUNDRED;
        }
    }

    @Component
    public static class DefaultLabelMaker extends EdgeLabelMaker
    {
        public DefaultLabelMaker()
        {
            super(null);
        }

        @Override
        public String getEdgeCnName()
        {
            return "";
        }

        @Override
        public String makeLabel(Map<String, Object> edge, Map<String, Map<String, Object>> idVertexMap)
        {
            String table = StringUtils.substringBefore(edge.get("_id").toString(), "/");
            GraphEdgeLabel edgeLabel = GraphEdgeLabel.EDGE_LABEL_CONF_MAP.get(table);
            if(edgeLabel != null && StringUtils.isNotBlank(edgeLabel.getField()))
                return edge.getOrDefault(edgeLabel.getField(), edgeLabel.getTableCnName()).toString();
            return edgeLabel != null ? edgeLabel.getTableCnName() : "";
        }

        @Override
        public Double getInvestPercentage(Map<String, Object> edge, Map<String, Map<String, Object>> idVertexMap)
        {
            return null;
        }
    }
}
