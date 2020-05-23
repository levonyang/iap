package com.haizhi.iap.follow.service;

import com.haizhi.iap.follow.model.Task;
import com.haizhi.iap.follow.model.atlas.AtlasRequest;
import com.haizhi.iap.follow.model.atlas.AtlasResponse;

import java.util.List;
import java.util.Map;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/10 11:29
 */
public interface CustdigService {

    /**
     * 保存上传名单并且创建挖掘任务
     * @param data
     * @param userid
     * @param username
     * @return
     */
    Task saveListAndCreateTask(byte[] data, String userid, String username);

    AtlasResponse getData(AtlasRequest request);

    /**
     * 查询上传企业列表
     * @return
     * @param userid
     */
    List<Map> queryUploadList(String userid);

    List<Map> test();

    /**
     * 获取任务信息
     * @param taskid
     * @return
     */
    Task getTask(long taskid);

    /**
     * 启动任务
     * @param taskid
     */
    void startTask(long taskid);

    void screenShot(String batchid);
}
