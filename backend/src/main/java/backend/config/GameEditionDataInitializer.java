package backend.config;

import backend.data.repository.GameEditionRepository;
import backend.domain.GameEdition;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameEditionDataInitializer implements CommandLineRunner {

    private final GameEditionRepository gameEditionRepository;

    @Override
    public void run(String... args) {
        seedEdition(
                "10TH",
                "10th Edition",
                "Current project ruleset for the army builder. Edition-specific logic is isolated in its own strategy class.",
                10
        );

        seedEdition(
                "9TH",
                "9th Edition",
                "Legacy project ruleset for the army builder. This keeps older lists separate from 10th edition lists.",
                9
        );
    }

    private void seedEdition(String code, String displayName, String description, int releaseOrder) {
        if (gameEditionRepository.existsByCode(code)) {
            return;
        }

        gameEditionRepository.save(GameEdition.builder()
                .code(code)
                .displayName(displayName)
                .description(description)
                .releaseOrder(releaseOrder)
                .build());
    }
}