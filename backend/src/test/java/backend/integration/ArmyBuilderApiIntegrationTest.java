package backend.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
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
class ArmyBuilderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void unitCatalogReturnsEditionSpecificUltramarinesUnits() throws Exception {
        String token = registerAndGetToken(uniqueUsername());

        mockMvc.perform(get("/unit-catalog")
                        .header("Authorization", "Bearer " + token)
                        .param("editionCode", "10TH")
                        .param("faction", "Ultramarines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Captain")))
                .andExpect(jsonPath("$[*].name", hasItem("Intercessor Squad")))
                .andExpect(jsonPath("$[*].unitType", hasItem("Character")))
                .andExpect(jsonPath("$[*].unitType", hasItem("Battleline")));
    }

    @Test
    void authenticatedUserCanCreateDuplicateSearchAndDeleteTenthEditionArmyList() throws Exception {
        String token = registerAndGetToken(uniqueUsername());
        String listName = "E2E Ultramarines " + UUID.randomUUID();

        MvcResult createResult = mockMvc.perform(post("/army-lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(tenthEditionArmyList(listName))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(listName))
                .andExpect(jsonPath("$.gameEdition.code").value("10TH"))
                .andExpect(jsonPath("$.faction").value("Ultramarines"))
                .andExpect(jsonPath("$.armyRule").value("Gladius Task Force"))
                .andExpect(jsonPath("$.pointsLimit").value(500))
                .andExpect(jsonPath("$.totalPoints").value(240))
                .andExpect(jsonPath("$.remainingPoints").value(260))
                .andExpect(jsonPath("$.units", hasSize(2)))
                .andReturn();

        long armyListId = objectMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("id")
                .asLong();

        mockMvc.perform(post("/army-lists/{id}/duplicate", armyListId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(listName + " Copy"))
                .andExpect(jsonPath("$.totalPoints").value(240))
                .andExpect(jsonPath("$.units", hasSize(2)));

        mockMvc.perform(get("/army-lists")
                        .header("Authorization", "Bearer " + token)
                        .param("search", listName)
                        .param("editionCode", "10th")
                        .param("faction", "Ultramarines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", hasItems(listName, listName + " Copy")));

        mockMvc.perform(delete("/army-lists/{id}", armyListId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/army-lists/{id}", armyListId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Army list was not found"));
    }

    @Test
    void ninthEditionArmyListUsesDetachmentSlotValidation() throws Exception {
        String token = registerAndGetToken(uniqueUsername());

        mockMvc.perform(post("/army-lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Invalid 9th Battalion",
                                "gameEditionCode", "9TH",
                                "faction", "Ultramarines",
                                "armyRule", "Battalion Detachment",
                                "pointsLimit", 1000,
                                "description", "Not enough Troops for a Battalion",
                                "units", List.of(
                                        Map.of(
                                                "name", "Primaris Captain",
                                                "unitType", "HQ",
                                                "points", 80,
                                                "quantity", 1,
                                                "notes", "Character"
                                        ),
                                        Map.of(
                                                "name", "Primaris Lieutenant",
                                                "unitType", "HQ",
                                                "points", 65,
                                                "quantity", 1,
                                                "notes", "Character"
                                        ),
                                        Map.of(
                                                "name", "Intercessor Squad",
                                                "unitType", "Troops",
                                                "points", 90,
                                                "quantity", 1,
                                                "notes", "Infantry"
                                        )
                                )
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Troops slot count must be between 3 and 6 for the selected 9th edition detachment"
                ));
    }

    @Test
    void userCannotReadAnotherUsersArmyList() throws Exception {
        String firstUserToken = registerAndGetToken(uniqueUsername());
        String secondUserToken = registerAndGetToken(uniqueUsername());

        MvcResult createResult = mockMvc.perform(post("/army-lists")
                        .header("Authorization", "Bearer " + firstUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(tenthEditionArmyList("Private Army List " + UUID.randomUUID()))))
                .andExpect(status().isOk())
                .andReturn();

        long armyListId = objectMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("id")
                .asLong();

        mockMvc.perform(get("/army-lists/{id}", armyListId)
                        .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Army list was not found"));
    }

    private Map<String, Object> tenthEditionArmyList(String name) {
        return Map.of(
                "name", name,
                "gameEditionCode", "10TH",
                "faction", "Ultramarines",
                "armyRule", "Gladius Task Force",
                "pointsLimit", 500,
                "description", "Created by integration test",
                "units", List.of(
                        Map.of(
                                "name", "Captain",
                                "unitType", "Character",
                                "points", 80,
                                "quantity", 1,
                                "notes", "Character"
                        ),
                        Map.of(
                                "name", "Intercessor Squad",
                                "unitType", "Battleline",
                                "points", 80,
                                "quantity", 2,
                                "notes", "Infantry"
                        )
                )
        );
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
        return "army" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}