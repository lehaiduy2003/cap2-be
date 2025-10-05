package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.dto.LocationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class GoogleMapsService {

    @Value("${GOOGLE_MAPS_API_KEY}")
    private String googleMapsApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String NEARBY_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private static final int RADIUS_METERS = 1000; // 1km

    public GoogleMapsService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public LocationResponse searchLocation(String address) {
        try {
            // First, geocode the address to get coordinates
            LocationResponse.LocationData locationData = geocodeAddress(address);
            log.debug("locationData: {}", locationData);
            if (locationData == null) {
                return new LocationResponse("ERROR", "Address not found", null, null);
            }

            // Then search for nearby places
            List<LocationResponse.NearbyPlace> nearbyPlaces = searchNearbyPlaces(
                locationData.getLatitude(), 
                locationData.getLongitude()
            );

            return new LocationResponse("SUCCESS", "Location found successfully", locationData, nearbyPlaces);

        } catch (Exception e) {
            log.error("Error searching location: {}", e.getMessage(), e);
            return new LocationResponse("ERROR", "Failed to search location: " + e.getMessage(), null, null);
        }
    }

    private static String normalizeAddress(String raw) {
        if (raw == null) return "";
        // Only normalize Unicode, don't URL encode here - let UriComponentsBuilder handle it
        return Normalizer.normalize(raw, Normalizer.Form.NFC).trim();
    }

    private LocationResponse.LocationData geocodeAddress(String address) {
        try {
            // Try multiple variations of the address for better accuracy
            String[] addressVariations = createAddressVariations(address);

            for (String addressVariation : addressVariations) {
                log.debug("Trying address variation: {}", addressVariation);
                LocationResponse.LocationData result = tryGeocode(addressVariation);
                if (result != null) {
                    // Validate the result is actually in Vietnam (rough bounds check)
                    if (isInVietnam(result.getLatitude(), result.getLongitude())) {
                        log.debug("Found valid Vietnam location: {}", result);
                        return result;
                    } else {
                        log.debug("Location found but outside Vietnam bounds: lat={}, lng={}",
                               result.getLatitude(), result.getLongitude());
                    }
                }
            }

            log.warn("No valid results found for any address variation");
            return null;

        } catch (Exception e) {
            log.error("Error geocoding address: {}", address, e);
            return null;
        }
    }

    private String[] createAddressVariations(String address) {
        String normalized = normalizeAddress(address);
        List<String> variations = new ArrayList<>();

        // Original address
        variations.add(normalized);

        // Add ", Vietnam" if not present
        if (!normalized.toLowerCase().contains("vietnam") && !normalized.toLowerCase().contains("việt nam")) {
            variations.add(normalized + ", Vietnam");
            variations.add(normalized + ", Việt Nam");
        }

        // Try with common Vietnamese address prefixes if they're missing
        String addressLower = normalized.toLowerCase();

        // Add "Đường" prefix to potential street names (if address starts with a number)
        if (addressLower.matches("^\\d+.*")) {
            // Extract potential street name (everything after the house number and slash)
            String[] parts = normalized.split("\\s+", 3); // Split into max 3 parts
            if (parts.length >= 2) {
                // If second part doesn't start with common prefixes, try adding "Đường"
                String secondPart = parts[1];
                if (!secondPart.toLowerCase().startsWith("đường") &&
                    !secondPart.toLowerCase().startsWith("phố") &&
                    !secondPart.toLowerCase().startsWith("street")) {
                    String withStreetPrefix = parts[0] + " Đường " + String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
                    variations.add(withStreetPrefix);
                    variations.add(withStreetPrefix + ", Vietnam");
                }
            }
        }

        return variations.toArray(new String[0]);
    }

    private LocationResponse.LocationData tryGeocode(String address) {
        try {
            String url = UriComponentsBuilder.fromUriString(GEOCODING_URL)
                .queryParam("address", address)
                .queryParam("region", "vn")
                .queryParam("components", "country:VN")
                .queryParam("language", "vi")
                .queryParam("key", googleMapsApiKey)
                .build()
                .toUriString();

            log.debug("Geocoding URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");
            headers.set("Accept-Language", "vi-VN,vi;q=0.9,en;q=0.8");

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
            );

            log.debug("API Response: {}", response.getBody());

            JsonNode root = objectMapper.readTree(response.getBody());

            String status = root.path("status").asText();
            if (!"OK".equals(status)) {
                log.warn("Geocoding failed with status: {} for address: {}", status, address);
                return null;
            }

            JsonNode results = root.path("results");
            if (!results.isArray() || results.isEmpty()) {
                log.warn("No results found for address: {}", address);
                return null;
            }

            JsonNode first = results.get(0);
            JsonNode geometry = first.path("geometry");
            JsonNode location = geometry.path("location");

            return new LocationResponse.LocationData(
                first.path("formatted_address").asText(),
                location.path("lat").asDouble(),
                location.path("lng").asDouble(),
                first.path("place_id").asText()
            );


        } catch (Exception e) {
            log.error("Error in tryGeocode for address: {}", address, e);
            return null;
        }
    }

    private boolean isInVietnam(double latitude, double longitude) {
        // Vietnam approximate bounds
        // North: ~23.4, South: ~8.2, East: ~109.5, West: ~102.1
        return latitude >= 8.0 && latitude <= 23.5 &&
               longitude >= 102.0 && longitude <= 110.0;
    }

    private List<LocationResponse.NearbyPlace> searchNearbyPlaces(double latitude, double longitude) {
        List<LocationResponse.NearbyPlace> nearbyPlaces = new ArrayList<>();

        // Define important place types to search for
        String[] placeTypes = {
            "hospital", "police", "university", "school",
            "fire_station", "bank",
            "supermarket", "restaurant", "hotel", "cafe",
            "park"
        };

        try {
            // Search for each place type
            for (String placeType : placeTypes) {
                searchPlacesByType(latitude, longitude, placeType, nearbyPlaces);
            }

            // Remove duplicates based on place_id and sort by distance
            nearbyPlaces = nearbyPlaces.stream()
                .collect(java.util.stream.Collectors.toMap(
                    LocationResponse.NearbyPlace::getPlaceId,
                    place -> place,
                    (existing, replacement) -> existing.getDistanceInMeters() <= replacement.getDistanceInMeters() ? existing : replacement
                ))
                .values()
                .stream()
                .sorted(Comparator.comparingDouble(LocationResponse.NearbyPlace::getDistanceInMeters))
                .toList();

        } catch (Exception e) {
            log.error("Error searching nearby places: {}", e.getMessage(), e);
        }

        return nearbyPlaces;
    }

    private void searchPlacesByType(double latitude, double longitude, String placeType, List<LocationResponse.NearbyPlace> nearbyPlaces) {
        try {
            String url = UriComponentsBuilder.fromUriString(NEARBY_SEARCH_URL)
                .queryParam("location", latitude + "," + longitude)
                .queryParam("radius", RADIUS_METERS)
                .queryParam("type", placeType)
                .queryParam("key", googleMapsApiKey)
                .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (!"OK".equals(root.get("status").asText())) {
                log.debug("Nearby search failed for type {} with status: {}", placeType, root.get("status").asText());
                return;
            }

            JsonNode results = root.get("results");
            for (JsonNode place : results) {
                JsonNode location = place.get("geometry").get("location");
                double placeLat = location.get("lat").asDouble();
                double placeLng = location.get("lng").asDouble();

                // Calculate distance
                double distance = calculateDistance(latitude, longitude, placeLat, placeLng);

                LocationResponse.NearbyPlace nearbyPlace = new LocationResponse.NearbyPlace(
                    place.get("name").asText(),
                    place.has("vicinity") ? place.get("vicinity").asText() : "",
                    placeLat,
                    placeLng,
                    place.get("place_id").asText(),
                    place.has("rating") ? place.get("rating").asDouble() : 0.0,
                    placeType, // Use the searched place type
                    distance
                );

                nearbyPlaces.add(nearbyPlace);
            }

        } catch (Exception e) {
            log.debug("Error searching for place type {}: {}", placeType, e.getMessage());
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        // distance in meters
        return R * c * 1000;
    }
}
