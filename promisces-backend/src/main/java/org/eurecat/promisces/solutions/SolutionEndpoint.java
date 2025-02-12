package org.eurecat.promisces.solutions;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletResponse;
import org.eurecat.promisces.tools.ReturnResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/solutions/")
public class SolutionEndpoint {

    @Autowired
    private SolutionService solutionService;

    @GetMapping("/")
    @ResponseBody
    @JsonView(Solution.class)
    public ResponseEntity<ReturnResult<List<Solution>>> getAllSolutions(@RequestParam("pagenum") Integer pageNumber, @RequestParam("perpage") Integer registersPerPage) {
        var solutions = solutionService.getAllSolutionsPaged(pageNumber, registersPerPage);
        if (solutions == null){
            return ResponseEntity.badRequest().build();
        }
        if (solutions.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(solutions);
    }

    @PostMapping("/details/")
    @ResponseBody
    @JsonView(Solution.class)
    public ResponseEntity<Solution> getSolutionDetails(@RequestBody Map<String, String> input) {
        if (!(input.containsKey("name"))) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Solution> solution = solutionService.findSolutionByName(input.get("name"));
        if (solution.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(solution.get());
    }

    @PostMapping("/")
    @ResponseBody
    @JsonView(Solution.class)
    public ResponseEntity<Solution> createSolution(@RequestBody Solution solution, HttpServletResponse response) {
        var created = solutionService.createSolution(solution, response);
        if (created == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(created);
    }

    @PostMapping("/link/{recipient}/")
    @ResponseBody
    @JsonView(Solution.class)
    public ResponseEntity<Solution> linkSubstance(@PathVariable("recipient") String recipient, @RequestBody Map<String, String> input, HttpServletResponse response) {
        if (recipient.equals("routes")) {
            if (!(input.containsKey("solution_name"))) {
                response.setHeader("error", "solution_name is missing");
                return ResponseEntity.badRequest().build();
            }
            if (!input.containsKey("route_name")) {
                response.setHeader("error", "route_name is missing");
                return ResponseEntity.badRequest().build();
            }
            String solutionName = input.get("solution_name");
            String routeName = input.get("route_name");
            Solution result = solutionService.linkSolutionByNameToRoute(solutionName, routeName, response);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } else if (recipient.equals("strategies")) {
            if (!(input.containsKey("solution_name"))) {
                response.setHeader("error", "solution_name is missing");
                return ResponseEntity.badRequest().build();
            }
            if (!input.containsKey("strategy_name")) {
                response.setHeader("error", "strategy_name is missing");
                return ResponseEntity.badRequest().build();
            }
            String solutionName = input.get("solution_name");
            String strategyName = input.get("strategy_name");
            Solution result = solutionService.linkSolutionByNameToStrategy(solutionName, strategyName, response);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } else {
            response.setHeader("error", recipient + " is not a valid recipient, valid options are \"routes\" and \"strategies\"");
            return ResponseEntity.badRequest().build();
        }
    }
}
