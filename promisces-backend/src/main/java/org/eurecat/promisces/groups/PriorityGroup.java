package org.eurecat.promisces.groups;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.*;
import org.eurecat.promisces.substances.Substance;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "prioritygroups")
public class PriorityGroup {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(unique = true, columnDefinition = "text")
    private String groupName;

    @Column(columnDefinition = "text")
    private String groupDescription;

    @OneToMany(mappedBy = "group")
    @JsonView(PriorityGroup.class)
    private Set<Substance> relatedSubstances;
}
