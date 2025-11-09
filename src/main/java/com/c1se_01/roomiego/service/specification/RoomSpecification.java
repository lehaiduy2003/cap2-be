package com.c1se_01.roomiego.service.specification;

import com.c1se_01.roomiego.dto.common.FilterCondition;
import com.c1se_01.roomiego.dto.common.FilterParam;
import com.c1se_01.roomiego.model.Room;
import com.c1se_01.roomiego.utils.FilterPredicateBuilder;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public class RoomSpecification {

    private RoomSpecification() {}

    public static Specification<Room> buildSpecification(FilterParam filterParam) {
        Specification<Room> spec = Specification.where(null);
        if (filterParam.getSearch() != null && !filterParam.getSearch().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + filterParam.getSearch().toLowerCase() + "%"));
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
            default -> throw new IllegalArgumentException("Unknown field: " + field);
        };
    }
}
