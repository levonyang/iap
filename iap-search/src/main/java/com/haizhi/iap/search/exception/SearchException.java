package com.haizhi.iap.search.exception;


import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.WrapperProvider;

/**
 * Created by jianghailong on 2016/9/26.
 */
public enum SearchException implements WrapperProvider {

    MISS_KEY_WORD(801, "参数key_word不能为空"),

    MISS_TYPE(802, "参数type不能为空"),

    WRONG_TYPE(803, "参数type有误"),

    MISS_NAME(804, "参数name不能为空"),

    WRONG_FINANCIAL_REPORT_TYPE(805, "不支持的caibao_type"),

    UN_CRAW_DATA(806, "暂未收录的数据"),

    PDF_GENERATE_ERROR(807, "pdf生成出错"),

    MISS_STOCK_CODE(808, "参数stock_code不能为空"),

    MISS_YEAR_QUARTER(809, "参数year_quarter不能为空"),

    MISS_CAIBAO_TYPE(810, "参数caibao_type不能为空"),

    MISS_COMPANY(811, "参数company不能为空"),

    NO_SUCH_COMPANY(812, "没有此公司"),

    MISS_EDGES(813, "参数edges不能为空"),

    MISS_OPTIONS(814, "参数options不能为空"),

    MISS_ID(815, "参数id不能为空"),

    GRAPH_SERVER_ERROR(816, "图谱服务器异常"),

    MISS_COMPANY1(817, "参数company1不能为空"),

    MISS_GROUP_ID(818, "参数groupId不能为空"),

    MISS_ENTITY_ID(819, "参数entityId不能为空"),

    MISS_PERSONA(820, "参数person1不能为空"),

    MISS_PERSONB(821, "参数person2不能为空"),

    MISS_COLLECTION(822, "参数collection不能为空"),

    MISS_RECORD_ID(823, "参数record_id不能为空"),

    WRONG_COLLECTION(824, "不支持的collection"),

    NO_DATA(825, "无数据"),

    NOT_PAGEABLE(826, "此sub_type暂不支持分页"),

    UN_SUPPORTED_STOCK_TYPE(827, "此公司不支持该stock_type"),

    UN_SUPPORTED_STOCK_CODE(828, "此公司不支持该stock_code"),

    WRONG_INFO_TYPE(829, "错误的info_type"),

    WRONG_ARANGO_DATA(830, "图谱数据有误,请联系构图人员"),

    FILE_NOT_FOUND(831, "文件不存在"),

    MISS_DOMAIN(832, "缺少企业域相关参数"),

    WRONG_DOMAIN_COMB(833, "错误的企业域参数组合"),

    MISS_ENTITY(834, "entity相关参数不能为空"),

    MISS_GROUP_TYPE(835, "参数group_type不能为空"),

    WRONG_RESULT_TYPE(836, "错误的参数result_type"),

    MISS_PATH_TYPE(837, "参数path_type不能为空"),

    MISS_FROM_LIST(838, "参数from_list不能为空"),

    MISS_THIRD_TYPE(839, "查新闻舆情时,参数third_type不能为空"),

    UNIDENTIFIED_RULE(840, "不能识别的rule"),

    MISS_DEPTH_LIST(841, "参数depth_list不能为空"),

    UNSUPPORTED_SUB_TYPE(842, "不支持的sub_type"),

    UNSUPPORTED_THIRD_TYPE(843, "不支持的third_type"),

    MISS_SUB_TYPE(844, "参数sub_type不能为空"),

    MISS_GROUP_NAME(845, "参数group_name不能为空");

    private int code;
    private String msg;

    SearchException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public Wrapper get() {
        return Wrapper.builder().status(code).msg(msg).build();
    }

}
