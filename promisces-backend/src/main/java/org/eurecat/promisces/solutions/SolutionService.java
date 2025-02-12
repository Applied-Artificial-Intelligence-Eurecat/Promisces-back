package org.eurecat.promisces.solutions;

import jakarta.servlet.http.HttpServletResponse;
import org.eurecat.promisces.routes.CERoute;
import org.eurecat.promisces.routes.RouteRepository;
import org.eurecat.promisces.solutions.criteria.DegradationCriteria;
import org.eurecat.promisces.solutions.criteria.RecoveryCriteria;
import org.eurecat.promisces.solutions.criteria.SolutionCriteriaRepository;
import org.eurecat.promisces.strategies.Strategy;
import org.eurecat.promisces.strategies.StrategyRepository;
import org.eurecat.promisces.tools.ReturnResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SolutionService {

    @Autowired
    private SolutionRepository solutionRepository;
    @Autowired
    private SolutionCriteriaRepository solutionCriteriaRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private StrategyRepository strategyRepository;


    public List<Solution> getAllSolutions() {
        List<Solution> items = new LinkedList<>();
        solutionRepository.findAll().forEach(items::add);
        return items;
    }

    public ReturnResult<List<Solution>> getAllSolutionsPaged(Integer pageNum, Integer registers) {
        List<Solution> items = new LinkedList<>();
        solutionRepository.findAll().forEach(items::add);
        ReturnResult<List<Solution>> ret = new ReturnResult<>();
        ret.setTotalRegisters(items.size());
        try {
            ret.setRegistersThisPage(registers);
            ret.setData(items.subList(registers*(pageNum-1), registers*pageNum));
        } catch (IndexOutOfBoundsException e){
            try {
                ret.setData(items.subList(registers * (pageNum - 1), items.size()));
                ret.setRegistersThisPage(ret.getData().size());
            } catch (IndexOutOfBoundsException ex) {
                return null;
            }
        }
        return ret;
    }

    public Optional<Solution> findSolutionByName(String name) {
        return solutionRepository.findByName(name);
    }

    public Solution createSolution(Solution solution, HttpServletResponse response) {
        if (solution.getType() == null) {
            response.setHeader("error", "A solution must have a type");
            return null;
        }
        if (solution.getCriteria() instanceof DegradationCriteria) {
            solution.setCriteria(solutionCriteriaRepository.save((DegradationCriteria) solution.getCriteria()));
        } else if (solution.getCriteria() instanceof RecoveryCriteria) {
            solution.setCriteria(solutionCriteriaRepository.save((RecoveryCriteria) solution.getCriteria()));
        } else {
            response.setHeader("error", "Type not valid. Must be \"recovery\" or \"degradation\"");
            return null;
        }
        if (solutionRepository.findByName(solution.getName()).isPresent()) {
            response.setHeader("error", "A solution with this name already exists");
            return null;
        }
        if (solution.getType() == null || Objects.equals(solution.getName(), "")) {
            response.setHeader("error", "Can't create a solution without name");
            return null;
        }
        // solution.setCriteria(null);
        return solutionRepository.save(solution);
    }

    public Solution linkSolutionByNameToRoute(String solutionName, String routeName, HttpServletResponse response) {
        Optional<Solution> optionalSolution = solutionRepository.findByName(solutionName);
        if (optionalSolution.isEmpty()) {
            response.setHeader("error", "Solution with this name not found");
            return null;
        }
        Optional<CERoute> optionalRoute = routeRepository.findByName(routeName);
        if (optionalRoute.isEmpty()) {
            response.setHeader("error", "Route with this name not found");
            return null;
        }
        //return linkSolutionToRoute(optionalSolution.get(), optionalRoute.get());
        return null;
    }

    /*
    private Solution linkSolutionToRoute(Solution solution, CERoute ceRoute) {
        if (solution.getCeRoute() != null && solution.getCeRoute().equals(ceRoute)){
            return solution;
        }
        solution.setCeRoute(ceRoute);
        solutionRepository.save(solution);
        Set<Solution> solutions = ceRoute.getRelatedSolutions();
        solutions.add(solution);
        ceRoute.setRelatedSolutions(solutions);
        routeRepository.save(ceRoute);
        return solution;
    }
    */

    public Solution linkSolutionByNameToStrategy(String solutionName, String strategyName, HttpServletResponse response) {
        Optional<Solution> optionalSolution = solutionRepository.findByName(solutionName);
        if (optionalSolution.isEmpty()) {
            response.setHeader("error", "Solution with this name not found");
            return null;
        }
        Optional<Strategy> optionalStrategy = strategyRepository.findByName(strategyName);
        if (optionalStrategy.isEmpty()) {
            response.setHeader("error", "Strategy with this name not found");
            return null;
        }
        return linkSolutionToStrategy(optionalSolution.get(), optionalStrategy.get());
    }

    private Solution linkSolutionToStrategy(Solution solution, Strategy strategy) {
        if (solution.getRelatedStrategies().contains(strategy)){
            return solution;
        }
        Set<Strategy> strategies = solution.getRelatedStrategies();
        strategies.add(strategy);
        solution.setRelatedStrategies(strategies);
        solutionRepository.save(solution);
        Set<Solution> solutions = strategy.getRelatedSolutions();
        solutions.add(solution);
        strategy.setRelatedSolutions(solutions);
        strategyRepository.save(strategy);
        return solution;
    }
}
