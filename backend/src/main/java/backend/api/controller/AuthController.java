package backend.api.controller;

import backend.api.dto.LoginResponseDto;
import backend.api.dto.LoginUserDto;
import backend.api.dto.RegisterUserDto;
import backend.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDto> register(@Valid @RequestBody RegisterUserDto request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginUserDto request) {
        return ResponseEntity.ok(userService.login(request));
    }
}