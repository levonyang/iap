package com.haizhi.iap.tag.controller.param;

import lombok.Data;

/**
 * @Author dmy
 * @Date 2017/11/28 上午11:10.
 */
@Data
public class TagParticipleParam {
    String content;
    boolean r_tag_id = true;
    boolean r_start_index = true;
    boolean r_end_index = true;
    boolean r_tag_name = true;
    boolean r_tag_fname = true;
    boolean r_tag_type = true;
    boolean r_tag_pids = true;
    boolean r_tag_pnames = false;
    boolean r_tag_pfnames = false;

}
