package com.c1se_01.roomiego.controller;

import com.c1se_01.roomiego.model.Marker;
import com.c1se_01.roomiego.repository.MarkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/markers")
public class MarkerController {
    @Autowired
    private MarkerRepository markerRepository;

    @GetMapping
    public List<Marker> getAllMarkers() {
        return markerRepository.findAll();
    }

    @PostMapping
    public Marker createMarker(@RequestBody Marker marker) {
        return markerRepository.save(marker);
    }

    @GetMapping("/{id}")
    public Marker getMarkerById(@PathVariable Long id) {
        return markerRepository.findById(id)
                .orElse(null);
    }

    @PutMapping("/{id}")
    public Marker updateMarker(@PathVariable Long id, @RequestBody Marker marker) {
        Marker existingMarker = markerRepository.findById(id)
                .orElse(null);
        if (existingMarker != null) {
            existingMarker.setName(marker.getName());
            existingMarker.setAddress(marker.getAddress());
            existingMarker.setLatitude(marker.getLatitude());
            existingMarker.setLongitude(marker.getLongitude());
            existingMarker.setPrice(marker.getPrice());
            existingMarker.setRoommates(marker.getRoommates());
            existingMarker.setImageUrl(marker.getImageUrl());
            return markerRepository.save(existingMarker);
        } else {
            return null;
        }
    }

    @DeleteMapping("/{id}")
    public void deleteMarker(@PathVariable Long id) {
        markerRepository.deleteById(id);
    }
}

