package org.eurecat.promisces.sectors;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SectorRepository extends CrudRepository<SectorOfUse, Long> {
    Optional<SectorOfUse> findByName(String name);

    Optional<SectorOfUse> findByCode(String code);
}
