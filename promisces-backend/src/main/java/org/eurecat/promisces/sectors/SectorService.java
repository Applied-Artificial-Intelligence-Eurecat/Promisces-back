package org.eurecat.promisces.sectors;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SectorService {

    @Autowired
    private SectorRepository sectorRepository;

    public List<SectorOfUse> getAllSectors() {
        List<SectorOfUse> sectors = new LinkedList<>();
        sectorRepository.findAll().forEach(sectors::add);
        return sectors;
    }

    public Optional<SectorOfUse> findSectorByName(String name) {
        return sectorRepository.findByName(name);
    }

    public SectorOfUse createSector(SectorOfUse sector, HttpServletResponse response) {
        if (sectorRepository.findByName(sector.getName()).isPresent()) {
            response.setHeader("error", "A sector with this name already exists");
            return null;
        }
        if (sector.getName() == null || Objects.equals(sector.getName(), "")) {
            response.setHeader("error", "Can't create a sector without name");
        }
        return sectorRepository.save(sector);
    }
}
