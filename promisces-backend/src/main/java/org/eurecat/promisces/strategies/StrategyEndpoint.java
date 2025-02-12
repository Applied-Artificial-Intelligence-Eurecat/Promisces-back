package org.eurecat.promisces.strategies;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/strategies/")
public class StrategyEndpoint {

    @Autowired
    private StrategyService strategyService;

    @GetMapping("/")
    @ResponseBody
    @JsonView(Strategy.class)
    public ResponseEntity<List<Strategy>> getAllStrategies() {
        var sectors = strategyService.getAllSectors();
        if (sectors.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(sectors);
    }

    @PostMapping("/details/")
    @ResponseBody
    @JsonView(Strategy.class)
    public ResponseEntity<Strategy> getStrategyDetails(@RequestBody Map<String, String> input) {
        if (!(input.containsKey("name"))) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Strategy> strategy = strategyService.findSectorByName(input.get("name"));
        if (strategy.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(strategy.get());
    }

    @PostMapping("/related/")
    @ResponseBody
    @JsonView(Strategy.class)
    public ResponseEntity<List<Strategy>> getRelatedStrategies(@RequestBody Map<String, String> input) {
        if (!(input.containsKey("substance_name") || input.containsKey("substance_cas"))) {
            return ResponseEntity.badRequest().build();
        }
        List<Strategy> strategy;
        if (input.containsKey("substance_cas")) {
            strategy = strategyService.findByRelatedSubstanceCas(input.get("substance_cas"));
        } else {
            strategy = strategyService.findByRelatedSubstanceName(input.get("substance_name"));
        }
        if (strategy == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(strategy);
    }

    @PostMapping("/")
    @ResponseBody
    @JsonView(Strategy.class)
    public ResponseEntity<Strategy> createStrategy(@RequestBody Strategy strategy, HttpServletResponse response) {
        var created = strategyService.createStrategy(strategy, response);
        if (created == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(created);
    }

    @PostMapping("/download/")
    @ResponseBody
    @JsonView(Strategy.class)
    public ResponseEntity<Resource> downloadFile(@RequestBody Map<String, String> input,
                                                 HttpServletResponse response) throws IOException {
        if (!input.containsKey("filename")){
            return ResponseEntity.badRequest().build();
        }
        var filename = input.get("filename");

        // TODO D'on podem treure els fitxers?
        File file = new File("Ruta del fitxer");

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename + ".pdf");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");


        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}