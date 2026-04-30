package com.rama.cloudstorage.service.user;

import com.rama.cloudstorage.dto.user.UserRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void register(UserRequestDto dto) {
        log.info("Starting registration new user: {}", dto.username());
        userService.save(dto.username(), dto.password());
        log.info("User '{}' successfully registered", dto.username());

    }

    public Authentication authenticate(String username, String password) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        log.info("User '{}' logged in successfully", username);
        return auth;
    }
}
