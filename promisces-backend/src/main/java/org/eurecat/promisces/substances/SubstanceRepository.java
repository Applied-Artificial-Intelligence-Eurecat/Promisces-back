package org.eurecat.promisces.substances;

import org.eurecat.promisces.sectors.SectorOfUse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface SubstanceRepository extends JpaRepository<Substance, Long> {
    Optional<Substance> findByCasNumber(String casNumber);

    List<Substance> findBySectorsOfUse_NameContainsIgnoreCase(String name);

    Stream<Substance> findByIdGreaterThanOrderByIdAsc(Long id);

    Optional<Substance> findByInchikey(String inchikey);
}
