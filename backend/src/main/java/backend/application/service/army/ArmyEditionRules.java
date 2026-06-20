package backend.application.service.army;

import backend.api.dto.ArmyListRequestDto;

public interface ArmyEditionRules {
    String getEditionCode();

    void validate(ArmyListRequestDto request, int totalPoints);
}