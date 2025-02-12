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
public class RecoveryCriteria extends SolutionCriteria {
    private BigDecimal mobilisationYield;

    @Builder
    public RecoveryCriteria(Long id, BigDecimal mobilisationYield, BigDecimal technologyReadinessLevel, BigDecimal capacity, BigDecimal cost){
        super(id, technologyReadinessLevel, capacity, cost);
        this.mobilisationYield = mobilisationYield;
    }
}
