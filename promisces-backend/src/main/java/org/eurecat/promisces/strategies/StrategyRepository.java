package org.eurecat.promisces.strategies;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StrategyRepository extends CrudRepository<Strategy, Long> {
    Optional<Strategy> findByName(String name);
}
