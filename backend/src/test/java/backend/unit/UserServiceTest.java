package backend.unit;

import backend.api.dto.LoginResponseDto;
import backend.api.dto.LoginUserDto;
import backend.api.dto.RegisterUserDto;
import backend.api.exception.FieldValidationException;
import backend.application.service.JwtService;
import backend.application.service.UserService;
import backend.data.repository.UserRepository;
import backend.domain.Role;
import backend.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerTrimsUserInputHashesPasswordAndReturnsToken() {
        RegisterUserDto request = registerRequest(" commander ", " commander@example.com ", "Password123!");
        User savedUser = User.builder()
                .id(1L)
                .username("commander")
                .email("commander@example.com")
                .password("hashed-password")
                .role(Role.USER)
                .build();

        when(passwordEncoder.encode("Password123!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(86400000L);

        LoginResponseDto response = userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertEquals("commander", userCaptor.getValue().getUsername());
        assertEquals("commander@example.com", userCaptor.getValue().getEmail());
        assertEquals("hashed-password", userCaptor.getValue().getPassword());
        assertEquals(Role.USER, userCaptor.getValue().getRole());
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void registerThrowsFieldValidationExceptionWhenPasswordsDoNotMatch() {
        RegisterUserDto request = registerRequest("commander", "commander@example.com", "Password123!");
        request.setConfirmPassword("Different123!");

        FieldValidationException exception = assertThrows(
                FieldValidationException.class,
                () -> userService.register(request)
        );

        assertEquals("Passwords do not match", exception.getErrors().get("confirmPassword"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginThrowsBadCredentialsWhenPasswordIsWrong() {
        LoginUserDto request = new LoginUserDto();
        request.setUsername("commander");
        request.setPassword("wrong-password");

        User user = User.builder()
                .id(1L)
                .username("commander")
                .email("commander@example.com")
                .password("hashed-password")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsername("commander")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.login(request));
        verify(jwtService, never()).generateToken(any());
    }

    private RegisterUserDto registerRequest(String username, String email, String password) {
        RegisterUserDto request = new RegisterUserDto();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(password);
        return request;
    }
}