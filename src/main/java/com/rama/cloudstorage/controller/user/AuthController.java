package com.rama.cloudstorage.controller.user;

import com.rama.cloudstorage.dto.user.UserRequestDto;
import com.rama.cloudstorage.dto.user.UserResponseDto;
import com.rama.cloudstorage.service.user.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDto> createUserAccount(@Valid @RequestBody UserRequestDto userRequestDto,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response) {

        authService.register(userRequestDto);

        Authentication authentication = authService.authenticate(userRequestDto.username(), userRequestDto.password());

        saveSessionContext(authentication, request, response);

        return ResponseEntity
                .status(CREATED)
                .body(new UserResponseDto(userRequestDto.username()));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDto> loginUserAccount(@Valid @RequestBody UserRequestDto userRequestDto,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {

        Authentication authentication = authService.authenticate(userRequestDto.username(), userRequestDto.password());

        saveSessionContext(authentication, request, response);

        return ResponseEntity.ok(new UserResponseDto(userRequestDto.username()));
    }

    private void saveSessionContext(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        request.getSession(true);

        request.changeSessionId();

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);

        securityContextRepository.saveContext(securityContext, request, response);
    }
}
