package org.eurecat.promisces.substances;

import jakarta.servlet.http.HttpServletResponse;
import org.eurecat.promisces.groups.PriorityGroupRepository;
import org.eurecat.promisces.routes.CERoute;
import org.eurecat.promisces.routes.RouteRepository;
import org.eurecat.promisces.sectors.SectorOfUse;
import org.eurecat.promisces.sectors.SectorRepository;
import org.eurecat.promisces.solutions.Solution;
import org.eurecat.promisces.solutions.SolutionRepository;
import org.eurecat.promisces.tools.ReturnResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SubstanceService {

    @Autowired
    private SubstanceRepository substanceRepository;
    @Autowired
    private SolutionRepository solutionRepository;
    @Autowired
    private SectorRepository sectorRepository;
    @Autowired
    private PriorityGroupRepository priorityGroupRepository;
    @Autowired
    private RouteRepository routeRepository;

    @Transactional
    public ReturnResult<List<Substance>> getAllSubstances(Integer pageNumber, Integer registers) {
        ReturnResult<List<Substance>> ret = new ReturnResult<>();

        // Fetch substances lazily using a stream
        try (Stream<Substance> substanceStream = substanceRepository.findByIdGreaterThanOrderByIdAsc(0L)) {
            List<Substance> pageData = substanceStream
                    .skip((long) registers * (pageNumber - 1)) // Skip previous pages
                    .limit(registers) // Limit to the current page size
                    .collect(Collectors.toList());

            // Set paginated data into the return result
            ret.setData(pageData);
            ret.setRegistersThisPage(pageData.size());
            ret.setTotalRegisters((int) substanceRepository.count()); // Lazy count from the repository
        } catch (Exception e) {
            e.printStackTrace(); // Log or handle the exception
            return null; // Return null if an exception occurs
        }

        return ret;
    }

    @Transactional
    public Stream<Substance> getAllSubstances() {
        return substanceRepository.findByIdGreaterThanOrderByIdAsc(0L);
    }

    @Transactional
    public Optional<Substance> findSubstanceByCASNumber(String cas_n) {
        return substanceRepository.findByCasNumber(cas_n);
    }

    @Transactional
    public Optional<Substance> findSubstanceByInchikey(String key) {
        return substanceRepository.findByInchikey(key);
    }

    @Transactional
    public Optional<Substance> findSubstanceByName(String name) {
        var result = findByNameSynonymsContains(name);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    @Transactional
    public List<Substance> findByNameSynonymsContains(String name) {
        List<Substance> substances = new LinkedList<>();

        substanceRepository.findByIdGreaterThanOrderByIdAsc(0L).forEach(e -> {
            if (e.getNameSynonyms().contains(name)) {
                substances.add(e);
            }
        });
        return substances;
    }

    @Transactional
    public List<Substance> getSubstanceSearchResults(String substanceName,
                                                     String substanceCas,
                                                     String typeOfSolution,
                                                     String solutionName,
                                                     String substanceGroup,
                                                     String ceRoute,
                                                     String chemicalClass,
                                                     String sectorOfUse,
                                                     String followedStrategy,
                                                     String inchikey,
                                                     Boolean experimental,
                                                     Boolean predicted) {
        Stream<Substance> substances = substanceRepository.findByIdGreaterThanOrderByIdAsc(0L);
        List<Substance> retval = new LinkedList<>();
        substances.forEach(s -> {
            boolean add = true;
            if (!Objects.equals(substanceName, "")) {
                // If any synonym contains the passed name
                if (!s.getNameSynonyms().stream()
                        .map(n -> n.toLowerCase().contains(substanceName.toLowerCase()))
                        .reduce(false, Boolean::logicalOr)) {
                    add = false;
                }
            }
            if (!Objects.equals(substanceCas, "")) {
                // If cas number does not contain the cas
                if (!s.getCasNumber().contains(substanceCas)) {
                    add = false;
                }
            }
            if (!Objects.equals(typeOfSolution, "")) {
                // If any solution has the related type of solution
                if (!(s.getRelatedSolutions().stream()
                        .map(sol -> sol.getType().equalsIgnoreCase(typeOfSolution))
                        .reduce(false, Boolean::logicalOr))) {
                    add = false;
                }
            }
            if (!Objects.equals(solutionName, "")) {
                // If any solution has the related name
                if (!(s.getRelatedSolutions().stream()
                        .map(sol -> sol.getName().contains(solutionName))
                        .reduce(false, Boolean::logicalOr))) {
                    add = false;
                }
            }
            if (!Objects.equals(substanceGroup, "")) {
                if (!s.getGroup().getGroupName().contains(substanceGroup)) {
                    add = false;
                }
            }
            if (!Objects.equals(ceRoute, "")) {
                if (!s.getCeRoutes().stream().map(CERoute::getName).toList().contains(ceRoute)) {
                    add = false;
                }
            }
            if (!Objects.equals(chemicalClass, "")) {
                if (!s.getChemicalClasses().contains(chemicalClass)) {
                    add = false;
                }
            }
            if (!Objects.equals(sectorOfUse, "")) {
                if (!(s.getSectorsOfUse().stream()
                        .map(sect -> sect.getCode().equalsIgnoreCase(sectorOfUse))
                        .reduce(false, Boolean::logicalOr))) {
                    add = false;
                }
            }
            if (!Objects.equals(followedStrategy, "")) {
                if (!(s.getRelatedSolutions().stream()
                        .map(sol -> sol.getRelatedStrategies().stream()
                                .map(st -> st.getName().contains(followedStrategy))
                                .reduce(false, Boolean::logicalOr))
                        .reduce(false, Boolean::logicalOr))) {
                    add = false;
                }
            }
            if (!Objects.equals(inchikey, "")) {
                if (!s.getInchikey().contains(inchikey)) {
                    add = false;
                }
            }
            if (experimental != predicted) {
                if (predicted) {
                    if (s.getNumericalData().keySet().stream()
                            .map(key -> key.contains("robust"))
                            .reduce(false, Boolean::logicalOr)) {
                        // Contains some exp
                        add = false;
                    }
                }
                if (experimental) {
                    if (s.getNumericalData().keySet().stream()
                            .map(key -> key.contains("conservative"))
                            .reduce(false, Boolean::logicalOr)) {
                        // Contains some pred
                        add = false;
                    }
                }
            }
            if (add) {
                retval.add(s);
            }
        });
        return retval;
    }

    @Transactional
    public Substance createSubstance(Substance substance, HttpServletResponse response) {
        if (Objects.equals(substance.getCasNumber(), "")) {
            response.setHeader("error", "Can't create a substance without CAS number");
            return null;
        }
        /*
        Optional<Substance> query = substanceRepository.findByCasNumber(substance.getCasNumber());
        if (query.isPresent()) {
            response.setHeader("error", "A substance with this CAS number already exists");
            return null;
        }
         */
        if (substance.getNameSynonyms() == null || substance.getNameSynonyms().stream()
                .map(String::isEmpty)
                .reduce(false, Boolean::logicalOr)) {
            response.setHeader("error", "Can't create a substance without at least one name");
            return null;
        }

        var group = priorityGroupRepository.findByGroupName(substance.getGroup().getGroupName());
        if (group.isEmpty()) {
            priorityGroupRepository.save(substance.getGroup());
        } else {
            substance.setGroup(group.get());
            priorityGroupRepository.save(substance.getGroup());
        }

        var routes = new ArrayList<>(substance.getCeRoutes().stream().toList());
        for (int i = 0; i < substance.getCeRoutes().size(); i++) {
            var route = routes.get(i);
            var r = routeRepository.findByName(route.getName());
            if (r.isPresent()) {
                routes.set(i, r.get());
            }
        }
        routeRepository.saveAll(routes);
        substance.setCeRoutes(new HashSet<>(routes));

        var sectors = new ArrayList<>(substance.getSectorsOfUse().stream().toList());
        for (int i = 0; i < sectors.size(); i++) {
            var sector = sectors.get(i);
            var s = sectorRepository.findByCode(sector.getCode());
            if (s.isPresent()) {
                sectors.set(i, s.get());
            }
        }
        sectorRepository.saveAll(sectors);
        substance.setSectorsOfUse(new HashSet<>(sectors));

        return substanceRepository.save(substance);
    }

    @Transactional
    public Substance updateSubstance(Long id, Substance substance) {
        substance.setId(id);

        var group = priorityGroupRepository.findByGroupName(substance.getGroup().getGroupName());
        if (group.isEmpty()) {
            priorityGroupRepository.save(substance.getGroup());
        } else {
            substance.setGroup(group.get());
            priorityGroupRepository.save(substance.getGroup());
        }

        var routes = new ArrayList<>(substance.getCeRoutes().stream().toList());
        for (int i = 0; i < substance.getCeRoutes().size(); i++) {
            var route = routes.get(i);
            var r = routeRepository.findByName(route.getName());
            if (r.isPresent()) {
                routes.set(i, r.get());
            }
        }
        routeRepository.saveAll(routes);
        substance.setCeRoutes(new HashSet<>(routes));

        var sectors = new ArrayList<>(substance.getSectorsOfUse().stream().toList());
        for (int i = 0; i < sectors.size(); i++) {
            var sector = sectors.get(i);
            var s = sectorRepository.findByCode(sector.getCode());
            if (s.isPresent()) {
                sectors.set(i, s.get());
            }
        }
        sectorRepository.saveAll(sectors);
        substance.setSectorsOfUse(new HashSet<>(sectors));

        return substanceRepository.save(substance);
    }

    @Transactional
    public Substance linkSubstanceByCasToSolution(String casNumber, String solutionName, HttpServletResponse
            response) {
        Optional<Substance> optionalSubstance = substanceRepository.findByCasNumber(casNumber);
        if (optionalSubstance.isEmpty()) {
            response.setHeader("error", "Substance with this CAS number not found");
            return null;
        }
        Optional<Solution> optionalSolution = solutionRepository.findByName(solutionName);
        if (optionalSolution.isEmpty()) {
            response.setHeader("error", "Solution with this name not found");
            return null;
        }
        return linkSubstanceToSolution(optionalSubstance.get(), optionalSolution.get());
    }

    @Transactional
    public Substance linkSubstanceToSolution(Substance substance, Solution solution) {
        if (substance.getRelatedSolutions().contains(solution)) {
            return substance;
        }
        Set<Solution> solutions = substance.getRelatedSolutions();
        solutions.add(solution);
        substance.setRelatedSolutions(solutions);
        substanceRepository.save(substance);
        Set<Substance> substances = solution.getRelatedSubstances();
        substances.add(substance);
        solution.setRelatedSubstances(substances);
        solutionRepository.save(solution);
        return substance;
    }

    @Transactional
    public Substance linkSubstanceByNameToSolution(String substanceName, String solutionName, HttpServletResponse
            response) {
        Iterator<Substance> substanceList = substanceRepository.findAll().iterator();
        Substance substance = null;
        while (substanceList.hasNext()) {
            Substance listValue = substanceList.next();
            if (listValue.getNameSynonyms().contains(substanceName)) {
                substance = listValue;
            }
        }
        if (substance == null) {
            response.setHeader("error", "Substance with this name not found");
            return null;
        }
        Optional<Solution> optionalSolution = solutionRepository.findByName(solutionName);
        if (optionalSolution.isEmpty()) {
            response.setHeader("error", "Solution with this name not found");
            return null;
        }
        return linkSubstanceToSolution(substance, optionalSolution.get());
    }

    @Transactional
    public Substance linkSubstanceByCasToSector(String casNumber, String sectorName, HttpServletResponse response) {
        Optional<Substance> optionalSubstance = substanceRepository.findByCasNumber(casNumber);
        if (optionalSubstance.isEmpty()) {
            response.setHeader("error", "Substance with this CAS number not found");
            return null;
        }
        Optional<SectorOfUse> optionalSector = sectorRepository.findByName(sectorName);
        if (optionalSector.isEmpty()) {
            response.setHeader("error", "Sector with this name not found");
            return null;
        }
        return linkSubstanceToSector(optionalSubstance.get(), optionalSector.get());
    }

    @Transactional
    public Substance linkSubstanceByNameToSector(String substanceName, String sectorName, HttpServletResponse
            response) {
        Iterator<Substance> substanceList = substanceRepository.findAll().iterator();
        Substance substance = null;
        while (substanceList.hasNext()) {
            Substance listValue = substanceList.next();
            if (listValue.getNameSynonyms().contains(substanceName)) {
                substance = listValue;
            }
        }
        if (substance == null) {
            response.setHeader("error", "Substance with this name not found");
            return null;
        }
        Optional<SectorOfUse> optionalSector = sectorRepository.findByName(sectorName);
        if (optionalSector.isEmpty()) {
            response.setHeader("error", "Sector with this name not found");
            return null;
        }
        return linkSubstanceToSector(substance, optionalSector.get());
    }

    @Transactional
    public Substance linkSubstanceToSector(Substance substance, SectorOfUse sectorOfUse) {
        if (substance.getSectorsOfUse().contains(sectorOfUse)) {
            return substance;
        }
        Set<SectorOfUse> sectors = substance.getSectorsOfUse();
        sectors.add(sectorOfUse);
        substance.setSectorsOfUse(sectors);
        substanceRepository.save(substance);
        Set<Substance> substances = sectorOfUse.getRelatedSubstances();
        substances.add(substance);
        sectorOfUse.setRelatedSubstances(substances);
        sectorRepository.save(sectorOfUse);
        return substance;
    }

    @Transactional
    public BigDecimal getMaxAttribute(String attribute) {
        BigDecimal max = new BigDecimal(0);
        List<BigDecimal> values = new LinkedList<>();
        switch (attribute) {
            case "persistence":
                substanceRepository.findByIdGreaterThanOrderByIdAsc(0L).forEach(s -> {
                    values.add(s.getPersistence());
                });
                for (BigDecimal value : values) {
                    if (value == null) {
                        continue;
                    }
                    if (value.compareTo(max) > 0) {
                        max = value;
                    }
                }
                return max;
            case "mobility":
                substanceRepository.findByIdGreaterThanOrderByIdAsc(0L).forEach(s -> {
                    values.add(s.getMobility());
                });
                for (BigDecimal value : values) {
                    if (value == null) {
                        continue;
                    }
                    if (value.compareTo(max) > 0) {
                        max = value;
                    }
                }
                return max;
            case "toxicity":
                substanceRepository.findByIdGreaterThanOrderByIdAsc(0L).forEach(s -> {
                    values.add(s.getToxicity());
                });
                for (BigDecimal value : values) {
                    if (value == null) {
                        continue;
                    }
                    if (value.compareTo(max) > 0) {
                        max = value;
                    }
                }
                return max;
            default:
                substanceRepository.findByIdGreaterThanOrderByIdAsc(0L).forEach(s -> {
                    values.add(s.getNumericalData().getOrDefault(attribute, BigDecimal.valueOf(0)));
                });
                for (BigDecimal value : values) {
                    if (value == null) {
                        continue;
                    }
                    if (value.compareTo(max) > 0) {
                        max = value;
                    }
                }
                return max;
        }
    }

    @Transactional
    public BigDecimal calculateSubstanceImportance(Substance substance,
                                                   BigDecimal maxPersistence,
                                                   BigDecimal maxToxicity,
                                                   BigDecimal maxMobility,
                                                   BigDecimal persistenceCoefficient,
                                                   BigDecimal mobilityCoefficient,
                                                   BigDecimal toxicityCoefficient) {
        BigDecimal P_normalised = BigDecimal.valueOf(Float.parseFloat(substance.getPersistence().toPlainString()) /
                Float.parseFloat(maxPersistence.toPlainString()));
        BigDecimal M_normalised = BigDecimal.valueOf(Float.parseFloat(substance.getMobility().toPlainString()) /
                Float.parseFloat(maxMobility.toPlainString()));
        BigDecimal T_normalised = BigDecimal.valueOf(Float.parseFloat(substance.getToxicity().toPlainString()) /
                Float.parseFloat(maxToxicity.toPlainString()));
        return P_normalised.multiply(persistenceCoefficient)
                .add(M_normalised.multiply(mobilityCoefficient))
                .add(T_normalised.multiply(toxicityCoefficient));
    }

    @Transactional
    public List<Substance> normaliseListOfSubstances(List<Substance> substances) {
        BigDecimal maxPersistence = this.getMaxAttribute("persistence");
        BigDecimal maxMobility = this.getMaxAttribute("mobility");
        BigDecimal maxToxicity = this.getMaxAttribute("toxicity");
        if (maxPersistence.equals(BigDecimal.ZERO) ||
                maxMobility.equals(BigDecimal.ZERO) ||
                maxToxicity.equals(BigDecimal.ZERO)) {
            return substances;
        }
        for (Substance sub : substances) {
            BigDecimal P_normalised = BigDecimal.valueOf(Float.parseFloat(sub.getPersistence().toPlainString()) /
                    Float.parseFloat(maxPersistence.toPlainString()));
            BigDecimal M_normalised = BigDecimal.valueOf(Float.parseFloat(sub.getMobility().toPlainString()) /
                    Float.parseFloat(maxMobility.toPlainString()));
            BigDecimal T_normalised = BigDecimal.valueOf(Float.parseFloat(sub.getToxicity().toPlainString()) /
                    Float.parseFloat(maxToxicity.toPlainString()));
            sub.setPersistence(P_normalised);
            sub.setMobility(M_normalised);
            sub.setToxicity(T_normalised);
        }
        return substances;
    }

    @Transactional
    public List<String> getAutocompleteOptions(String substanceName) {
        List<String> names = new LinkedList<>();
        substanceRepository.findByIdGreaterThanOrderByIdAsc(0L).forEach(s -> {
            s.getNameSynonyms().forEach(n -> {
                if (n.toLowerCase().startsWith(substanceName.toLowerCase()) && !names.contains(n)) {
                    names.add(n);
                }
            });
        });
        substanceRepository.findByIdGreaterThanOrderByIdAsc(0L).forEach(s -> {
            s.getNameSynonyms().forEach(n -> {
                if (n.toLowerCase().contains(substanceName.toLowerCase()) && !names.contains(n)) {
                    names.add(n);
                }
            });
        });
        return names;
    }

    @Transactional
    public List<String> getAutocompleteOptionsCAS(String substanceName) {
        List<String> names = new LinkedList<>();
        substanceRepository.findByIdGreaterThanOrderByIdAsc(0L).forEach(s -> {
            if (s.getCasNumber().toLowerCase().startsWith(substanceName.toLowerCase()) && !names.contains(s.getCasNumber())) {
                names.add(s.getCasNumber());
            }
        });
        substanceRepository.findByIdGreaterThanOrderByIdAsc(0L).forEach(s -> {
            if (s.getCasNumber().toLowerCase().contains(substanceName.toLowerCase()) && !names.contains(s.getCasNumber())) {
                names.add(s.getCasNumber());
            }
        });
        return names;
    }

    @Transactional
    public List<String> getAutocompleteOptionsInchi(String substanceName) {
        List<String> names = new LinkedList<>();
        substanceRepository.findByIdGreaterThanOrderByIdAsc(0L).forEach(s -> {
            if (s.getInchikey().toLowerCase().startsWith(substanceName.toLowerCase()) && !names.contains(s.getInchikey())) {
                names.add(s.getInchikey());
            }
        });
        substanceRepository.findByIdGreaterThanOrderByIdAsc(0L).forEach(s -> {
            if (s.getInchikey().toLowerCase().contains(substanceName.toLowerCase()) && !names.contains(s.getInchikey())) {
                names.add(s.getInchikey());
            }
        });
        return names;
    }

    @Transactional
    public List<Substance> getAllSubstancesFiltered(String filterBy, List<String> acceptedFilterValues) {
        return switch (filterBy) {
            case "chemicalClasses" -> getSubstancesByChemicalClass(acceptedFilterValues);
            case "sectorsOfUse" -> getSubstancesBySectorsOfUseName(acceptedFilterValues);
            default -> new LinkedList<>();
        };
    }

    @Transactional
    public List<Substance> getSubstancesBySectorsOfUseName(List<String> names) {
        List<Substance> substances = new LinkedList<>();
        names.forEach(n -> {
            List<Substance> result = substanceRepository.findBySectorsOfUse_NameContainsIgnoreCase(n);
            result.forEach(s -> {
                if (!substances.stream().map(Substance::getCasNumber).toList().contains(s.getCasNumber())) {
                    substances.add(s);
                }
            });
        });
        return substances;
    }

    @Transactional
    public List<Substance> getSubstancesByChemicalClass(List<String> classes) {
        List<Substance> substances = new LinkedList<>();
        Stream<Substance> st = substanceRepository.findByIdGreaterThanOrderByIdAsc(0L);
        st.forEach(s -> {
            if (!Collections.disjoint(s.getChemicalClasses(), classes)) {
                substances.add(s);
            }
        });
        return substances;
    }

    @Transactional
    public List<String> getChemicalClassOptions() {
        List<String> options = new LinkedList<>();
        Stream<Substance> st = substanceRepository.findByIdGreaterThanOrderByIdAsc(0L);
        st.forEach(s -> {
            for (String chemicalClass : s.getChemicalClasses()) {
                if (!options.contains(chemicalClass)) {
                    options.add(chemicalClass);
                }
            }
        });
        options.sort(String::compareTo);
        return options;
    }

    @Transactional
    public BigDecimal calculateSubstanceConcentrationMedian(Substance o1) {
        List<BigDecimal> values = new LinkedList<>();

        for (Sample s : o1.getSamples()) {
            values.add(s.getConcentration());
        }

        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        } else {
            return values.get(Math.floorDiv(values.size(), 2));
        }
    }

    @Transactional
    public ReturnResult<List<Substance>> substanceRelevance(Map<String, String> input) {
        BigDecimal maxPersistence = getMaxAttribute("persistence");
        BigDecimal maxMobility = getMaxAttribute("mobility");
        BigDecimal maxToxicity = getMaxAttribute("toxicity");
        BigDecimal persistenceCoefficient = BigDecimal.valueOf(Float.parseFloat(input.get("persistence")));
        BigDecimal mobilityCoefficient = BigDecimal.valueOf(Float.parseFloat(input.get("mobility")));
        BigDecimal toxicityCoefficient = BigDecimal.valueOf(Float.parseFloat(input.get("toxicity")));

        Stream<Substance> substances = getAllSubstances();
        List<Substance> retdata = new LinkedList<>();

        AtomicInteger totalSubstances = new AtomicInteger();
        substances.forEach(s -> {
            totalSubstances.getAndIncrement();
            if (retdata.size() < 6) {
                retdata.add(s);
            } else {
                BigDecimal importance = calculateSubstanceImportance(s, maxPersistence, maxToxicity, maxMobility, persistenceCoefficient, mobilityCoefficient, toxicityCoefficient);
                if (importance.compareTo(calculateSubstanceImportance(retdata.get(5), maxPersistence, maxToxicity, maxMobility, persistenceCoefficient, mobilityCoefficient, toxicityCoefficient)) > 0) {
                    retdata.remove(5);
                    retdata.add(s);
                }
                retdata.sort((o1, o2) -> {
                    BigDecimal o1_importance = calculateSubstanceImportance(o1, maxPersistence, maxToxicity, maxMobility, persistenceCoefficient, mobilityCoefficient, toxicityCoefficient);
                    BigDecimal o2_importance = calculateSubstanceImportance(o2, maxPersistence, maxToxicity, maxMobility, persistenceCoefficient, mobilityCoefficient, toxicityCoefficient);
                    // Al reves perque volem de gran a petit
                    return o2_importance.compareTo(o1_importance);
                });
            }
        });

        ReturnResult<List<Substance>> ret = new ReturnResult<>();
        ret.setTotalRegisters(totalSubstances.get());
        ret.setRegistersThisPage(6);
        ret.setData(retdata);
        return ret;
    }

    @Transactional
    public Map<String, List<BigDecimal>> violinPlot(Substance substance) {
        Map<String, List<BigDecimal>> data = new HashMap<>();

        for (String key : substance.getConcentrationValues().keySet()) {
            String matrix = key.split(" - ")[0].strip();
            if (!data.containsKey(matrix)) {
                data.put(matrix, new LinkedList<>());
            }
            List<BigDecimal> values = data.get(matrix);
            values.addAll(substance.getConcentrationValues().get(key));
            data.put(matrix, values);
        }
        return data;
    }

    @Transactional
    public ReturnResult<List<Substance>> targetedChemicalPlot() {
        Stream<Substance> substances = getAllSubstances();
        List<Substance> retdata = new LinkedList<>();

        AtomicInteger totalSubstances = new AtomicInteger();
        substances.forEach(s -> {
            totalSubstances.getAndIncrement();
            if (retdata.size() < 20) {
                retdata.add(s);
            } else {
                BigDecimal median = calculateSubstanceConcentrationMedian(s);
                if (median.compareTo(calculateSubstanceConcentrationMedian(retdata.get(19))) > 0) {
                    retdata.remove(19);
                    retdata.add(s);
                }
                retdata.sort((o1, o2) -> {
                    BigDecimal o1_median = calculateSubstanceConcentrationMedian(o1);
                    BigDecimal o2_median = calculateSubstanceConcentrationMedian(o2);
                    // Al reves perque volem de gran a petit
                    return o2_median.compareTo(o1_median);
                });
            }
        });
        ReturnResult<List<Substance>> ret = new ReturnResult<>();
        ret.setTotalRegisters(totalSubstances.get());
        ret.setRegistersThisPage(20);
        ret.setData(retdata);
        return ret;
    }

    @Transactional
    public ReturnResult<List<Substance>> substanceKemiscoreRelevance() {
        BigDecimal maxPersistence = getMaxAttribute("persistence");
        BigDecimal maxMobility = getMaxAttribute("mobility");
        BigDecimal maxToxicity = getMaxAttribute("toxicity");
        BigDecimal maxKemiscore = getMaxAttribute("ExposureScore_Water_KEMI");

        Stream<Substance> substances = getAllSubstances();
        List<Substance> retdata = new LinkedList<>();

        AtomicInteger totalSubstances = new AtomicInteger();
        substances.forEach(s -> {
            totalSubstances.getAndIncrement();
            if (retdata.size() < 6) {
                retdata.add(s);
            } else {
                BigDecimal importance = calculateSubstanceKemiscoreImportance(s, maxPersistence, maxToxicity, maxMobility, maxKemiscore);
                if (importance.compareTo(calculateSubstanceKemiscoreImportance(retdata.get(5), maxPersistence, maxToxicity, maxMobility, maxKemiscore)) > 0) {
                    retdata.remove(5);
                    retdata.add(s);
                }
                retdata.sort((o1, o2) -> {
                    BigDecimal o1_importance = calculateSubstanceKemiscoreImportance(o1, maxPersistence, maxToxicity, maxMobility, maxKemiscore);
                    BigDecimal o2_importance = calculateSubstanceKemiscoreImportance(o2, maxPersistence, maxToxicity, maxMobility, maxKemiscore);
                    // Al reves perque volem de gran a petit
                    return o2_importance.compareTo(o1_importance);
                });
            }
        });

        ReturnResult<List<Substance>> ret = new ReturnResult<>();
        ret.setTotalRegisters(totalSubstances.get());
        ret.setRegistersThisPage(6);
        ret.setData(retdata);
        return ret;
    }

    @Transactional
    public BigDecimal calculateSubstanceKemiscoreImportance(Substance substance,
                                                            BigDecimal maxPersistence,
                                                            BigDecimal maxToxicity,
                                                            BigDecimal maxMobility,
                                                            BigDecimal maxKemiscore) {
        BigDecimal P_normalised = BigDecimal.valueOf(Float.parseFloat(substance.getPersistence().toPlainString()) /
                Float.parseFloat(maxPersistence.toPlainString()));
        BigDecimal M_normalised = BigDecimal.valueOf(Float.parseFloat(substance.getMobility().toPlainString()) /
                Float.parseFloat(maxMobility.toPlainString()));
        BigDecimal T_normalised = BigDecimal.valueOf(Float.parseFloat(substance.getToxicity().toPlainString()) /
                Float.parseFloat(maxToxicity.toPlainString()));
        BigDecimal K_normalised = BigDecimal.valueOf(Float.parseFloat(substance.getNumericalData().getOrDefault("ExposureScore_Water_KEMI", BigDecimal.ZERO).toPlainString()) /
                Float.parseFloat(maxKemiscore.toPlainString()));
        return BigDecimal.valueOf(0.25).multiply(P_normalised.add(M_normalised).add(T_normalised).add(K_normalised));
    }

    @Transactional
    public List<AverageReturn> findAverageScores(Substance substance, String groupby) {
        Map<String, AverageReturn> averages = new HashMap<>();
        List<String> keys;
        if (groupby.equals("pc")) {
            keys = new LinkedList<>();
            Map<String, BigDecimal> numericalData = substance.getNumericalData();
            for (String key : numericalData.keySet()) {
                if (key.contains("PC") && numericalData.get(key).intValue() == 1) {
                    keys.add(key);
                }
            }
            for (String key : keys) {
                averages.put(key, new AverageReturn(key, 0.0F, 0.0F, 0.0F, 0));
            }
            getAllSubstances().forEach(s -> {
                Map<String, BigDecimal> nData = s.getNumericalData();
                for (String k : keys) {
                    if (nData.containsKey(k) && nData.get(k).intValue() == 1) {
                        AverageReturn a = averages.get(k);
                        a.p += s.getPersistence().floatValue();
                        a.m += s.getMobility().floatValue();
                        a.k += nData.getOrDefault("ExposureScore_Water_KEMI", BigDecimal.ZERO).floatValue();
                        a.numberOfSubstances += 1;
                        averages.put(k, a);
                    }
                }
            });
        } else {
            keys = substance.getSectorsOfUse().stream().map(SectorOfUse::getName).toList();
            for (String key : keys) {
                averages.put(key, new AverageReturn(key, 0.0F, 0.0F, 0.0F, 0));
            }
            getAllSubstances().forEach(s -> {
                for (SectorOfUse sec : substance.getSectorsOfUse()) {
                    if (s.getSectorsOfUse().contains(sec)) {
                        AverageReturn a = averages.get(sec.getName());
                        a.p += s.getPersistence().floatValue();
                        a.m += s.getMobility().floatValue();
                        a.k += s.getNumericalData().getOrDefault("ExposureScore_Water_KEMI", BigDecimal.ZERO).floatValue();
                        a.numberOfSubstances += 1;
                        averages.put(sec.getName(), a);
                    }
                }
            });
        }
        List<AverageReturn> retdata = new LinkedList<>();
        for (String key : averages.keySet()) {
            AverageReturn a = averages.get(key);
            a.p = a.p / a.numberOfSubstances;
            a.m = a.m / a.numberOfSubstances;
            a.k = a.k / a.numberOfSubstances;
            retdata.add(a);
        }
        return retdata;
    }
}
