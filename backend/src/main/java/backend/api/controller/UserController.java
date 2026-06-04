package backend.api.controller;

import backend.domain.User;
import backend.api.dto.UserResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponseDto.fromUser(user));
    }
}