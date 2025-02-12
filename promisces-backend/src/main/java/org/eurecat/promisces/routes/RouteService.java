package org.eurecat.promisces.routes;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class RouteService {

    @Autowired
    private RouteRepository routeRepository;

    public List<CERoute> getAllSectors() {
        List<CERoute> sectors = new LinkedList<>();
        routeRepository.findAll().forEach(sectors::add);
        return sectors;
    }

    public Optional<CERoute> findSectorByName(String name) {
        return routeRepository.findByName(name);
    }

    public CERoute createRoute(CERoute route, HttpServletResponse response) {
        if (routeRepository.findByName(route.getName()).isPresent()) {
            response.setHeader("error", "A route with this name already exists");
            return null;
        }
        if (route.getName() == null || Objects.equals(route.getName(), "")) {
            response.setHeader("error", "Can't create a route without name");
            return null;
        }
        return routeRepository.save(route);
    }
}
