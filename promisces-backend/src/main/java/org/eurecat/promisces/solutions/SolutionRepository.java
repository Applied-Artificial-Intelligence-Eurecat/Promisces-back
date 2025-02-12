package org.eurecat.promisces.solutions;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SolutionRepository extends CrudRepository<Solution, Long> {
    Optional<Solution> findByName(String name);
}
