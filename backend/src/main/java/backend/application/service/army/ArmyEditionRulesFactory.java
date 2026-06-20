package backend.application.service.army;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ArmyEditionRulesFactory {
    private final Map<String, ArmyEditionRules> rulesByEditionCode;

    public ArmyEditionRulesFactory(List<ArmyEditionRules> rules) {
        this.rulesByEditionCode = rules.stream()
                .collect(Collectors.toMap(ArmyEditionRules::getEditionCode, Function.identity()));
    }

    public ArmyEditionRules getRulesForEdition(String editionCode) {
        ArmyEditionRules rules = rulesByEditionCode.get(editionCode);

        if (rules == null) {
            throw new IllegalArgumentException("No army building rules found for edition: " + editionCode);
        }

        return rules;
    }
}