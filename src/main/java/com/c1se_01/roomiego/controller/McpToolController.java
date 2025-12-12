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
   * - toAddress: Destination address (optional if toPropertyId provided)
   * - fromLat: Source latitude (optional)
   * - fromLng: Source longitude (optional)
   * - toLat: Destination latitude (optional)
   * - toLng: Destination longitude (optional)
   *
   * @return Distance information including kilometers and meters
   */
  @GetMapping("/distance")
  public ResponseEntity<?> calculateDistance(
      @RequestParam(required = false) Long fromPropertyId,
      @RequestParam(required = false) Long toPropertyId,
      @RequestParam(required = false) String fromAddress,
      @RequestParam(required = false) String toAddress,
      @RequestParam(required = false) Double fromLat,
      @RequestParam(required = false) Double fromLng,
      @RequestParam(required = false) Double toLat,
      @RequestParam(required = false) Double toLng) {

    log.info("[MCP Tool] Calculating distance - From Property: {}, To Property: {}, From Address: {}, To Address: {}",
        fromPropertyId, toPropertyId, fromAddress, toAddress);

    try {
      Double startLat = fromLat;
      Double startLng = fromLng;
      Double endLat = toLat;
      Double endLng = toLng;

      // Get start coordinates
      if (startLat == null || startLng == null) {
        if (fromPropertyId != null) {
          RoomDTO fromProperty = roomService.getRoomById(fromPropertyId);
          if (fromProperty == null) {
            return ResponseEntity.badRequest().body("Source property not found");
          }
          startLat = fromProperty.getLatitude();
          startLng = fromProperty.getLongitude();
        } else if (fromAddress != null && !fromAddress.isEmpty()) {
          var fromLocation = googleMapsService.geocodeAddress(fromAddress);
          if (fromLocation == null) {
            return ResponseEntity.badRequest().body("Could not geocode source address");
          }
          startLat = fromLocation.getLatitude();
          startLng = fromLocation.getLongitude();
        } else {
          return ResponseEntity.badRequest().body("Must provide fromPropertyId, fromAddress, or fromLat/fromLng");
        }
      }

      // Get end coordinates
      if (endLat == null || endLng == null) {
        if (toPropertyId != null) {
          RoomDTO toProperty = roomService.getRoomById(toPropertyId);
          if (toProperty == null) {
            return ResponseEntity.badRequest().body("Destination property not found");
          }
          endLat = toProperty.getLatitude();
          endLng = toProperty.getLongitude();
        } else if (toAddress != null && !toAddress.isEmpty()) {
          var toLocation = googleMapsService.geocodeAddress(toAddress);
          if (toLocation == null) {
            return ResponseEntity.badRequest().body("Could not geocode destination address");
          }
          endLat = toLocation.getLatitude();
          endLng = toLocation.getLongitude();
        } else {
          return ResponseEntity.badRequest().body("Must provide toPropertyId, toAddress, or toLat/toLng");
        }
      }

      // Validate coordinates
      if (startLat == null || startLng == null || endLat == null || endLng == null) {
        return ResponseEntity.badRequest().body("Invalid coordinates");
      }

      // Calculate distance using Haversine formula
      double distance = calculateHaversineDistance(startLat, startLng, endLat, endLng);

      var response = new java.util.HashMap<String, Object>();
      response.put("distanceKm", Math.round(distance * 100.0) / 100.0);
      response.put("distanceMeters", Math.round(distance * 1000.0));
      response.put("fromCoordinates", new double[] { startLat, startLng });
      response.put("toCoordinates", new double[] { endLat, endLng });

      log.info("[MCP Tool] Distance calculated: {} km", distance);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("[MCP Tool] Error calculating distance: {}", e.getMessage());
      return ResponseEntity.internalServerError().body("Error calculating distance: " + e.getMessage());
    }
  }

  /**
   * Calculate distance between two points using Haversine formula
   * 
   * @param lat1 Latitude of first point
   * @param lon1 Longitude of first point
   * @param lat2 Latitude of second point
   * @param lon2 Longitude of second point
   * @return Distance in kilometers
   */
  private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
    final int EARTH_RADIUS_KM = 6371;

    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS_KM * c;
  }
}
