package com.c1se_01.roomiego.component;

import com.c1se_01.roomiego.model.Marker;
import com.c1se_01.roomiego.repository.MarkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private MarkerRepository markerRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create some sample markers
        Marker marker1 = new Marker("Room 1", "35 Da Le", 16.426394, 107.634812, 1400, 3, "https://example.com/images/room1.jpg");
        Marker marker2 = new Marker("Room 2", "25 Da Le", 16.427902, 107.635476, 1200, 2, "https://example.com/images/room2.jpg");
        markerRepository.save(marker1);
        markerRepository.save(marker2);
    }
}

