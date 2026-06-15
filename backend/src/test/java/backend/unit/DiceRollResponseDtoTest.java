package backend.unit;

import backend.api.dto.DiceRollResponseDto;
import backend.domain.DiceRoll;
import backend.domain.DiceType;
import backend.domain.Role;
import backend.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiceRollResponseDtoTest {

    @Test
    void fromRollCalculatesTotalSuccessesAndFails() {
        DiceRoll roll = DiceRoll.builder()
                .id(10L)
                .user(testUser())
                .diceType(DiceType.D6)
                .diceCount(5)
                .successThreshold(4)
                .results(List.of(1, 3, 4, 5, 6))
                .createdAt(LocalDateTime.now())
                .build();

        DiceRollResponseDto response = DiceRollResponseDto.fromRoll(roll);

        assertEquals(19, response.getTotal());
        assertEquals(3, response.getSuccessCount());
        assertEquals(2, response.getFailCount());
    }

    @Test
    void fromRollLeavesSuccessCountsEmptyWhenThresholdIsNotSet() {
        DiceRoll roll = DiceRoll.builder()
                .id(11L)
                .user(testUser())
                .diceType(DiceType.D10)
                .diceCount(3)
                .successThreshold(null)
                .results(List.of(2, 8, 10))
                .createdAt(LocalDateTime.now())
                .build();

        DiceRollResponseDto response = DiceRollResponseDto.fromRoll(roll);

        assertEquals(20, response.getTotal());
        assertNull(response.getSuccessCount());
        assertNull(response.getFailCount());
    }

    private User testUser() {
        return User.builder()
                .id(1L)
                .username("tester")
                .email("tester@example.com")
                .password("password")
                .role(Role.USER)
                .build();
    }
}