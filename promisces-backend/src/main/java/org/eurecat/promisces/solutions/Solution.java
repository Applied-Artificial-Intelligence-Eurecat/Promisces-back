package org.eurecat.promisces.solutions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.*;
import org.eurecat.promisces.routes.CERoute;
import org.eurecat.promisces.solutions.criteria.DegradationCriteria;
import org.eurecat.promisces.solutions.criteria.RecoveryCriteria;
import org.eurecat.promisces.solutions.criteria.SolutionCriteria;
import org.eurecat.promisces.strategies.Strategy;
import org.eurecat.promisces.substances.Substance;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "solution")
public class Solution {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(unique = true)
    private String name;
    private String type;
    private String addressedApplication;
    private Boolean isBiological;
    private Boolean exOrInSitu;

    @OneToOne
    @JoinColumn(name = "criteria_id")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = DegradationCriteria.class, name = "degradation"),
            @JsonSubTypes.Type(value = RecoveryCriteria.class, name = "recovery")
    })
    private SolutionCriteria criteria;

    @ManyToMany(mappedBy = "relatedSolutions")
    @JsonView(Solution.class)
    Set<Substance> relatedSubstances;

    @ManyToMany
    @JoinTable(
            name = "solution_strategy",
            joinColumns = @JoinColumn(name = "solution_id"),
            inverseJoinColumns = @JoinColumn(name = "strategy_id")
    )
    @JsonView(Solution.class)
    Set<Strategy> relatedStrategies = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "ceroute_id", nullable = true)
    @JsonView(Solution.class)
    private CERoute ceRoute;
}
