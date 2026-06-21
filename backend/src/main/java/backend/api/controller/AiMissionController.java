package backend.api.controller;

import backend.api.dto.MissionGenerationRequestDto;
import backend.api.dto.MissionGenerationResponseDto;
import backend.application.service.AiMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiMissionController {
    private final AiMissionService aiMissionService;

    @PostMapping("/mission")
    public MissionGenerationResponseDto generateMission(
            @RequestBody MissionGenerationRequestDto request
    ) {
        return aiMissionService.generateMission(request);
    }
}