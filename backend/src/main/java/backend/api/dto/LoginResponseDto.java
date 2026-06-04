package backend.api.dto;

import backend.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private String token;
    private String tokenType;
    private long expiresIn;
    private String username;
    private Role role;
}