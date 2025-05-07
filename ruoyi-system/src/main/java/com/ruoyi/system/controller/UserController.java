package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Result;
import com.ruoyi.system.domain.entity.Scmoannouser;
import com.ruoyi.system.service.UserServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    @Autowired
    UserServer userServer;

    @RequestMapping("/findUsers")
    @CrossOrigin(origins = "*")
    public Result<List<Scmoannouser>> findUsers() {
        List<Scmoannouser> users = userServer.findUsers();

        // 遍历所有用户并转换头像
        for (Scmoannouser user : users) {
            if (user.getAvatar() != null) {
                String base64Avatar = Base64.getEncoder().encodeToString(user.getAvatar());
                user.setAvatarBase64(base64Avatar); // 添加 Base64 编码字段
                user.setAvatar(null); // 清除 BLOB 字段
            }
        }
        return Result.success(users);
    }

    @RequestMapping("/login")
    public Result<Scmoannouser> login(@RequestBody Map<String, String> map) {
        Scmoannouser user = userServer.findUserByUserNameAndPassword(map.get("userName"), map.get("password"));
        if(user != null ) {
            if(user.getIsVerified()==0){
                return Result.error("Your registration has not been approved by an administrator!");
            }
            if (user.getAvatar() != null) {
                String base64Avatar = Base64.getEncoder().encodeToString(user.getAvatar());
                user.setAvatarBase64(base64Avatar); // 添加 Base64 编码字段
                user.setAvatar(null);
            }
            return Result.success(user);
        }
        else
            return Result.error("the username or password is wrong!");
    }

    @RequestMapping("/deleteUserByUserID")
    @CrossOrigin(origins = "*")
    public void deleteUserByUserID(@RequestParam long userID){
        userServer.deleteUserByUserID(userID);
    }

    @RequestMapping("/updateUser")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> updateUser(@RequestParam("userId") Long userId,
                                             @RequestParam("userName") String userName,
                                             @RequestParam("email") String email,
                                             @RequestParam("phone") String phone,
                                             @RequestParam("isAdmin") Long isAdmin,
                                             @RequestParam(value = "psw", required = false) String psw,
                                             @RequestParam(value = "avatar", required = false) MultipartFile avatar) {

        try {
            // 处理用户信息
            Scmoannouser user = new Scmoannouser();
            user.setUserId(userId);
            user.setUserName(userName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setIsAdmin(isAdmin);
            user.setPsw(psw);

            // 如果有上传的头像文件，处理头像文件
            if (avatar != null && !avatar.isEmpty()) {
                byte[] avatarBytes = avatar.getBytes();
                user.setAvatar(avatarBytes); // 头像字段存储为 BLOB
            }

            userServer.updateUser(user);

            return ResponseEntity.ok("User updated successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user.");
        }
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody Scmoannouser scmoannouser) {
        if(userServer.findUserByUserName(scmoannouser.getUserName())!=null) {
            return Result.error("The username already exists!");
        }
        else if(userServer.findUserByEmail(scmoannouser.getEmail())!=null) {
            return Result.error("The email address is registered!");
        }
        else if(userServer.findUserByPhone(scmoannouser.getPhone())!=null) {
            return Result.error("The phone number is registered!");
        }else{
            userServer.register(scmoannouser);
            return Result.success();
        }
    }

    @RequestMapping("/findUserByUserId")
    public ResponseEntity<Scmoannouser> findUserByUserId(@RequestBody Map<String, String> map) {
        Long userId = Long.parseLong(map.get("userId"));
        Scmoannouser user = userServer.findUserByUserId(userId);
        // 将头像从 BLOB 转换为 Base64 编码
        if (user.getAvatar() != null) {
            String base64Avatar = Base64.getEncoder().encodeToString(user.getAvatar());
            user.setAvatarBase64(base64Avatar); // 添加 Base64 编码字段
            user.setAvatar(null);
        }
        return ResponseEntity.ok(user);
    }

    @RequestMapping("/approveUser")
    @CrossOrigin(origins = "*")
    public Result<String> approveUser(@RequestParam("userId") Long userId) {
        userServer.approveUser(userId);
        return Result.success();
        }
}
