package com.c1se_01.roomiego.service.specification;

import com.c1se_01.roomiego.dto.common.FilterCondition;
import com.c1se_01.roomiego.dto.common.FilterParam;
import com.c1se_01.roomiego.model.Room;
import com.c1se_01.roomiego.utils.FilterPredicateBuilder;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public class RoomSpecification {

    private RoomSpecification() {
    }

    public static Specification<Room> buildSpecification(FilterParam filterParam) {
        Specification<Room> spec = Specification.where(null);
        if (filterParam.getSearch() != null && !filterParam.getSearch().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")),
                    "%" + filterParam.getSearch().toLowerCase() + "%"));
        }

        List<FilterCondition> conditions = filterParam.getConditions();
        if (conditions != null) {
            for (FilterCondition cond : conditions) {
                spec = spec.and(buildPredicate(cond));
            }
        }
        return spec;
    }

    private static Specification<Room> buildPredicate(FilterCondition cond) {
        String field = cond.field();
        String op = cond.operator();
        String value = cond.value();
        return switch (field) {
            case "price" -> FilterPredicateBuilder.buildRangePredicate("price", op, new BigDecimal(value));
            case "size" -> FilterPredicateBuilder.buildRangePredicate("roomSize", op, Float.parseFloat(value));
            case "city" -> FilterPredicateBuilder.buildStringEqualPredicate("city", op, value);
            case "district" -> FilterPredicateBuilder.buildStringEqualPredicate("district", op, value);
            case "ward" -> FilterPredicateBuilder.buildStringEqualPredicate("ward", op, value);
            case "street" -> FilterPredicateBuilder.buildStringEqualPredicate("street", op, value);
            case "location" -> {
                if (":".equals(op) && value.startsWith("nearby:")) {
                    // Handle location:nearby:lat;lng;radius format
                    String nearbyValue = value.substring("nearby:".length());
                    String[] parts = nearbyValue.split(";");
                    if (parts.length == 3) {
                        double lat = Double.parseDouble(parts[0]);
                        double lng = Double.parseDouble(parts[1]);
                        double radiusKm = Double.parseDouble(parts[2]) / 1000.0; // Convert meters to km
                        yield buildNearbyLocationPredicate(lat, lng, radiusKm);
                    } else {
                        throw new IllegalArgumentException("Invalid location format: " + value);
                    }
                } else if ("nearby".equals(op)) {
                    // Parse lat;lng;radius from value
                    String[] parts = value.split(";");
                    if (parts.length == 3) {
                        double lat = Double.parseDouble(parts[0]);
                        double lng = Double.parseDouble(parts[1]);
                        double radiusKm = Double.parseDouble(parts[2]) / 1000.0; // Convert meters to km
                        yield buildNearbyLocationPredicate(lat, lng, radiusKm);
                    } else {
                        throw new IllegalArgumentException("Invalid location format: " + value);
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported location operator: " + op);
                }
            }
            default -> throw new IllegalArgumentException("Unknown field: " + field);
        };
    }

    private static Specification<Room> buildNearbyLocationPredicate(double centerLat, double centerLng,
            double radiusKm) {
        return (root, query, cb) -> {
            // Approximate bounding box for the radius
            // 1 degree of latitude ≈ 111 km
            // 1 degree of longitude ≈ 111 * cos(lat) km
            double latDelta = radiusKm / 111.0;
            double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(centerLat)));

            double minLat = centerLat - latDelta;
            double maxLat = centerLat + latDelta;
            double minLng = centerLng - lngDelta;
            double maxLng = centerLng + lngDelta;

            var latitude = root.get("latitude").as(Double.class);
            var longitude = root.get("longitude").as(Double.class);

            // Create bounding box predicate
            var latPredicate = cb.between(latitude, minLat, maxLat);
            var lngPredicate = cb.between(longitude, minLng, maxLng);

            return cb.and(latPredicate, lngPredicate);
        };
    }
}
