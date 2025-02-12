package org.eurecat.promisces.sectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.*;
import org.eurecat.promisces.substances.Substance;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "sector")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SectorOfUse {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(unique = true)
    private String code;

    @Column(unique = true, columnDefinition = "text")
    private String name;

    @ManyToMany(mappedBy = "sectorsOfUse")
    @JsonView(SectorOfUse.class)
    Set<Substance> relatedSubstances = new HashSet<>();
}
