package com.haizhi.iap.configure.exception;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.WrapperProvider;

/**
 * Created by chenbo on 17/4/14.
 */
public enum ConfigException implements WrapperProvider {

    MISS_FIRST_MENU_LOCATION(701, "一级导航的位置不能为空"),

    MISS_FIRST_MENU_NAME(702, "一级导航的名称不能为空"),

    FIRST_MENU_LIMIT(703, "一级导航的名称不能超过8个字符"),

    FIRST_MENU_NAME_REPEAT(704, "一级导航的名称不能重复"),

    MISS_SECOND_MENU_ORDER(705, "二级导航的顺序不能为空"),

    MISS_SECOND_MENU_NAME(706, "二级导航的名称不能为空"),

    SECOND_MENU_LIMIT(707, "二级导航的名称不能超过24个字符"),

    SECOND_MENU_NAME_REPEAT(708, "二级导航的名称不能重复"),

    MISS_MENU_COMPONENT_DATASOURCE(709, "内容组件对应的数据源不能为空"),

    MISS_COMPONENT_ID(710, "component_id不能为空"),

    MISS_ITEM_LOCATION(711, "元素的位置描述不能为空"),

    MISS_ITEM_FIELD(712, "元素对应的数据源字段不能为空"),

    MISS_ITEM_NAME(713, "元素对应的名称不能为空"),

    ITEM_NAME_LIMIT(714, "元素对应的名称不能超过24个字符"),

    MISS_ITEM_SPACEINFO(715, "元素所占空间不能为空"),

    ITEM_NAME_REPEAT(716, "元素名称在同一组件中不能重复"),

    MISS_ITEM_COMPONENT_ID(717, "元素对应的ele_component_id不能为空"),

    MISS_ITEM_SOURCE_FIELD_ID(718, "组件对应源字段id不能为空"),

    MISS_FIRST_MENU(719, "一级导航缺失"),

    OVER_LIMIT_FIRST_MENU(720, "导航卡片数最多只能添加9个"),

    WRONG_DATASOURCE_ID(721, "含有错误的datasourceId"),

    MISS_SOURCE_NAMES(722, "参数source_names缺失"),

    NO_SUCH_SOURCEFIELD(723, "不存在的sourceField"),

    WRONG_FIELD_IN_SOURCE(724, "请在正确的数据源中选择源字段"),

    WRONG_SOURCE_NAME(725, "错误的数据源表名"),

    NO_PAGE_PARAMETER(726, "开启分页后offset不能为空"),

    NOT_EXIST_COMPONENT(727, "组件不存在"),

    NO_SOURCE_CHOICE(728, "组件未选取数据源"),

    NO_COMPANY_NAME(729, "公司名不能为空"),

    MISS_PARAM_SOURCE_ID(730, "数据源参数的datasource_id不能为空"),

    MISS_PARAM_ORDER_KEY(731, "数据源参数的排序字段不能为空"),

    MISS_FIRST_MENU_FIX(732, "一级导航的固定参数不能为空"),

    MISS_ITEM_TYPE(733, "组件元素类型不能为空"),

    MISS_ITEM_COUNT_FORM(734, "计算方式不能为空"),

    KEY_IS_NOT_NUM(735, "字段为非数值型"),

    NAME_ALREADY_EXISTS(736, "主题名称已存在"),

    MISS_SOURCE_TYPE(737, "参数source_type不能为空"),

    UNSUPPORTED_SOURCE_TYPE(738, "不支持的数据源类型"),

    WRONG_CONN(739, "连接不可用"),

    MISS_NAME(740, "参数name不能为空"),

    MISS_DATA_TYPE(741, "参数data_type不能为空"),

    MISS_HOST(742, "参数host不能为空"),

    MISS_PORT(743, "参数port不能为空"),

    MISS_USERNAME(744, "参数username不能为空"),

    MISS_DATABASE(745, "参数database不能为空"),

    MISS_SOURCE_TABLE(746, "参数source_table不能为空"),

    MISS_FIELD_MAP_LIST(747, "字段映射列表field_map_list不能为空"),

    MISS_FROM_TO_MAP(748, "图数据缺少from或to的字段映射"),

    MULTIPLE_KEY(749, "不能包含多个主键"),

    MISS_SOURCE_CONFIG_ID(750, "参数source_config_id不能为空"),

    MISS_GRAPH_ID(751, "参数graph_id缺失"),

    MISS_GRAPH_NAME(752, "图名字不能为空"),

    SHUT_IMPORTING_FIRST(753, "该主题有导入正在进行,请先停止导入!"),

    WRONG_UPDATE_MODE(754, "错误的update_mode值"),

    MISS_SOURCE_FIELD_OR_NAME(755, "source_field或name不能为空"),

    DULPLICATE_SOURCE_FIELD_OR_NAME(756, "source_field或name不允许重复"),

    ABORTED_NOT_ALLOWED(757, "任务状态已不是导入中，不能中止");

    private Integer status;
    private String msg;

    ConfigException(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public Wrapper get() {
        return Wrapper.builder().status(status).msg(msg).build();
    }
}
