package org.eurecat.promisces.strategies;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.*;
import org.eurecat.promisces.solutions.Solution;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "strategy")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Strategy {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(unique = true, columnDefinition = "text")
    private String name;

    @ManyToMany(mappedBy = "relatedStrategies")
    @JsonView(Strategy.class)
    Set<Solution> relatedSolutions = new HashSet<>();
}
