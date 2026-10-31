package com.gdufe.mall.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gdufe.mall.common.exception.BizException;
import com.gdufe.mall.common.result.ResultCode;
import com.gdufe.mall.common.security.JwtUtil;
import com.gdufe.mall.common.security.SecurityUtil;
import com.gdufe.mall.dto.LoginDTO;
import com.gdufe.mall.dto.RegisterDTO;
import com.gdufe.mall.entity.User;
import com.gdufe.mall.mapper.UserMapper;
import com.gdufe.mall.service.AuthService;
import com.gdufe.mall.vo.LoginVO;
import com.gdufe.mall.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        return doLogin(dto, false);
    }

    @Override
    public LoginVO adminLogin(LoginDTO dto) {
        return doLogin(dto, true);
    }

    @Override
    public LoginVO register(RegisterDTO dto) {
        Long exists = userMapper.selectCount(Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, dto.getUsername()));
        if (exists != null && exists > 0) {
            throw new BizException(ResultCode.DUPLICATE_OPERATION, "用户名已被占用");
        }
        String email = StringUtils.hasText(dto.getEmail()) ? dto.getEmail().trim() : null;
        if (email != null) {
            Long emailExists = userMapper.selectCount(Wrappers.lambdaQuery(User.class)
                    .eq(User::getEmail, email));
            if (emailExists != null && emailExists > 0) {
                throw new BizException(ResultCode.DUPLICATE_OPERATION, "邮箱已被注册");
            }
        }

        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname().trim() : dto.getUsername().trim());
        // 空邮箱必须存 NULL：uk_email 唯一索引下多个空串会冲突（契约边界 #35）
        user.setEmail(email);
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);

        String token = jwtUtil.create(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, toVO(user));
    }

    @Override
    public UserVO currentUser() {
        Long userId = SecurityUtil.currentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "用户不存在或已被删除");
        }
        return toVO(user);
    }

    @Override
    public void logout() {
        // 无状态 JWT：服务端不维护黑名单，前端清本地 token 即可；此处只留痕便于排查
        log.info("用户退出登录：{}", SecurityUtil.current().getUsername());
    }

    private LoginVO doLogin(LoginDTO dto, boolean adminOnly) {
        User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, dto.getUsername().trim()));
        // 用户名不存在与密码错误返回同一提示，避免被枚举
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.LOGIN_FAILED);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        if (adminOnly && !"ADMIN".equals(user.getRole())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        String token = jwtUtil.create(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, toVO(user));
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        return vo;
    }
}
