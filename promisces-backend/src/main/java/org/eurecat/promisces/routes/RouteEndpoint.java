package org.eurecat.promisces.routes;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/routes/")
public class RouteEndpoint {

    @Autowired
    private RouteService routeService;

    @GetMapping("/")
    @ResponseBody
    @JsonView(RouteEndpoint.class)
    public ResponseEntity<List<CERoute>> getAllSectors() {
        var sectors = routeService.getAllSectors();
        if (sectors.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(sectors);
    }

    @PostMapping("/details/")
    @ResponseBody
    @JsonView(RouteEndpoint.class)
    public ResponseEntity<CERoute> getSectorDetails(@RequestBody Map<String, String> input) {
        if (!(input.containsKey("name"))) {
            return ResponseEntity.badRequest().build();
        }
        Optional<CERoute> sector = routeService.findSectorByName(input.get("name"));
        if (sector.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sector.get());
    }

    @PostMapping("/")
    @ResponseBody
    @JsonView(RouteEndpoint.class)
    public ResponseEntity<CERoute> createCERoute(@RequestBody CERoute route, HttpServletResponse response){
        var created = routeService.createRoute(route, response);
        if (created == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(created);
    }
}