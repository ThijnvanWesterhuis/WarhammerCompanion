package backend.application.service;

import backend.domain.Role;
import backend.domain.User;
import backend.api.dto.LoginResponseDto;
import backend.api.dto.LoginUserDto;
import backend.api.dto.RegisterUserDto;
import backend.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDto register(RegisterUserDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already in use");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        return createAuthResponse(savedUser);
    }

    public LoginResponseDto login(LoginUserDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return createAuthResponse(user);
    }

    private LoginResponseDto createAuthResponse(User user) {
        String token = jwtService.generateToken(user);

        return new LoginResponseDto(
                token,
                "Bearer",
                jwtService.getExpirationTime(),
                user.getUsername(),
                user.getRole()
        );
    }
}