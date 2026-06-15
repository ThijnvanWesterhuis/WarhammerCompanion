package backend.integration;

import backend.data.repository.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
class WarhammerApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DiceRollRepository diceRollRepository;

    @Autowired
    private DicePresetRepository dicePresetRepository;

    @Autowired
    private GameSessionRoundScoreRepository roundScoreRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        diceRollRepository.deleteAll();
        dicePresetRepository.deleteAll();
        roundScoreRepository.deleteAll();
        gameSessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerReturnsJwtTokenAndUserInformation() throws Exception {
        String username = uniqueUsername();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "email", username + "@example.com",
                                "password", "Password123!",
                                "confirmPassword", "Password123!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void registerReturnsBadRequestWhenUsernameAlreadyExists() throws Exception {
        String username = uniqueUsername();
        registerAndGetToken(username);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "email", username + "2@example.com",
                                "password", "Password123!",
                                "confirmPassword", "Password123!"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is already in use"));
    }

    @Test
    void loginReturnsUnauthorizedWhenPasswordIsWrong() throws Exception {
        String username = uniqueUsername();
        registerAndGetToken(username);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "password", "WrongPassword123!"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void startingSessionWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/sessions/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "playerOneName", "Thijn",
                                "playerTwoName", "Opponent"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserCanStartUpdateEndAndFindMatchInHistory() throws Exception {
        String token = registerAndGetToken(uniqueUsername());

        MvcResult startResult = mockMvc.perform(post("/sessions/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "playerOneName", "Thijn",
                                "playerTwoName", "Opponent",
                                "playerOneFaction", "Space Marines",
                                "playerTwoFaction", "Orks",
                                "missionName", "Take and Hold",
                                "deploymentMap", "Dawn of War",
                                "notes", "Integration test match"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.currentRound").value(1))
                .andReturn();

        long sessionId = objectMapper
                .readTree(startResult.getResponse().getContentAsString())
                .get("id")
                .asLong();

        mockMvc.perform(patch("/sessions/{sessionId}/score", sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "playerOneScore", 75,
                                "playerTwoScore", 60
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerOneScore").value(75))
                .andExpect(jsonPath("$.playerTwoScore").value(60));

        mockMvc.perform(patch("/sessions/{sessionId}/end", sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.result").value("VICTORY"));

        mockMvc.perform(get("/sessions/history")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "orks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value((int) sessionId))
                .andExpect(jsonPath("$.content[0].result").value("VICTORY"))
                .andExpect(jsonPath("$.content[0].roundScores", hasSize(greaterThanOrEqualTo(1))));
    }

    private String registerAndGetToken(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "email", username + "@example.com",
                                "password", "Password123!",
                                "confirmPassword", "Password123!"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("token").asText();
    }

    private String uniqueUsername() {
        return "user" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}