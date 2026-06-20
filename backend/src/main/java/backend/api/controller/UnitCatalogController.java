package backend.api.controller;

import backend.api.dto.UnitCatalogResponseDto;
import backend.application.service.UnitCatalogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unit-catalog")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UnitCatalogController {

    private final UnitCatalogService unitCatalogService;

    @GetMapping
    public ResponseEntity<List<UnitCatalogResponseDto>> getUnits(
            @RequestParam String editionCode,
            @RequestParam String faction
    ) {
        return ResponseEntity.ok(unitCatalogService.getUnits(editionCode, faction));
    }
}