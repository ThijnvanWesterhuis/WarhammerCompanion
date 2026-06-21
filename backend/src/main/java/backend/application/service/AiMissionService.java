package backend.application.service;

import backend.api.dto.MissionGenerationRequestDto;
import backend.api.dto.MissionGenerationResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
public class AiMissionService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4.1-mini}")
    private String model;

    public MissionGenerationResponseDto generateMission(MissionGenerationRequestDto request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "OpenAI API key is not configured."
            );
        }

        try {
            String prompt = buildPrompt(request);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "input", prompt
            );

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(
                        BAD_GATEWAY,
                        "AI mission generation failed."
                );
            }

            String aiText = extractText(response.body());
            return parseMission(aiText);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    BAD_GATEWAY,
                    "Could not generate AI mission.",
                    exception
            );
        }
    }

    private String buildPrompt(MissionGenerationRequestDto request) {
        String playerOneFaction = emptyToUnknown(request.playerOneFaction());
        String playerTwoFaction = emptyToUnknown(request.playerTwoFaction());
        String notes = emptyToNone(request.notes());

        return """
                Generate a simple custom Warhammer 40K mission for my companion app.

                Player one faction: %s
                Player two faction: %s
                Extra notes from the user: %s

                Requirements:
                - Keep it short and usable during a real game.
                - Do not copy official Games Workshop mission text.
                - Do not create complicated rules.
                - Return only valid JSON.
                - Use this exact JSON structure:

                {
                  "missionName": "short mission name",
                  "deploymentMap": "short deployment map name",
                  "missionBriefing": "2 or 3 short sentences explaining the custom mission"
                }
                """.formatted(playerOneFaction, playerTwoFaction, notes);
    }

    private String extractText(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode output = root.path("output");

        for (JsonNode outputItem : output) {
            JsonNode content = outputItem.path("content");

            for (JsonNode contentItem : content) {
                JsonNode text = contentItem.path("text");

                if (!text.isMissingNode() && !text.asText().isBlank()) {
                    return text.asText();
                }
            }
        }

        throw new IllegalStateException("No text returned by AI.");
    }

    private MissionGenerationResponseDto parseMission(String aiText) throws Exception {
        String json = extractJsonObject(aiText);
        JsonNode root = objectMapper.readTree(json);

        return new MissionGenerationResponseDto(
                root.path("missionName").asText("AI Mission"),
                root.path("deploymentMap").asText("Standard Deployment"),
                root.path("missionBriefing").asText("No briefing generated.")
        );
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start == -1 || end == -1 || end <= start) {
            throw new IllegalArgumentException("AI response did not contain JSON.");
        }

        return text.substring(start, end + 1);
    }

    private String emptyToUnknown(String value) {
        return value == null || value.isBlank() ? "Unknown" : value.trim();
    }

    private String emptyToNone(String value) {
        return value == null || value.isBlank() ? "None" : value.trim();
    }
}