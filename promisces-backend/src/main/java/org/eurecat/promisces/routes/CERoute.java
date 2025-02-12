package org.eurecat.promisces.routes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.*;
import org.eurecat.promisces.solutions.Solution;
import org.eurecat.promisces.substances.Substance;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ceroute")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CERoute {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(unique = true, columnDefinition = "text")
    private String name;

    @ManyToMany(mappedBy = "ceRoutes")
    @JsonView(CERoute.class)
    Set<Substance> relatedSubstances = new HashSet<>();
}
