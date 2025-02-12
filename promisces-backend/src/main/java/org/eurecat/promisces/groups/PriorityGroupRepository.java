package org.eurecat.promisces.groups;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PriorityGroupRepository extends CrudRepository<PriorityGroup, Long> {
    Optional<PriorityGroup> findByGroupName(String groupName);
}
