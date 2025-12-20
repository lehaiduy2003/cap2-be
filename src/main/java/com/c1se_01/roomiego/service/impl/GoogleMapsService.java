package com.c1se_01.roomiego.service.impl;

import com.c1se_01.roomiego.dto.DistanceMatrixResult;
import com.c1se_01.roomiego.dto.LocationMarkerRequest;
import com.c1se_01.roomiego.dto.LocationMarkerResponse;
import com.c1se_01.roomiego.dto.LocationResponse;
import com.c1se_01.roomiego.dto.RoomDTO;
import com.c1se_01.roomiego.dto.common.FilterParam;
import com.c1se_01.roomiego.service.RoomService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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
import java.util.Optional;

@Service
@Slf4j
public class GoogleMapsService {

    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();
    private final RoomService roomService;

    private static final String GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String NEARBY_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private static final String DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
    private static final int RADIUS_METERS = 500; // 500m

    public GoogleMapsService(@Lazy RoomService roomService) {
        this.roomService = roomService;
    }

    public LocationMarkerResponse[] getMarkers(List<LocationMarkerRequest> requests) {
        List<LocationMarkerResponse> responses = new ArrayList<>();
        FilterParam filter = new FilterParam();
        filter.setPage(0);
        filter.setSize(1000);
        List<RoomDTO> rooms = this.roomService.getAllRooms(filter);

        if (rooms.isEmpty()) {
            for (LocationMarkerRequest request : requests) {
                try {
                    LocationResponse.LocationData locationData = geocodeAddress(request.getAddress());
                    if (locationData != null) {
                        LocationMarkerResponse response = new LocationMarkerResponse(
                                Long.valueOf(request.getId()),
                                locationData.getFormattedAddress(),
                                locationData.getLongitude(),
                                locationData.getLatitude(),
                                Optional.empty(),
                                false);
                        responses.add(response);
                    } else {
                        log.warn("No location data found for address: {}", request.getAddress());
                    }
                } catch (Exception e) {
                    log.error("Error processing marker request for address {}: {}", request.getAddress(),
                            e.getMessage(),
                            e);
                }
            }
        }

        for (RoomDTO room : rooms) {
            LocationMarkerResponse response = new LocationMarkerResponse(
                    room.getId(),
                    room.getAddressDetails(),
                    room.getLongitude(),
                    room.getLatitude(),
                    Optional.of(room),
                    true);
            responses.add(response);
        }

        return responses.toArray(new LocationMarkerResponse[0]);
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
                    locationData.getLongitude());

            return new LocationResponse("SUCCESS", "Location found successfully", locationData, nearbyPlaces);

        } catch (Exception e) {
            log.error("Error searching location: {}", e.getMessage(), e);
            return new LocationResponse("ERROR", "Failed to search location: " + e.getMessage(), null, null);
        }
    }

    private static String normalizeAddress(String raw) {
        if (raw == null)
            return "";
        // Only normalize Unicode, don't URL encode here - let UriComponentsBuilder
        // handle it
        return Normalizer.normalize(raw, Normalizer.Form.NFC).trim();
    }

    public LocationResponse.LocationData geocodeAddress(String address) {
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
            variations.add(normalized + ", Việt Nam");
        }

        // Try with common Vietnamese address prefixes if they're missing
        String addressLower = normalized.toLowerCase();

        // Add "Đường" prefix to potential street names (if address starts with a
        // number)
        if (addressLower.matches("^\\d+.*")) {
            // Extract potential street name (everything after the house number and slash)
            String[] parts = normalized.split("\\s+", 3); // Split into max 3 parts
            if (parts.length >= 2) {
                // If second part doesn't start with common prefixes, try adding "Đường"
                String secondPart = parts[1];
                if (!secondPart.toLowerCase().startsWith("đường") &&
                        !secondPart.toLowerCase().startsWith("phố") &&
                        !secondPart.toLowerCase().startsWith("street")) {
                    String withStreetPrefix = parts[0] + " Đường "
                            + String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
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
                    String.class);

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

            // Find the best matching result based on the original query
            JsonNode bestMatch = findBestMatchingResult(results, address);

            if (bestMatch == null) {
                log.warn("No suitable match found for address: {}", address);
                return null;
            }

            JsonNode geometry = bestMatch.path("geometry");
            JsonNode location = geometry.path("location");

            return new LocationResponse.LocationData(
                    bestMatch.path("formatted_address").asText(),
                    location.path("lat").asDouble(),
                    location.path("lng").asDouble(),
                    bestMatch.path("place_id").asText());

        } catch (Exception e) {
            log.error("Error in tryGeocode for address: {}", address, e);
            return null;
        }
    }

    /**
     * Find the best matching result from geocoding API based on the query string
     * Prioritizes results that contain key parts of the original query
     */
    private JsonNode findBestMatchingResult(JsonNode results, String query) {
        // Extract important keywords from the query using regex
        // Look for street names, district names, university names, etc.
        String normalizedQuery = normalizeAddress(query).toLowerCase();

        // Extract potential street names, places, or landmarks from the query
        List<String> keywords = extractKeywords(normalizedQuery);

        log.debug("Extracted keywords from query '{}': {}", query, keywords);

        int bestScore = -1;
        JsonNode bestResult = null;

        for (int i = 0; i < results.size(); i++) {
            JsonNode result = results.get(i);
            String formattedAddress = result.path("formatted_address").asText().toLowerCase();

            int score = 0;

            // Check how many keywords are present in the formatted address
            for (String keyword : keywords) {
                if (formattedAddress.contains(keyword)) {
                    score += 10; // High weight for keyword match
                }
            }

            // Prefer results that are NOT partial matches (exact match is better)
            boolean isPartialMatch = result.path("partial_match").asBoolean(false);
            if (!isPartialMatch) {
                score += 5;
            }

            // Prefer more specific location types (street_address > route > locality)
            JsonNode types = result.path("types");
            if (types.isArray()) {
                for (JsonNode type : types) {
                    String typeStr = type.asText();
                    if ("street_address".equals(typeStr) || "premise".equals(typeStr)) {
                        score += 3;
                    } else if ("route".equals(typeStr)) {
                        score += 2;
                    } else if ("establishment".equals(typeStr) || "point_of_interest".equals(typeStr)) {
                        score += 1;
                    }
                }
            }

            log.debug("Result {}: '{}' - Score: {} (partial_match: {})",
                    i, formattedAddress, score, isPartialMatch);

            if (score > bestScore) {
                bestScore = score;
                bestResult = result;
            }
        }

        log.debug("Best match selected with score: {}", bestScore);
        return bestResult != null ? bestResult : results.get(0); // Fallback to first result
    }

    /**
     * Extract important keywords from address query using regex
     * Focuses on street names, place names, district names, etc.
     */
    private List<String> extractKeywords(String normalizedQuery) {
        List<String> keywords = new ArrayList<>();

        // Common Vietnamese address patterns
        // 1. Street names (after "đường", "phố", or standalone capitalized words)
        java.util.regex.Pattern streetPattern = java.util.regex.Pattern.compile(
                "(?:đường|phố|street)\\s+([\\p{L}\\s]+?)(?:,|$|\\s+(?:phường|quận|huyện|thành phố))",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher streetMatcher = streetPattern.matcher(normalizedQuery);
        while (streetMatcher.find()) {
            keywords.add(streetMatcher.group(1).trim());
        }

        // 2. District/Ward names
        java.util.regex.Pattern districtPattern = java.util.regex.Pattern.compile(
                "(?:quận|huyện)\\s+([\\p{L}0-9\\s]+?)(?:,|$)",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher districtMatcher = districtPattern.matcher(normalizedQuery);
        while (districtMatcher.find()) {
            keywords.add(districtMatcher.group(1).trim());
        }

        // 3. University/Institution names
        java.util.regex.Pattern uniPattern = java.util.regex.Pattern.compile(
                "(?:đại học|trường|university)\\s+([\\p{L}\\s]+?)(?:cơ sở|campus|,|$)",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher uniMatcher = uniPattern.matcher(normalizedQuery);
        while (uniMatcher.find()) {
            keywords.add(uniMatcher.group(1).trim());
        }

        // 4. Campus/Branch location (cơ sở ...)
        java.util.regex.Pattern campusPattern = java.util.regex.Pattern.compile(
                "cơ sở\\s+([\\p{L}\\s]+?)(?:,|$)",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher campusMatcher = campusPattern.matcher(normalizedQuery);
        while (campusMatcher.find()) {
            String campusLocation = campusMatcher.group(1).trim();
            keywords.add(campusLocation);
            // Also add as a potential street name
            keywords.add(campusLocation.replaceAll("\\s+", " "));
        }

        // 5. Extract standalone capitalized words (potential proper nouns)
        // This helps catch multi-word street/place names
        String[] words = normalizedQuery.split("[,\\s]+");
        StringBuilder properNoun = new StringBuilder();
        for (String word : words) {
            // Skip common Vietnamese address keywords
            if (word.matches("(?i)đường|phố|phường|quận|huyện|thành|phố|tỉnh|xã|street|ward|district|city|province")) {
                if (properNoun.length() > 0) {
                    keywords.add(properNoun.toString().trim());
                    properNoun = new StringBuilder();
                }
            } else if (word.length() > 2) { // Avoid single letters
                if (properNoun.length() > 0) {
                    properNoun.append(" ");
                }
                properNoun.append(word);
            }
        }
        if (properNoun.length() > 0) {
            keywords.add(properNoun.toString().trim());
        }

        return keywords.stream()
                .filter(k -> k.length() > 2) // Filter out very short keywords
                .distinct()
                .toList();
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
                "fire_station", "supermarket", "restaurant", "train_station", "gas_station", "park"
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
                            (existing,
                                    replacement) -> existing.getDistanceInMeters() <= replacement.getDistanceInMeters()
                                            ? existing
                                            : replacement))
                    .values()
                    .stream()
                    .sorted(Comparator.comparingDouble(LocationResponse.NearbyPlace::getDistanceInMeters))
                    .toList();

        } catch (Exception e) {
            log.error("Error searching nearby places: {}", e.getMessage(), e);
        }

        return nearbyPlaces;
    }

    private void searchPlacesByType(double latitude, double longitude, String placeType,
            List<LocationResponse.NearbyPlace> nearbyPlaces) {
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
                        distance);

                nearbyPlaces.add(nearbyPlace);
            }

        } catch (Exception e) {
            log.debug("Error searching for place type {}: {}", placeType, e.getMessage());
        }
    }

    /**
     * Calculate distance and duration between two addresses using Google Maps
     * Distance Matrix API
     * This method uses address strings directly for better accuracy with place
     * names
     * 
     * @param originAddress Origin address string
     * @param destAddress   Destination address string
     * @param mode          Travel mode: "driving", "walking", "bicycling",
     *                      "transit"
     *                      (default: "driving")
     * @return DistanceMatrixResult with distance and duration information
     */
    public DistanceMatrixResult calculateDistanceMatrix(
            String originAddress, String destAddress, String mode) {
        try {
            String travelMode = (mode != null && !mode.isEmpty()) ? mode : "driving";

            String url = UriComponentsBuilder.fromUriString(DISTANCE_MATRIX_URL)
                    .queryParam("origins", originAddress)
                    .queryParam("destinations", destAddress)
                    .queryParam("mode", travelMode)
                    .queryParam("language", "vi")
                    .queryParam("region", "vn")
                    .queryParam("key", googleMapsApiKey)
                    .build()
                    .encode()
                    .toUriString();

            log.debug("Distance Matrix API URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            log.debug("Distance Matrix API Response: {}", response.getBody());

            JsonNode root = objectMapper.readTree(response.getBody());
            String status = root.get("status").asText();

            if (!"OK".equals(status)) {
                log.warn("Distance Matrix API returned status: {}", status);
                return null;
            }

            JsonNode rows = root.get("rows");
            if (rows == null || !rows.isArray() || rows.isEmpty()) {
                log.warn("No rows in Distance Matrix API response");
                return null;
            }

            JsonNode elements = rows.get(0).get("elements");
            if (elements == null || !elements.isArray() || elements.isEmpty()) {
                log.warn("No elements in Distance Matrix API response");
                return null;
            }

            JsonNode element = elements.get(0);
            String elementStatus = element.get("status").asText();

            if (!"OK".equals(elementStatus)) {
                log.warn("Distance Matrix element status: {}", elementStatus);
                return null;
            }

            JsonNode distanceNode = element.get("distance");
            JsonNode durationNode = element.get("duration");

            if (distanceNode == null || durationNode == null) {
                log.warn("Missing distance or duration in response");
                return null;
            }

            int distanceMeters = distanceNode.get("value").asInt();
            double distanceKm = distanceMeters / 1000.0;
            String distanceText = distanceNode.get("text").asText();

            int durationSeconds = durationNode.get("value").asInt();
            int durationMinutes = durationSeconds / 60;
            String durationText = durationNode.get("text").asText();

            log.info("Distance Matrix result - Distance: {} km, Duration: {} minutes (mode: {})",
                    distanceKm, durationMinutes, travelMode);

            return new DistanceMatrixResult(
                    distanceKm,
                    distanceMeters,
                    distanceText,
                    durationMinutes,
                    durationSeconds,
                    durationText,
                    travelMode);

        } catch (Exception e) {
            log.error("Error calling Distance Matrix API: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Calculate distance and duration between two locations using Google Maps
     * Distance Matrix API
     * This method uses coordinates (lat/lng)
     * 
     * @param originLat Origin latitude
     * @param originLng Origin longitude
     * @param destLat   Destination latitude
     * @param destLng   Destination longitude
     * @param mode      Travel mode: "driving", "walking", "bicycling", "transit"
     *                  (default: "driving")
     * @return DistanceMatrixResult with distance and duration information
     */
    public DistanceMatrixResult calculateDistanceMatrix(
            double originLat, double originLng,
            double destLat, double destLng,
            String mode) {
        try {
            String travelMode = (mode != null && !mode.isEmpty()) ? mode : "driving";

            String url = UriComponentsBuilder.fromUriString(DISTANCE_MATRIX_URL)
                    .queryParam("origins", String.format("%.6f,%.6f", originLat, originLng))
                    .queryParam("destinations", String.format("%.6f,%.6f", destLat, destLng))
                    .queryParam("mode", travelMode)
                    .queryParam("language", "vi")
                    .queryParam("key", googleMapsApiKey)
                    .build()
                    .toUriString();

            log.debug("Distance Matrix API URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            log.debug("Distance Matrix API Response: {}", response.getBody());

            JsonNode root = objectMapper.readTree(response.getBody());
            String status = root.get("status").asText();

            if (!"OK".equals(status)) {
                log.warn("Distance Matrix API returned status: {}", status);
                return null;
            }

            JsonNode rows = root.get("rows");
            if (rows == null || !rows.isArray() || rows.isEmpty()) {
                log.warn("No rows in Distance Matrix API response");
                return null;
            }

            JsonNode elements = rows.get(0).get("elements");
            if (elements == null || !elements.isArray() || elements.isEmpty()) {
                log.warn("No elements in Distance Matrix API response");
                return null;
            }

            JsonNode element = elements.get(0);
            String elementStatus = element.get("status").asText();

            if (!"OK".equals(elementStatus)) {
                log.warn("Distance Matrix element status: {}", elementStatus);
                return null;
            }

            JsonNode distanceNode = element.get("distance");
            JsonNode durationNode = element.get("duration");

            if (distanceNode == null || durationNode == null) {
                log.warn("Missing distance or duration in response");
                return null;
            }

            int distanceMeters = distanceNode.get("value").asInt();
            double distanceKm = distanceMeters / 1000.0;
            String distanceText = distanceNode.get("text").asText();

            int durationSeconds = durationNode.get("value").asInt();
            int durationMinutes = durationSeconds / 60;
            String durationText = durationNode.get("text").asText();

            log.info("Distance Matrix result - Distance: {} km, Duration: {} minutes (mode: {})",
                    distanceKm, durationMinutes, travelMode);

            return new DistanceMatrixResult(
                    distanceKm,
                    distanceMeters,
                    distanceText,
                    durationMinutes,
                    durationSeconds,
                    durationText,
                    travelMode);

        } catch (Exception e) {
            log.error("Error calling Distance Matrix API: {}", e.getMessage(), e);
            return null;
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
