package com.aiposter.auth;

import com.aiposter.auth.dto.CurrentUserResponse;
import com.aiposter.auth.dto.LoginRequest;
import com.aiposter.auth.dto.LoginResponse;
import com.aiposter.common.BusinessException;
import com.aiposter.security.JwtTokenProvider;
import com.aiposter.security.LoginUser;
import com.aiposter.user.UserEntity;
import com.aiposter.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername().trim();
        log.info("用户登录请求: username={}", username);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("用户登录失败: username={}, reason={}", username, "用户名或密码错误");
                    return new BusinessException("AUTH_FAILED", "用户名或密码错误");
                });

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            log.warn("用户登录失败: userId={}, username={}, reason={}", user.getId(), user.getUsername(), "账号已被禁用");
            throw new BusinessException("USER_DISABLED", "账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("用户登录失败: userId={}, username={}, reason={}", user.getId(), user.getUsername(), "用户名或密码错误");
            throw new BusinessException("AUTH_FAILED", "用户名或密码错误");
        }

        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername());
        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return new LoginResponse(token, toCurrentUser(user));
    }

    public CurrentUserResponse currentUser(LoginUser loginUser) {
        log.debug("获取当前用户信息: userId={}", loginUser.getId());
        UserEntity user = userRepository.findById(loginUser.getId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        return toCurrentUser(user);
    }

    private CurrentUserResponse toCurrentUser(UserEntity user) {
        String nickname = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                nickname,
                user.getAvatar(),
                user.getRole()
        );
    }
}
