package org.eurecat.promisces.substances;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.*;
import org.eurecat.promisces.groups.PriorityGroup;
import org.eurecat.promisces.routes.CERoute;
import org.eurecat.promisces.sectors.SectorOfUse;
import org.eurecat.promisces.solutions.Solution;
import org.eurecat.promisces.tools.DecimalListConverter;
import org.eurecat.promisces.tools.MapConverter;
import org.eurecat.promisces.tools.MapListConverter;
import org.eurecat.promisces.tools.StringListConverter;

import javax.swing.text.View;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "substance")
public class Substance {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    @JsonView({Views.AllAttributes.class, Substance.class})
    private Long id;

    //@Column(unique = true)
    @Column(columnDefinition = "text")
    @JsonView({Views.AllAttributes.class, Substance.class, Views.TargetedChemicals.class, Views.Search.class, Views.Details.class})
    private String casNumber;
    //@Column(unique = true)
    @Column(columnDefinition = "text")
    @JsonView({Views.AllAttributes.class, Substance.class, Views.Search.class, Views.Details.class})
    private String ECNumber;
    //@Column(unique = true)
    @Column(columnDefinition = "text")
    @JsonView({Views.AllAttributes.class, Substance.class, Views.Search.class})
    private String promiscesID;
    //@Column(unique = true)
    @Column(columnDefinition = "text")
    @JsonView({Views.AllAttributes.class, Substance.class, Views.Search.class, Views.Details.class})
    private String normanSusDatID;

    @JsonView({Views.AllAttributes.class, Substance.class})
    @Column(columnDefinition = "text")
    private String inchikey;

    //@Column(unique = true)
    @JsonView({Views.AllAttributes.class, Substance.class, Views.Search.class, Views.Details.class})
    @Column(columnDefinition = "text")
    private String canonicalSMILES;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "text", length = 2000)
    @JsonView({Views.AllAttributes.class, Substance.class, Views.TargetedChemicals.class, Views.Search.class, Views.Details.class})
    private List<String> nameSynonyms;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "text", length = 2000)
    @JsonView({Views.AllAttributes.class, Substance.class})
    private List<String> chemicalClasses;

    @JsonView({Views.AllAttributes.class, Substance.class})
    private String tClass;

    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonView({Views.AllAttributes.class, Substance.class})
    private PriorityGroup group;

    @JsonView({Views.AllAttributes.class, Substance.class, Views.Search.class})
    private BigDecimal persistence;
    @JsonView({Views.AllAttributes.class, Substance.class, Views.Search.class})
    private BigDecimal mobility;
    @JsonView({Views.AllAttributes.class, Substance.class, Views.Search.class})
    private BigDecimal toxicity;

    @JsonView({Views.AllAttributes.class, Substance.class})
    private String potentialEnvironmentEmissions;
    @JsonView({Views.AllAttributes.class, Substance.class})
    private String addressedApplication;

    @JsonView({Views.AllAttributes.class, Substance.class})
    private String CLPharmonised;

    @JsonView({Views.AllAttributes.class, Substance.class, Views.Search.class, Views.Details.class})
    private String conservativeClassification;
    @JsonView({Views.AllAttributes.class, Substance.class, Views.Search.class, Views.Details.class})
    private String robustClassification;
    @JsonView({Views.AllAttributes.class, Substance.class, Views.Search.class, Views.Details.class})
    private String averageClassification;

    @Convert(converter = MapConverter.class)
    @Column(columnDefinition = "text")
    @JsonView({Views.AllAttributes.class, Substance.class, Views.Search.class, Views.Details.class})
    private Map<String, BigDecimal> numericalData;

    @Convert(converter = MapListConverter.class)
    @Column(columnDefinition = "text")
    @JsonView({Views.AllAttributes.class, Substance.class})
    private Map<String, List<BigDecimal>> concentrationValues;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "substance_solution",
            joinColumns = @JoinColumn(name = "substance_id"),
            inverseJoinColumns = @JoinColumn(name = "solution_id")
    )
    @JsonView(Substance.class)
    Set<Solution> relatedSolutions;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "substance_sector",
            joinColumns = @JoinColumn(name = "substance_id"),
            inverseJoinColumns = @JoinColumn(name = "sector_id")
    )
    @JsonView(Substance.class)
    Set<SectorOfUse> sectorsOfUse;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "substance_route",
            joinColumns = @JoinColumn(name = "substance_id"),
            inverseJoinColumns = @JoinColumn(name = "ceroute_id")
    )
    @JsonView(Substance.class)
    Set<CERoute> ceRoutes;

    @OneToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "substance_sample",
            joinColumns = @JoinColumn(name = "substance_id")
    )
    @JsonView({Substance.class, Views.TargetedChemicals.class})
    Set<Sample> samples;
}
