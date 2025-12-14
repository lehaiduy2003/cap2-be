package com.c1se_01.roomiego.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.c1se_01.roomiego.dto.RoomDTO;
import com.c1se_01.roomiego.dto.UserDetailDTO;
import com.c1se_01.roomiego.dto.common.FilterParam;
import com.c1se_01.roomiego.service.impl.GoogleMapsService;
import com.c1se_01.roomiego.service.RoomService;
import com.c1se_01.roomiego.service.UserProfileService;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

/**
 * MCP Tool Controller
 * Provides endpoints for external services (like RAG) to fetch owner and
 * property details
 * These endpoints are designed to be called by AI agents/tools
 */
@RestController
@RequestMapping("/api/mcp/tools")
@Slf4j
public class McpToolController {

  private final UserProfileService userProfileService;
  private final RoomService roomService;
  private final GoogleMapsService googleMapsService;

  public McpToolController(UserProfileService userProfileService, RoomService roomService,
      GoogleMapsService googleMapsService) {
    this.userProfileService = userProfileService;
    this.roomService = roomService;
    this.googleMapsService = googleMapsService;
  }

  /**
   * Get owner/user details by ID
   * Endpoint: GET /api/mcp/tools/owner/{ownerId}
   * 
   * @param ownerId The owner/user ID
   * @return Owner details including name, email, phone, etc.
   */
  @GetMapping("/owner/{ownerId}")
  public ResponseEntity<UserDetailDTO> getOwnerDetails(
      @PathVariable Long ownerId) {

    log.info("[MCP Tool] Fetching owner details for ID: {}", ownerId);

    try {
      UserDetailDTO owner = userProfileService.getUserProfile(ownerId);

      if (owner == null) {
        log.warn("[MCP Tool] Owner not found: {}", ownerId);
        return ResponseEntity.notFound().build();
      }

      log.info("[MCP Tool] Successfully retrieved owner: {}", owner);
      return ResponseEntity.ok(owner);

    } catch (Exception e) {
      log.error("[MCP Tool] Error fetching owner {}: {}", ownerId, e.getMessage());
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Get property/room details by ID
   * Endpoint: GET /api/mcp/tools/property/{propertyId}
   * 
   * @param propertyId The property/room ID
   * @return Property details including title, price, address, amenities, etc.
   */
  @GetMapping("/property/{propertyId}")
  public ResponseEntity<RoomDTO> getPropertyDetails(
      @PathVariable Long propertyId) {

    log.info("[MCP Tool] Fetching property details for ID: {}", propertyId);

    try {
      RoomDTO property = roomService.getRoomById(propertyId);

      if (property == null) {
        log.warn("[MCP Tool] Property not found: {}", propertyId);
        return ResponseEntity.notFound().build();
      }

      log.info("[MCP Tool] Successfully retrieved property: {}", property.getTitle());
      return ResponseEntity.ok(property);

    } catch (Exception e) {
      log.error("[MCP Tool] Error fetching property {}: {}", propertyId, e.getMessage());
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Search rooms by criteria
   * Endpoint: GET /api/mcp/tools/property/search
   * Query parameters: search, filter, page, size, sort, order
   * 
   * @param filterParam Filter parameters for searching rooms
   * @return List of rooms matching the criteria
   */
  @GetMapping("/property/search")
  public ResponseEntity<List<RoomDTO>> searchRooms(FilterParam filterParam) {

    log.info("[MCP Tool] Searching rooms with filter: {}", filterParam);

    try {
      List<RoomDTO> rooms = roomService.getAllRooms(filterParam);

      log.info("[MCP Tool] Found {} rooms matching criteria", rooms.size());
      return ResponseEntity.ok(rooms);

    } catch (Exception e) {
      log.error("[MCP Tool] Error searching rooms: {}", e.getMessage());
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Search rooms nearby a specific address
   * Endpoint: GET /api/mcp/tools/property/nearby
   * Query parameters: address, radius (optional, default 500m)
   *
   * @param address The address to search around
   * @param radius  Search radius in meters (optional, default 500)
   * @return List of rooms near the specified address
   */
  @GetMapping("/property/nearby")
  public ResponseEntity<List<RoomDTO>> searchNearbyRooms(
      @RequestParam String address,
      @RequestParam(defaultValue = "500") Double radius) {

    log.info("[MCP Tool] Searching rooms near address: {} within {} meters", address, radius);

    try {
      // Get coordinates for the address
      var locationData = googleMapsService.geocodeAddress(address);
      if (locationData == null) {
        log.warn("[MCP Tool] Could not geocode address: {}", address);
        return ResponseEntity.badRequest().build();
      }

      // Create filter param with location-based search
      FilterParam filterParam = new FilterParam();
      // Build filter string for location search
      String locationFilter = String.format("location:nearby:%.6f;%.6f;%.0f",
          locationData.getLatitude(), locationData.getLongitude(), radius);
      filterParam.setFilter(locationFilter);
      filterParam.setPage(0);
      filterParam.setSize(20); // Limit results

      List<RoomDTO> rooms = roomService.getAllRooms(filterParam);

      log.info("[MCP Tool] Found {} rooms near {}", rooms.size(), address);
      return ResponseEntity.ok(rooms);

    } catch (Exception e) {
      log.error("[MCP Tool] Error searching nearby rooms for address {}: {}", address, e.getMessage());
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Calculate distance between two properties or addresses
   * Endpoint: GET /api/mcp/tools/distance
   * Query parameters:
   * - fromPropertyId: Source property ID (optional if fromAddress provided)
   * - toPropertyId: Destination property ID (optional if toAddress provided)
   * - fromAddress: Source address (optional if fromPropertyId provided)
   * - toAddress: Destination address (optional if toAddress provided)
   * - mode: Travel mode - "driving" (default), "walking", "bicycling", "transit"
   *
   * @return Distance information including kilometers, meters, duration, and
   *         travel mode
   */
  @GetMapping("/distance")
  public ResponseEntity<?> calculateDistance(
      @RequestParam(required = false) Long fromPropertyId,
      @RequestParam(required = false) Long toPropertyId,
      @RequestParam(required = false) String fromAddress,
      @RequestParam(required = false) String toAddress,
      @RequestParam(required = false, defaultValue = "driving") String mode) {
    try {

      log.info(
          "[MCP Tool] Calculating distance - From Property: {}, To Property: {}, From Address: {}, To Address: {}, Mode: {}",
          fromPropertyId, toPropertyId, fromAddress, toAddress, mode);

      String originAddress = null;
      String destinationAddress = null;
      Double startLat = null;
      Double startLng = null;
      Double endLat = null;
      Double endLng = null;

      // Get origin - geocode address to coordinates for accuracy
      if (fromAddress != null && !fromAddress.isEmpty()) {
        originAddress = fromAddress;

        // Geocode to get accurate coordinates
        var fromLocation = googleMapsService.geocodeAddress(originAddress);
        if (fromLocation == null) {
          return ResponseEntity.badRequest().body("Could not geocode source address");
        }
        startLat = fromLocation.getLatitude();
        startLng = fromLocation.getLongitude();
        originAddress = fromLocation.getFormattedAddress(); // Use geocoded address
      } else if (fromPropertyId != null) {
        RoomDTO fromProperty = roomService.getRoomById(fromPropertyId);
        if (fromProperty == null) {
          return ResponseEntity.badRequest().body("Source property not found");
        }
        startLat = fromProperty.getLatitude();
        startLng = fromProperty.getLongitude();
        originAddress = buildFullAddress(fromProperty);
      } else {
        return ResponseEntity.badRequest().body("Must provide fromPropertyId or fromAddress");
      }

      // Get destination - geocode address to coordinates for accuracy
      if (toAddress != null && !toAddress.isEmpty()) {
        destinationAddress = toAddress;

        // Add "Đà Nẵng" if not present to help with geocoding accuracy
        if (!toAddress.toLowerCase().contains("đà nẵng") &&
            !toAddress.toLowerCase().contains("da nang")) {
          destinationAddress = toAddress + ", Đà Nẵng";
        }

        // Geocode to get accurate coordinates
        var toLocation = googleMapsService.geocodeAddress(destinationAddress);
        if (toLocation == null) {
          return ResponseEntity.badRequest().body("Could not geocode destination address");
        }
        endLat = toLocation.getLatitude();
        endLng = toLocation.getLongitude();
        destinationAddress = toLocation.getFormattedAddress(); // Use geocoded address
      } else if (toPropertyId != null) {
        RoomDTO toProperty = roomService.getRoomById(toPropertyId);
        if (toProperty == null) {
          return ResponseEntity.badRequest().body("Destination property not found");
        }
        endLat = toProperty.getLatitude();
        endLng = toProperty.getLongitude();
        destinationAddress = buildFullAddress(toProperty);
      } else {
        return ResponseEntity.badRequest().body("Must provide toPropertyId or toAddress");
      }

      // Validate coordinates before calling Distance Matrix API
      if (startLat == null || startLng == null || endLat == null || endLng == null) {
        return ResponseEntity.badRequest().body("Could not determine coordinates for one or both locations");
      }

      // Use Google Maps Distance Matrix API with coordinates (more reliable than
      // addresses)
      var distanceResult = googleMapsService.calculateDistanceMatrix(
          startLat, startLng, endLat, endLng, mode);

      if (distanceResult == null) {
        return ResponseEntity.internalServerError().body("Failed to calculate distance using Google Maps API");
      }

      var response = new HashMap<String, Object>();
      response.put("distanceKm", distanceResult.getDistanceKm());
      response.put("distanceMeters", distanceResult.getDistanceMeters());
      response.put("distanceText", distanceResult.getDistanceText());
      response.put("durationMinutes", distanceResult.getDurationMinutes());
      response.put("durationSeconds", distanceResult.getDurationSeconds());
      response.put("durationText", distanceResult.getDurationText());
      response.put("travelMode", distanceResult.getTravelMode());
      response.put("originAddress", originAddress);
      response.put("destinationAddress", destinationAddress);
      if (startLat != null && startLng != null) {
        response.put("fromCoordinates", new double[] { startLat, startLng });
      }
      if (endLat != null && endLng != null) {
        response.put("toCoordinates", new double[] { endLat, endLng });
      }

      log.info("[MCP Tool] Distance calculated: {} km, {} minutes ({})",
          distanceResult.getDistanceKm(), distanceResult.getDurationMinutes(), distanceResult.getTravelMode());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("[MCP Tool] Error calculating distance: {}", e.getMessage());
      return ResponseEntity.internalServerError().body("Error calculating distance: " + e.getMessage());
    }
  }

  /**
   * Build a full address string from RoomDTO fields
   * 
   * @param room The room DTO with address components
   * @return Full address string in Vietnamese format
   */
  private String buildFullAddress(RoomDTO room) {
    StringBuilder address = new StringBuilder();

    // Use location field first (it's usually the most complete)
    if (room.getLocation() != null && !room.getLocation().isEmpty()) {
      return room.getLocation();
    }

    // Otherwise, build from components: addressDetails, street, ward, district,
    // city
    if (room.getAddressDetails() != null && !room.getAddressDetails().isEmpty()) {
      address.append(room.getAddressDetails());
    }

    if (room.getStreet() != null && !room.getStreet().isEmpty()) {
      if (address.length() > 0)
        address.append(", ");
      address.append(room.getStreet());
    }

    if (room.getWard() != null && !room.getWard().isEmpty()) {
      if (address.length() > 0)
        address.append(", ");
      address.append(room.getWard());
    }

    if (room.getDistrict() != null && !room.getDistrict().isEmpty()) {
      if (address.length() > 0)
        address.append(", ");
      address.append(room.getDistrict());
    }

    if (room.getCity() != null && !room.getCity().isEmpty()) {
      if (address.length() > 0)
        address.append(", ");
      address.append(room.getCity());
    }

    return address.toString();
  }
}
