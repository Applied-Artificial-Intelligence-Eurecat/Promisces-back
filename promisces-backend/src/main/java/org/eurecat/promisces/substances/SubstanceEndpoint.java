package org.eurecat.promisces.substances;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletResponse;
import org.eurecat.promisces.tools.ReturnResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/substances/")
public class SubstanceEndpoint {

    @Autowired
    private SubstanceService substanceService;

    @Autowired
    private SimilarityService similarityService;

    @GetMapping("/")
    @ResponseBody
    @JsonView(Substance.class)
    public ResponseEntity<ReturnResult<List<Substance>>> getAllSubstances(@RequestParam("pagenum") Integer pageNumber, @RequestParam("perpage") Integer registersPerPage) {
        var substances = substanceService.getAllSubstances(pageNumber, registersPerPage);
        if (substances == null) {
            return ResponseEntity.badRequest().build();
        }
        if (substances.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        substances.setData(substanceService.normaliseListOfSubstances(substances.getData()));
        return ResponseEntity.ok(substances);
    }

    @GetMapping("/autocomplete/")
    @ResponseBody
    @JsonView(Substance.class)
    public ResponseEntity<List<String>> getAutocompleteOptions(@RequestParam("string") String value, @RequestParam("type") String type) {
        List<String> names;
        switch (type) {
            case "name":
                names = substanceService.getAutocompleteOptions(value);
                break;
            case "cas_n":
                names = substanceService.getAutocompleteOptionsCAS(value);
                break;
            case "inchikey":
                names = substanceService.getAutocompleteOptionsInchi(value);
                break;
            default:
                return ResponseEntity.badRequest().build();
        }
        if (names.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(names);
        }
    }

    @GetMapping("/chemicalclasses/")
    @ResponseBody
    @JsonView(Substance.class)
    public ResponseEntity<List<String>> getChemicalClassOptions() {
        var names = substanceService.getChemicalClassOptions();
        if (names.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(names);
        }
    }

    @PostMapping("/details/")
    @ResponseBody
    @JsonView(Views.Details.class)
    public ResponseEntity<Substance> getSubstanceDetails(@RequestBody Map<String, String> input) {
        if (!(input.containsKey("name") || input.containsKey("cas_n"))) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Substance> substance;
        if (input.containsKey("cas_n")) {
            substance = substanceService.findSubstanceByCASNumber(input.get("cas_n"));
        } else if (input.containsKey("name")) {
            substance = substanceService.findSubstanceByName(input.get("name"));
        } else {
            return ResponseEntity.badRequest().build();
        }
        if (substance.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(substance.get());
    }

    @PostMapping("/search/")
    @ResponseBody
    @JsonView(Views.Search.class)
    public ResponseEntity<ReturnResult<List<Substance>>> getSubstanceSearchResults(@RequestParam("pagenum") Integer pageNumber,
                                                                                   @RequestParam("perpage") Integer registersPerPage,
                                                                                   @RequestBody Map<String, String> input) {
        String substanceName, typeOfSolution, solutionName, substanceGroup, CERoute, chemicalClass, sectorOfUse, followedStrategy, substanceCas, inchikey;
        substanceName = input.getOrDefault("substance_name", "");
        substanceCas = input.getOrDefault("substance_cas", "");
        typeOfSolution = input.getOrDefault("solution_type", "");
        solutionName = input.getOrDefault("solution_name", "");
        substanceGroup = input.getOrDefault("substance_group", "");
        CERoute = input.getOrDefault("ce_route", "");
        chemicalClass = input.getOrDefault("chemical_class", "");
        sectorOfUse = input.getOrDefault("use_sector", "");
        followedStrategy = input.getOrDefault("followed_strategy", "");
        inchikey = input.getOrDefault("inchikey", "");
        Boolean experimental = Boolean.parseBoolean(input.getOrDefault("experimental", "false"));
        Boolean predicted = Boolean.parseBoolean(input.getOrDefault("predicted", "false"));
        List<Substance> substances = substanceService.getSubstanceSearchResults(substanceName,
                substanceCas,
                typeOfSolution,
                solutionName,
                substanceGroup,
                CERoute,
                chemicalClass,
                sectorOfUse,
                followedStrategy,
                inchikey,
                experimental,
                predicted);
        if (substances.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        ReturnResult<List<Substance>> ret = new ReturnResult<>();
        ret.setTotalRegisters(substances.size());
        try {
            ret.setData(substances.subList(registersPerPage * (pageNumber - 1), registersPerPage * pageNumber));
            ret.setRegistersThisPage(registersPerPage);
        } catch (IndexOutOfBoundsException e) {
            try {
                ret.setData(substances.subList(registersPerPage * (pageNumber - 1), substances.size()));
                ret.setRegistersThisPage(ret.getData().size());
            } catch (IndexOutOfBoundsException ex) {
                return null;
            }
        }
        ret.setData(substanceService.normaliseListOfSubstances(ret.getData()));
        return ResponseEntity.ok(ret);
    }

    @PostMapping("/")
    @ResponseBody
    @JsonView(Substance.class)
    public ResponseEntity<Substance> createSubstance(@RequestBody Substance substance, HttpServletResponse response) {
        var created = substanceService.createSubstance(substance, response);
        if (created == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(created);
    }

    @PostMapping("/link/{recipient}/")
    @ResponseBody
    @JsonView(Substance.class)
    public ResponseEntity<Substance> linkSubstance(@PathVariable("recipient") String recipient, @RequestBody Map<String, String> input, HttpServletResponse response) {
        if (recipient.equals("solutions")) {
            if (!(input.containsKey("substance_name") || input.containsKey("substance_cas"))) {
                response.setHeader("error", "substance_name and substance_cas are missing");
                return ResponseEntity.badRequest().build();
            }
            if (!input.containsKey("solution_name")) {
                response.setHeader("error", "solution_name is missing");
                return ResponseEntity.badRequest().build();
            }
            String solutionName = input.get("solution_name");
            if (input.containsKey("substance_cas")) {
                String casNumber = input.get("substance_cas");
                Substance result = substanceService.linkSubstanceByCasToSolution(casNumber, solutionName, response);
                if (result == null) {
                    return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(result);
            } else {
                String substanceName = input.get("substance_name");
                Substance result = substanceService.linkSubstanceByNameToSolution(substanceName, solutionName, response);
                if (result == null) {
                    return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(result);
            }
        } else if (recipient.equals("sectors")) {
            if (!(input.containsKey("substance_name") || input.containsKey("substance_cas"))) {
                response.setHeader("error", "substance_name and substance_cas are missing");
                return ResponseEntity.badRequest().build();
            }
            if (!input.containsKey("sector_name")) {
                response.setHeader("error", "sector_name is missing");
                return ResponseEntity.badRequest().build();
            }
            String sectorName = input.get("sector_name");
            if (input.containsKey("substance_cas")) {
                String casNumber = input.get("substance_cas");
                Substance result = substanceService.linkSubstanceByCasToSector(casNumber, sectorName, response);
                if (result == null) {
                    return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(result);
            } else {
                String substanceName = input.get("substance_name");
                Substance result = substanceService.linkSubstanceByNameToSector(substanceName, sectorName, response);
                if (result == null) {
                    return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(result);
            }
        } else {
            response.setHeader("error", recipient + " is not a valid recipient, valid options are \"solutions\" and \"sectors\"");
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/relevance/")
    @ResponseBody
    @JsonView(Substance.class)
    public ResponseEntity<ReturnResult<List<Substance>>> getSubstancesByRelevance(@RequestBody Map<String, String> input) {
        return ResponseEntity.ok(substanceService.substanceRelevance(input));
    }

    @PostMapping("/counts/{filterBy}/")
    @ResponseBody
    @JsonView(Substance.class)
    public ResponseEntity<Map<String, Map<String, Integer>>> getClassificationCountsFiltered(@PathVariable("filterBy") String filterBy,
                                                                                             @RequestBody Map<String, String> input) {
        if (!Objects.equals(filterBy, "sectorsOfUse") && !Objects.equals(filterBy, "chemicalClasses")) {
            return ResponseEntity.badRequest().build();
        }
        List<String> acceptedFilterValues = new LinkedList<>();
        if (input != null && input.containsKey("values")) {
            acceptedFilterValues.addAll(new LinkedList<>(List.of(input.get("values").split(","))));
        }
        // Obtain substances filtering by acceptedFilterValues
        List<Substance> acceptedSubstances = substanceService.getAllSubstancesFiltered(filterBy, acceptedFilterValues);
        // Recontar quantes substancies de cada classificacio hi ha
        Map<String, Map<String, Integer>> result = new HashMap<>();
        for (String key : List.of("conservative", "robust", "average")) {
            result.put(key, new HashMap<>());
        }

        for (Substance substance : acceptedSubstances) {
            result.get("conservative").put(
                    substance.getConservativeClassification(),
                    result.get("conservative").getOrDefault(substance.getConservativeClassification(), 0) + 1);
            result.get("robust").put(
                    substance.getRobustClassification(),
                    result.get("robust").getOrDefault(substance.getRobustClassification(), 0) + 1);
            result.get("average").put(
                    substance.getAverageClassification(),
                    result.get("average").getOrDefault(substance.getAverageClassification(), 0) + 1);
        }
        // Retornar response
        return ResponseEntity.ok(result);
    }

    @PostMapping("/violinplot/")
    @ResponseBody
    @JsonView(Substance.class)
    public ResponseEntity<Map<String, List<BigDecimal>>> getDataForViolinPlot(@RequestBody Map<String, String> input) {
        if (!(input.containsKey("name") || input.containsKey("cas_n") || input.containsKey("inchikey"))) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Substance> opt = Optional.empty();
        if (input.containsKey("name")) {
            opt = substanceService.findSubstanceByName(input.get("name"));
        } else if (input.containsKey("cas_n")) {
            opt = substanceService.findSubstanceByCASNumber(input.get("cas_n"));
        } else if (input.containsKey("inchikey")) {
            opt = substanceService.findSubstanceByInchikey(input.get("inchikey"));
        }
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(substanceService.violinPlot(opt.get()));
    }

    @GetMapping("/targetedchemical/")
    @ResponseBody
    @JsonView(Views.TargetedChemicals.class)
    public ResponseEntity<ReturnResult<List<Substance>>> getDataForTargetedChemicalPlot() {
        return ResponseEntity.ok(substanceService.targetedChemicalPlot());
    }

    @GetMapping("/kemiscore/")
    @ResponseBody
    @JsonView(Substance.class)
    public ResponseEntity<ReturnResult<List<Substance>>> getSubstancesByKemiscoreRelevance() {
        return ResponseEntity.ok(substanceService.substanceKemiscoreRelevance());
    }

    @PostMapping("/similarSubstances/")
    @ResponseBody
    public ResponseEntity<List<SimilarityReturn>> getSimilarSubstances(@RequestBody Map<String, String> input) throws IOException, InterruptedException {
        if (!(input.containsKey("name") || input.containsKey("cas_n") || input.containsKey("inchikey"))) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Substance> opt = Optional.empty();
        if (input.containsKey("name")) {
            opt = substanceService.findSubstanceByName(input.get("name"));
        } else if (input.containsKey("cas_n")) {
            opt = substanceService.findSubstanceByCASNumber(input.get("cas_n"));
        } else if (input.containsKey("inchikey")) {
            opt = substanceService.findSubstanceByInchikey(input.get("inchikey"));
        }
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<SimilarityReturn> similars = similarityService.getSimilarities(opt.get().getCanonicalSMILES(), opt.get().getNameSynonyms().get(0));

        if (similars.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        /*
        for (SimilarityReturn similar : similars) {
            Optional<Substance> sus = substanceService.findSubstanceByName(similar.name);
            if (sus.isPresent()) {
                Substance s = sus.get();
                similar.p = s.getPersistence().floatValue();
                similar.m = s.getMobility().floatValue();
                similar.t = s.getToxicity().floatValue();
                similar.k = s.getNumericalData().getOrDefault("ExposureScore_Water_KEMI", BigDecimal.valueOf(0)).floatValue();
            }
        }
         */
        return ResponseEntity.ok(similars);
    }

    @PostMapping("/averagescores/")
    @ResponseBody
    public ResponseEntity<List<AverageReturn>> getAverageScores(@RequestBody Map<String, String> input) {
        if (!(input.containsKey("name") || input.containsKey("cas_n") || input.containsKey("inchikey"))) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Substance> opt = Optional.empty();
        if (input.containsKey("name")) {
            opt = substanceService.findSubstanceByName(input.get("name"));
        } else if (input.containsKey("cas_n")) {
            opt = substanceService.findSubstanceByCASNumber(input.get("cas_n"));
        } else if (input.containsKey("inchikey")) {
            opt = substanceService.findSubstanceByInchikey(input.get("inchikey"));
        }
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!input.containsKey("groupby")){
            return ResponseEntity.badRequest().build();
        }
        String groupby = input.get("groupby");
        if (!groupby.equals("pc") && !groupby.equals("sector_of_use")) {
            return ResponseEntity.badRequest().build();
        }

        List<AverageReturn> retval = substanceService.findAverageScores(opt.get(), groupby);

        return ResponseEntity.ok(retval);
    }
}
