package com.haizhi.iap.follow.exception;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.WrapperProvider;
import com.haizhi.iap.follow.enums.LimitConfig;

/**
 * Created by chenbo on 17/1/10.
 */
public enum FollowException implements WrapperProvider {
    UNSUPPORT_FILE(701, "不支持的文件类型"),

    NO_CACHE_KEY(702, "cache_key不能为空"),

    NO_GROUP_ID(703, "group_id不能为空"),

    OPERATION_TIMEOUT(704, "处理超时"),

    CRAWLER_SCHEDULE(705, "抓取调度失败"),

    NO_FOLLOW_LIST_ID(706, "follow_list_id不能为空"),

    FOLLOW_LIST_NOT_EXIST(707, "follow_list不存在"),

    USER_NOT_EXISTS(708, "用户不存在"),

    NO_NAME(709, "name不能为空"),

    BATCH_FAILED(710, "批量入库失败"),

    NO_TASK_NAME(711, "task_name不能为空"),

    NO_TASK_DATE(712, "开始日期或截止时间不能为空"),

    NO_TIME_OPTION(713, "time_option不能为空"),

    NO_FILE_CONTENT(714, "文件内容不能为空"),

    NO_SUB_DIR(715, "参数sub_dir不能为空"),

    NO_PAGE(716, "参数page不能为空"),

    NO_PAGE_SIZE(717, "参数page_size不能为空"),

    NAME_ALREADY_USED(718, "该分组已存在"),

    MISS_PARAM(719, "参数缺失"),

    MISS_COMPANY_NAME(720, "参数company_name不能为空"),

    NOT_BELONG_TO(721, "操作对象不属于当前用户"),

    MISS_TASK_ID(722, "参数task_id不能为空"),

    NO_THIS_TASK(723, "没有此任务"),

    MISS_BODY(724, "无请求body"),

    NAME_LIMIT(725, "组名不能超过16个字符"),

    BEGIN_TIME_T_START_TIME(726, "开始时间大于或等于结束时间"),

    ALEARDY_HAS_TASK_NAME(727, "任务名称已经存在"),

    SPECIAL_CHAR(728, "任务名称不能包含 / @ # $ & 这些字符"),

    OVER_LIMIT_NAME(729, "任务名超出长度限制"),

    WRONG_TYPE(730, "参数type有误"),

    MISS_READ(731, "参数read不能为空"),

    MISS_COLLECTED(732, "参数collected不能为空"),

    MISS_IDS(733, "参数ids不能为空"),

    MISS_ID(734, "参数id不能为空"),

    MISS_COMPANIES(735, "参数companies不能为空"),

    WRONG_COMPANIES(736, "参数companies数据类型错误"),

    MISS_TYPE(737, "参数type不能为空"),

    MISS_OPERATION(738, "参数operation不能为空"),

    WRONG_OPERATION(739, "参数operation有误"),

    WRONG_SUB_TYPE(740, "参数sub_type有误"),

    OVER_LIMIT_IMPORT(741, "导入的条数不能超过100000条"),

    OVER_LIMIT_LIST_NUM_PER_USER(742, "您关注的企业分组数已达上限(" + LimitConfig.LIST_NUM_PER_USER + "个),请先删除部分企业分组"),

    OVER_LIMIT_ITEM_NUM_PER_LIST(743, "单个分组企业数已达上限(10w),请先移除部分关注企业"),

    OVER_LIMIT_ITEM_SUM_PER_USER(744, "您关注的企业数已达上限(60w),请先移除部分关注企业"),

    PDF_OPTION_FAILE(755, "导出PDF报告选项读取失败"),

    PDF_SCREEN_SHOT_FAILED(756, "截图失败"),

    OVER_LIMIT_KEYWORD(757, "最多可选10个关键词,可删除已选关键词再选择新的关键词"),

    MISS_RULE_TYPE(758, "参数ruleType不能为空"),

    NO_COMPANIES_TYPE(759, "集团下没有实体信息"),

    EMPTY_IMPORT(760, "导入数据为空"),

    READ_IMPORT_ERROR(761, "读取导入数据失败"),

    IMPORT_MIN_LIMIT_TWO(762,"查询企业个数不得少于两个"),

    IMPORT_BEYOND_THRESHOLD(763,"查询企业个数建议不要超过@size个");

    private Integer status;
    private String msg;

    FollowException(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    @Override
    public Wrapper get() {
        return Wrapper.builder().status(status).msg(msg).build();
    }

    public FollowException set(String key,String value){
        this.msg = this.msg.replace("@"+key,value);
        return this;
    }
}