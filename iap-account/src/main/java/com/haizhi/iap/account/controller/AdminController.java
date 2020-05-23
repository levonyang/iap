package com.haizhi.iap.account.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.haizhi.iap.account.exception.AccountException;
import com.haizhi.iap.account.model.User;
import com.haizhi.iap.account.model.UserGroup;
import com.haizhi.iap.account.repo.UserGroupRepo;
import com.haizhi.iap.account.repo.UserRepo;
import com.haizhi.iap.account.utils.SecretUtil;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import io.swagger.annotations.Api;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/5/31.
 */
@Api(tags="【账号-用户管理模块】对用户及用户分组进行增删改查")
@RestController
@RequestMapping(value = "/admin")
public class AdminController {

    @Setter
    @Autowired
    UserGroupRepo userGroupRepo;

    @Setter
    @Autowired
    UserRepo userRepo;

    /**
     * 群组列表
     *
     * @return
     */
    @RequestMapping(value = "/group", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper viewGroup() {
        Map<String, Object> result = Maps.newHashMap();
        List<UserGroup> groupList = userGroupRepo.getAll();
        for (UserGroup group : groupList) {
            Long count = userRepo.countByGroup(group.getId());
            group.setUserCount(count);
        }
        result.put("data", groupList);
        result.put("total_count", userRepo.countAll());
        //构建一个"全部"组
        UserGroup allGroup = new UserGroup();
        allGroup.setName("全部");
        allGroup.setId(0l);
        allGroup.setUserCount(userRepo.countAll());
        groupList.add(allGroup);
        Collections.sort(groupList, (group1, group2) -> {
            if (group1.getName().equals("全部") || group2.getName().equals("全部")) {
                return -1;
            } else {
                return group2.getName().compareTo(group1.getName());
            }
        });
        return Wrapper.OKBuilder.data(result).build();
    }

    /**
     * 用户列表
     *
     * @param count
     * @param offset
     * @return
     */
    @RequestMapping(value = "/users/{group_id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper users(@PathVariable("group_id") Long groupId,
                         @RequestParam(value = "offset", required = false) Integer offset,
                         @RequestParam(value = "count", required = false) Integer count) {
        Map<String, Object> result = Maps.newHashMap();
        List<User> users;
        if(offset == null){
            offset = 0;
        }
        if(count == null){
            count = 10;
        }
        if (groupId == null || groupId.equals(0l)) {
            users = userRepo.findByCondition(null, offset, count);
            result.put("total_count", userRepo.countAll());
        } else {
            users = userRepo.findByCondition(groupId, offset, count);
            result.put("total_count", userRepo.countByGroup(groupId));
        }
        users.forEach(User::mask);
        result.put("data", users);
        return Wrapper.OKBuilder.data(result).build();
    }

    @RequestMapping(value = "/user/modify", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper modifyUser(@RequestBody User user) {
        if(user == null || user.getId() == null){
            return AccountException.MISS_ID.get();
        }
        User dbUser = userRepo.findById(user.getId());
        if (dbUser == null) {
            return AccountException.USER_NOT_EXISTS.get();
        }
        if (user.getActivated() != null) {
            dbUser.setActivated(user.getActivated());
        }
        if (user.getRoleId() != null) {
            dbUser.setRoleId(user.getRoleId());
        }
        if(user.getGroupId() != null && !user.getGroupId().equals(0l)) {
            dbUser.setGroupId(user.getGroupId());
        }

        userRepo.update(dbUser);
        dbUser.mask();
        return Wrapper.OKBuilder.data(dbUser).build();
    }

    @RequestMapping(value = "/user/{user_id}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper deleteUser(@PathVariable("user_id") Long userId) {
        Long curUserId = DefaultSecurityContext.getUserId();
        if(userId.equals(curUserId)) {
            return AccountException.WRONG_DELETE_MYSELF.get();
        }

        userRepo.delete(userId);
        return Wrapper.OK;
    }

    @RequestMapping(value = "/user/reset/{user_id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper resetPwd(@PathVariable("user_id") Long userId) {
        User user = userRepo.findById(userId);
        if (user == null) {
            return AccountException.USER_NOT_EXISTS.get();
        }
        String randomPwd = SecretUtil.genPwd();
        user.setPassword(SecretUtil.genHashPassword(randomPwd));
        userRepo.updatePassword(user);
        return Wrapper.OKBuilder.data(randomPwd).build();
    }

    @RequestMapping(value = "/group", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper addGroup(@RequestBody UserGroup group) {
        if (Strings.isNullOrEmpty(group.getName())) {
            return AccountException.USER_GROUP_MISS.get();
        } else {
            if(group.getName().equals("全部")){
                return AccountException.SYS_GROUP_NAME.get();
            }
            if(group.getName().length() > 16) {
                return AccountException.NAME_LIMIT.get();
            }

            UserGroup dbGroup = userGroupRepo.findByName(group.getName());
            if (dbGroup != null && dbGroup.getId() != null) {
                return AccountException.USER_GROUP_EXISTS.get();
            }
        }

        userGroupRepo.create(group);
        return Wrapper.OK;
    }

    @RequestMapping(value = "/group", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper modifyGroup(@RequestBody UserGroup group) {
        if(group.getId() == null || group.getId().equals(0l)){
            return AccountException.MISS_ID.get();
        }
        if (Strings.isNullOrEmpty(group.getName())) {
            return AccountException.USER_GROUP_MISS.get();
        }
        if(group.getName().equals("全部")){
            return AccountException.SYS_GROUP_NAME.get();
        }
        if(group.getName().length() > 16) {
            return AccountException.NAME_LIMIT.get();
        }

        UserGroup existGroup = userGroupRepo.findByName(group.getName());
        if (existGroup != null && existGroup.getId() != group.getId()) {
            return AccountException.USER_GROUP_EXISTS.get();
        }

        UserGroup dbGroup = userGroupRepo.findById(group.getId());
        if(dbGroup == null){
            return AccountException.USER_GROUP_NOT_EXISTS.get();
        }

        dbGroup.setName(group.getName());
        userGroupRepo.update(dbGroup);
        return Wrapper.OK;
    }

    @RequestMapping(value = "/group/{user_id}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper removeFromGroup(@PathVariable("user_id") Long userId) {
        User dbUser = userRepo.findById(userId);
        if (dbUser == null) {
            return AccountException.USER_NOT_EXISTS.get();
        }

        userRepo.removeFromGroup(userId);
        return Wrapper.OK;
    }

    @RequestMapping(value = "/rm_group", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper deleteGroup(@RequestParam("group_id") Long groupId) {
        UserGroup dbGroup = userGroupRepo.findById(groupId);
        if(dbGroup == null){
            return AccountException.USER_GROUP_NOT_EXISTS.get();
        }

        userGroupRepo.delete(groupId);
        return Wrapper.OK;
    }
}
