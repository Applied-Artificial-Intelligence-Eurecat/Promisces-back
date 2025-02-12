package org.eurecat.promisces.sectors;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/sectors/")
public class SectorEndpoint {

    @Autowired
    private SectorService sectorService;

    @GetMapping("/")
    @ResponseBody
    @JsonView(SectorEndpoint.class)
    public ResponseEntity<List<SectorOfUse>> getAllSectors() {
        var sectors = sectorService.getAllSectors();
        if (sectors.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(sectors);
    }

    @PostMapping("/details/")
    @ResponseBody
    @JsonView(SectorEndpoint.class)
    public ResponseEntity<SectorOfUse> getSectorDetails(@RequestBody Map<String, String> input) {
        if (!(input.containsKey("name"))) {
            return ResponseEntity.badRequest().build();
        }
        Optional<SectorOfUse> sector = sectorService.findSectorByName(input.get("name"));
        if (sector.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sector.get());
    }

    @PostMapping("/")
    @ResponseBody
    @JsonView(SectorEndpoint.class)
    public ResponseEntity<SectorOfUse> createSectorOfUse(@RequestBody SectorOfUse sector, HttpServletResponse response){
        var created = sectorService.createSector(sector, response);
        if (created == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(created);
    }
}