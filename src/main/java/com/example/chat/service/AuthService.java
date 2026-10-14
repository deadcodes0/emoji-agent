package com.example.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.chat.dto.*;
import com.example.chat.entity.User;
import com.example.chat.exception.BizException;
import com.example.chat.exception.UnauthorizedException;
import com.example.chat.mapper.UserMapper;
import com.example.chat.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final JwtService jwtService;

    public AuthService(UserMapper userMapper, JwtService jwtService) {
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getAccountNumber, req.accountNumber()));
        if (exists != null && exists > 0) {
            throw new BizException("账号已存在");
        }
        User u = new User();
        u.setAccountNumber(req.accountNumber());
        u.setPassword(req.password());
        u.setNickname(StringUtils.hasText(req.nickname())
                ? req.nickname() : "用户" + req.accountNumber());
        userMapper.insert(u);
        return buildResponse(u);
    }

    public LoginResponse login(LoginRequest req) {
        User u = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getAccountNumber, req.accountNumber()));
        if (u == null || u.getPassword() == null
                || !u.getPassword().equals(req.password())) {
            throw new BizException("账号或密码错误");
        }
        return buildResponse(u);
    }

    public UserVO me(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new UnauthorizedException("用户不存在");
        return UserVO.from(u);
    }

    /** 修改密码：校验旧密码 → 覆盖新密码。明文比对，保持表结构不动。 */
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest req) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new UnauthorizedException("用户不存在");

        if (u.getPassword() == null || !u.getPassword().equals(req.oldPassword())) {
            throw new BizException("原密码不正确");
        }
        if (req.oldPassword().equals(req.newPassword())) {
            throw new BizException("新密码不能与原密码相同");
        }

        u.setPassword(req.newPassword());
        userMapper.updateById(u);
    }

    private LoginResponse buildResponse(User u) {
        String token = jwtService.issue(u.getId());
        return new LoginResponse(token, jwtService.getExpireSeconds(), UserVO.from(u));
    }
    @Transactional
    public UserVO updateProfile(Long userId, UpdateProfileRequest req) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new UnauthorizedException("用户不存在");

        if (req.nickname() != null) {
            String nickname = req.nickname().trim();
            if (nickname.isEmpty()) throw new BizException("昵称不能为空");
            if (nickname.length() > 32) throw new BizException("昵称最长 32 字符");
            u.setNickname(nickname);
        }

        if (req.avatarUrl() != null) {
            String url = req.avatarUrl().trim();
            if (url.length() > 1_000_000) throw new BizException("头像文件过大");
            u.setAvatarUrl(url.isEmpty() ? null : url);
        }

        userMapper.updateById(u);
        return UserVO.from(u);
    }
}