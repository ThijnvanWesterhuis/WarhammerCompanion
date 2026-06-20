package backend.config;

import backend.data.repository.GameEditionRepository;
import backend.data.repository.UnitCatalogRepository;
import backend.domain.GameEdition;
import backend.domain.UnitCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnitCatalogDataInitializer implements CommandLineRunner {

    private final UnitCatalogRepository unitCatalogRepository;
    private final GameEditionRepository gameEditionRepository;

    @Override
    public void run(String... args) {
        GameEdition tenth = findOrCreateEdition(
                "10TH",
                "10th Edition",
                "Current project ruleset for the army builder.",
                10
        );

        GameEdition ninth = findOrCreateEdition(
                "9TH",
                "9th Edition",
                "Legacy project ruleset for the army builder.",
                9
        );

        seedTenthUltramarines(tenth);
        seedTenthTyranids(tenth);
        seedNinthUltramarines(ninth);
        seedNinthTyranids(ninth);
    }

    private GameEdition findOrCreateEdition(String code, String displayName, String description, int releaseOrder) {
        return gameEditionRepository.findByCode(code)
                .orElseGet(() -> gameEditionRepository.save(GameEdition.builder()
                        .code(code)
                        .displayName(displayName)
                        .description(description)
                        .releaseOrder(releaseOrder)
                        .build()));
    }

    private void seedTenthUltramarines(GameEdition edition) {
        seedUnit(edition, "Ultramarines", "Marneus Calgar", "Epic Hero", 200, "3 models", "Character, Infantry");
        seedUnit(edition, "Ultramarines", "Roboute Guilliman", "Epic Hero", 345, "1 model", "Monster, Primarch");
        seedUnit(edition, "Ultramarines", "Chief Librarian Tigurius", "Epic Hero", 75, "1 model", "Character, Psyker");

        seedUnit(edition, "Ultramarines", "Captain", "Character", 80, "1 model", "Character");
        seedUnit(edition, "Ultramarines", "Lieutenant", "Character", 65, "1 model", "Character");

        seedUnit(edition, "Ultramarines", "Intercessor Squad", "Battleline", 80, "5 models", "Infantry");
        seedUnit(edition, "Ultramarines", "Assault Intercessor Squad", "Battleline", 75, "5 models", "Infantry");

        seedUnit(edition, "Ultramarines", "Terminator Squad", "Infantry", 170, "5 models", "Infantry");
        seedUnit(edition, "Ultramarines", "Infernus Squad", "Infantry", 90, "5 models", "Infantry");

        seedUnit(edition, "Ultramarines", "Impulsor", "Dedicated Transport", 80, "1 model", "Vehicle, Transport");
        seedUnit(edition, "Ultramarines", "Redemptor Dreadnought", "Vehicle", 210, "1 model", "Vehicle, Walker");
        seedUnit(edition, "Ultramarines", "Ballistus Dreadnought", "Vehicle", 140, "1 model", "Vehicle, Walker");
        seedUnit(edition, "Ultramarines", "Repulsor Executioner", "Vehicle", 220, "1 model", "Vehicle, Transport");
    }

    private void seedTenthTyranids(GameEdition edition) {
        seedUnit(edition, "Tyranids", "Winged Tyranid Prime", "Character", 65, "1 model", "Character, Fly");
        seedUnit(edition, "Tyranids", "Hive Tyrant", "Monster", 235, "1 model", "Character, Monster");
        seedUnit(edition, "Tyranids", "Neurotyrant", "Character", 105, "1 model", "Character, Synapse");

        seedUnit(edition, "Tyranids", "Termagants", "Battleline", 60, "10 models", "Infantry");
        seedUnit(edition, "Tyranids", "Hormagaunts", "Battleline", 65, "10 models", "Infantry");

        seedUnit(edition, "Tyranids", "Genestealers", "Infantry", 75, "5 models", "Infantry");
        seedUnit(edition, "Tyranids", "Tyranid Warriors with Melee Bio-weapons", "Infantry", 75, "3 models", "Infantry, Synapse");
        seedUnit(edition, "Tyranids", "Zoanthropes", "Infantry", 100, "3 models", "Infantry, Psyker");

        seedUnit(edition, "Tyranids", "Screamer-Killer", "Monster", 145, "1 model", "Monster");
        seedUnit(edition, "Tyranids", "Carnifexes", "Monster", 115, "1 model", "Monster");
        seedUnit(edition, "Tyranids", "Tyrannofex", "Monster", 190, "1 model", "Monster");
        seedUnit(edition, "Tyranids", "Norn Emissary", "Monster", 260, "1 model", "Monster, Synapse");
    }

    private void seedNinthUltramarines(GameEdition edition) {
        seedUnit(edition, "Ultramarines", "Marneus Calgar", "HQ", 180, "1 unit", "Character");
        seedUnit(edition, "Ultramarines", "Roboute Guilliman", "Lord of War", 300, "1 model", "Character, Monster");
        seedUnit(edition, "Ultramarines", "Chief Librarian Tigurius", "HQ", 135, "1 model", "Character, Psyker");

        seedUnit(edition, "Ultramarines", "Primaris Captain", "HQ", 80, "1 model", "Character");
        seedUnit(edition, "Ultramarines", "Primaris Lieutenant", "HQ", 65, "1 model", "Character");

        seedUnit(edition, "Ultramarines", "Intercessor Squad", "Troops", 90, "5 models", "Infantry");
        seedUnit(edition, "Ultramarines", "Assault Intercessor Squad", "Troops", 85, "5 models", "Infantry");

        seedUnit(edition, "Ultramarines", "Terminator Squad", "Elites", 165, "5 models", "Infantry");
        seedUnit(edition, "Ultramarines", "Redemptor Dreadnought", "Elites", 185, "1 model", "Vehicle");

        seedUnit(edition, "Ultramarines", "Outrider Squad", "Fast Attack", 105, "3 models", "Bike");
        seedUnit(edition, "Ultramarines", "Inceptor Squad", "Fast Attack", 120, "3 models", "Infantry, Jump Pack");

        seedUnit(edition, "Ultramarines", "Repulsor Executioner", "Heavy Support", 250, "1 model", "Vehicle");
        seedUnit(edition, "Ultramarines", "Stormtalon Gunship", "Flyer", 165, "1 model", "Aircraft");
    }

    private void seedNinthTyranids(GameEdition edition) {
        seedUnit(edition, "Tyranids", "Hive Tyrant", "HQ", 180, "1 model", "Character, Monster");
        seedUnit(edition, "Tyranids", "Winged Hive Tyrant", "HQ", 210, "1 model", "Character, Monster, Fly");
        seedUnit(edition, "Tyranids", "Neurothrope", "HQ", 100, "1 model", "Character, Psyker");

        seedUnit(edition, "Tyranids", "Termagants", "Troops", 70, "10 models", "Infantry");
        seedUnit(edition, "Tyranids", "Hormagaunts", "Troops", 80, "10 models", "Infantry");
        seedUnit(edition, "Tyranids", "Genestealers", "Troops", 80, "5 models", "Infantry");
        seedUnit(edition, "Tyranids", "Tyranid Warriors", "Troops", 90, "3 models", "Infantry, Synapse");

        seedUnit(edition, "Tyranids", "Zoanthropes", "Elites", 150, "3 models", "Infantry, Psyker");

        seedUnit(edition, "Tyranids", "Gargoyles", "Fast Attack", 80, "10 models", "Infantry, Fly");
        seedUnit(edition, "Tyranids", "Raveners", "Fast Attack", 105, "3 models", "Infantry");

        seedUnit(edition, "Tyranids", "Carnifexes", "Heavy Support", 125, "1 model", "Monster");
        seedUnit(edition, "Tyranids", "Tyrannofex", "Heavy Support", 185, "1 model", "Monster");

        seedUnit(edition, "Tyranids", "Harpy", "Flyer", 170, "1 model", "Aircraft, Monster");
    }

    private void seedUnit(
            GameEdition edition,
            String faction,
            String name,
            String unitType,
            int points,
            String models,
            String keywords
    ) {
        boolean alreadyExists = unitCatalogRepository.existsByGameEditionAndFactionIgnoreCaseAndNameIgnoreCase(
                edition,
                faction,
                name
        );

        if (alreadyExists) {
            return;
        }

        unitCatalogRepository.save(UnitCatalog.builder()
                .gameEdition(edition)
                .faction(faction)
                .name(name)
                .unitType(unitType)
                .points(points)
                .models(models)
                .keywords(keywords)
                .build());
    }
}