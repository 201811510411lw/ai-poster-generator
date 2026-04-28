package com.aiposter.auth;

import com.aiposter.auth.dto.CurrentUserResponse;
import com.aiposter.auth.dto.LoginRequest;
import com.aiposter.auth.dto.LoginResponse;
import com.aiposter.common.BusinessException;
import com.aiposter.security.JwtTokenProvider;
import com.aiposter.security.LoginUser;
import com.aiposter.user.UserEntity;
import com.aiposter.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {
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
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("AUTH_FAILED", "用户名或密码错误"));

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("USER_DISABLED", "账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("AUTH_FAILED", "用户名或密码错误");
        }

        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername());
        return new LoginResponse(token, toCurrentUser(user));
    }

    public CurrentUserResponse currentUser(LoginUser loginUser) {
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
