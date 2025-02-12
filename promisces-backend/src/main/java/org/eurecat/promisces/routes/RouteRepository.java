package org.eurecat.promisces.routes;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RouteRepository extends CrudRepository<CERoute, Long> {
    Optional<CERoute> findByName(String name);
}
