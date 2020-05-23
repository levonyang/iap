package com.haizhi.iap.account.controller;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.haizhi.iap.account.controller.model.Authorization;
import com.haizhi.iap.account.controller.model.ModifyPassword;
import com.haizhi.iap.account.exception.AccountException;
import com.haizhi.iap.account.model.User;
import com.haizhi.iap.account.repo.UserRepo;
import com.haizhi.iap.account.service.UserService;
import com.haizhi.iap.account.utils.SecretUtil;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.DeviceUtil;

/**
 * Created by chenbo on 17/5/31.
 */
@Api(tags="【账号-用户模块】用户登录登出操作")
@RestController
@RequestMapping(value = "/account/user")
public class UserController {
    @Setter
    @Autowired
    UserService userService;

    @Setter
    @Autowired
    UserRepo userRepo;

    @Setter
    @Autowired
    private HttpServletRequest request;

    private static Pattern VERIFY_PASSWORD_MATCHER = Pattern.compile("^[@A-Za-z0-9!#\\$%\\^&\\*\\.~]{6,20}$");

    @RequestMapping(value = "/login", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Authorization login(@RequestBody User user){
        if (Strings.isNullOrEmpty(user.getUsername()) || Strings.isNullOrEmpty(user.getPassword())) {
            throw new ServiceAccessException(AccountException.USER_PASS_NOT_PROVIDED);
        }

        //des加密解密
        user.setPassword(getRealPassword(user.getPassword()));

        return userService.login(user, DeviceUtil.getChannel(request));
    }
    
    @RequestMapping(value = "/auto-login", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Authorization autoLogin(@RequestBody User user){
        if (Strings.isNullOrEmpty(user.getUsername())) {
            throw new ServiceAccessException(AccountException.USER_PASS_NOT_PROVIDED);
        }

        return userService.autoLogin(user, DeviceUtil.getChannel(request));
    }

    /**
     * 仅仅作为测试接口，测试结束删除
     * @param username
     * @return
     */
    @RequestMapping(value = "/test-login", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Authorization testLogin(@RequestParam("username") String username){
        if (Strings.isNullOrEmpty(username)) {
            throw new ServiceAccessException(AccountException.USER_PASS_NOT_PROVIDED);
        }
        User user = new User();
        user.setUsername(username);
        return userService.autoLogin(user, DeviceUtil.getChannel(request));
    }

    @RequestMapping(value = "/reg", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper register(@RequestBody User register){
        if (Strings.isNullOrEmpty(register.getUsername()) || Strings.isNullOrEmpty(register.getPassword())) {
            return AccountException.USER_PASS_NOT_PROVIDED.get();
        } else {
            register.setPassword(getRealPassword(register.getPassword()));

            if(userRepo.countAll() >= 500){
                return AccountException.OVER_LIMIT.get();
            }
            User user = userRepo.findByUsernameIgnoreDel(register.getUsername());
            if (user != null && user.getId() != null) {
                return AccountException.USERNAME_EXISTS.get();
            }
        }

        userService.create(register);
        return Wrapper.OK;
    }

    @RequestMapping(value = "/", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper modify(@RequestBody User user){
        Long userId = DefaultSecurityContext.getUserId();
        User dbUser = userRepo.findById(userId);
        if(dbUser == null){
            return AccountException.USER_NOT_EXISTS.get();
        }
        dbUser.setEmail(user.getEmail() == null ? "" : user.getEmail());
        dbUser.setPhone(user.getPhone() == null ? "" : user.getPhone());
        userRepo.update(dbUser);
        return Wrapper.OK;
    }

    @RequestMapping(value = "/modify_password", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper modifyPassword(@RequestBody ModifyPassword modify){
        if (modify == null || Strings.isNullOrEmpty(modify.getOldPassword()) || Strings.isNullOrEmpty(modify.getNewPassword())) {
            return AccountException.MISSING_PASSWORD.get();
        }
        User user = userRepo.findById(DefaultSecurityContext.getUserId());
        if (user == null || user.getId() == null) {
            return AccountException.USER_NOT_EXISTS.get();
        }

        modify.setOldPassword(getRealPassword(modify.getOldPassword()));
        modify.setNewPassword(getRealPassword(modify.getNewPassword()));

        if (!SecretUtil.checkHashPassword(modify.getOldPassword(), user.getPassword())) {
            return AccountException.WRONG_ORIGINAL_PASS.get();
        }

        if (!VERIFY_PASSWORD_MATCHER.matcher(modify.getNewPassword()).matches()) {
            return AccountException.ILLEGAL_NEW_PASSWORD.get();
        }

        if(modify.getNewPassword().equals(modify.getOldPassword())) {
            return AccountException.OLD_PW_EQ_NEW_PW.get();
        }

        user.setPassword(SecretUtil.genHashPassword(modify.getNewPassword()));
        userRepo.updatePassword(user);

        return Wrapper.OK;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper logout(){
        userService.clearSession(DefaultSecurityContext.getUserId(), DeviceUtil.getChannel(request));
        return Wrapper.OK;
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper profile(){
        Long userId = DefaultSecurityContext.getUserId();
        if (userId == null) {
            return Wrapper.OK;
        } else {
            User user = userRepo.findById(userId);
            if(user == null){
                return AccountException.USER_NOT_EXISTS.get();
            }
            user.mask();
            return Wrapper.OKBuilder.data(user).build();
        }
    }

    @RequestMapping(value = "/check_name", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper checkNameIsValid(@RequestParam("username") String username){
        User user = userRepo.findByUsernameIgnoreDel(username);
        return Wrapper.OKBuilder.data(user == null).build();
    }

    //对前端传来的加密后的密码解密
    private String getRealPassword(String encyptPwd){
        return com.haizhi.iap.common.utils.SecretUtil.decodeDes(encyptPwd);
    }
}
