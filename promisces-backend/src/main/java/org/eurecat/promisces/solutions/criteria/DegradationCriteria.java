package org.eurecat.promisces.solutions.criteria;

import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DegradationCriteria extends SolutionCriteria {
    private BigDecimal degradationYield;
    private BigDecimal degradationCompleteness;
    // private BigDecimal degradationProducts;
    // private BigDecimal toxicityOfEffluent;

    @Builder
    public DegradationCriteria(Long id, BigDecimal technologyReadinessLevel, BigDecimal capacity, BigDecimal cost,
                               BigDecimal degradationYield, BigDecimal degradationCompleteness) {
        super(id, technologyReadinessLevel, capacity, cost);
        this.degradationYield = degradationYield;
        this.degradationCompleteness = degradationCompleteness;
    }
}