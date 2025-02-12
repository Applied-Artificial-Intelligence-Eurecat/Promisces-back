package org.eurecat.promisces.strategies;

import jakarta.servlet.http.HttpServletResponse;
import org.eurecat.promisces.solutions.Solution;
import org.eurecat.promisces.substances.Substance;
import org.eurecat.promisces.substances.SubstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class StrategyService {

    @Autowired
    private StrategyRepository strategyRepository;
    @Autowired
    private SubstanceRepository substanceRepository;

    public List<Strategy> getAllSectors() {
        List<Strategy> sectors = new LinkedList<>();
        strategyRepository.findAll().forEach(sectors::add);
        return sectors;
    }

    public Optional<Strategy> findSectorByName(String name) {
        return strategyRepository.findByName(name);
    }

    public Strategy createStrategy(Strategy strategy, HttpServletResponse response) {
        if (strategyRepository.findByName(strategy.getName()).isPresent()) {
            response.setHeader("error", "A strategy with this name already exists");
            return null;
        }
        if (strategy.getName() == null || Objects.equals(strategy.getName(), "")) {
            response.setHeader("error", "Can't create a strategy without name");
        }
        return strategyRepository.save(strategy);
    }

    public List<Strategy> findByRelatedSubstanceCas(String substanceCas) {
        Optional<Substance> substance = substanceRepository.findByCasNumber(substanceCas);
        if (substance.isEmpty()) {
            return null;
        }
        return findByRelatedSubstance(substance.get());
    }

    public List<Strategy> findByRelatedSubstanceName(String substanceName) {
        for (Substance substance : substanceRepository.findAll()) {
            if (substance.getNameSynonyms().contains(substanceName)) {
                return findByRelatedSubstance(substance);
            }
        }
        return null;
    }

    private List<Strategy> findByRelatedSubstance(Substance substance) {
        List<Strategy> result = new LinkedList<>();
        for (Solution solution : substance.getRelatedSolutions()){
            for (Strategy strategy : solution.getRelatedStrategies()) {
                if (!result.contains(strategy)){
                    result.add(strategy);
                }
            }
        }
        return result;
    }
}
