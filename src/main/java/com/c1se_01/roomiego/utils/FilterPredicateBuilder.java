package com.c1se_01.roomiego.utils;

import org.springframework.data.jpa.domain.Specification;

/**
 * Utility class for building JPA Specifications based on filter conditions.
 */
public final class FilterPredicateBuilder {
    private FilterPredicateBuilder() {}

    /**
     * Builds a range predicate specification for the given field, operator, and value.
     * @param field the field name
     * @param op the operator (":", ">", "<", ":>", ":<", "~")
     * @param value the value to compare against
     * @return the specification representing the predicate
     * @param <T> the entity type
     * @param <V> the value type, must be Comparable
     */
    public static <T, V extends Comparable<V>> Specification<T> buildRangePredicate(String field, String op, V value) {
        return switch (op) {
            case ":" -> (root, query, cb) -> cb.equal(root.get(field), value);
            case ">" -> (root, query, cb) -> cb.greaterThan(root.get(field), value);
            case "<" -> (root, query, cb) -> cb.lessThan(root.get(field), value);
            case ":>" -> (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(field), value);
            case ":<" -> (root, query, cb) -> cb.lessThanOrEqualTo(root.get(field), value);
            default -> throw new IllegalArgumentException("Invalid operator for " + field + ": " + op);
        };
    }

    /**
     * Builds a string equality predicate specification for the given field, operator, and value.
     * @param field the field name
     * @param op the operator (should be ":")
     * @param value the string value to compare against
     * @return the specification representing the predicate
     * @param <T> the entity type
     */
    public static <T> Specification<T> buildStringEqualPredicate(String field, String op, String value) {
        if (!":".equals(op)) {
            throw new IllegalArgumentException("Invalid operator for " + field + ": " + op);
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get(field)), value.toLowerCase());
    }

    /**
     * Builds a string LIKE predicate specification for the given field, operator, and value.
     * @param field the field name
     * @param op the operator (should be "~")
     * @param value the string value to compare against
     * @return the specification representing the predicate
     * @param <T> the entity type
     */
    public static <T> Specification<T> buildStringLikePredicate(String field, String op, String value) {
        return buildStringLikePredicate(field, op, value, false);
    }

    /**
     * Builds a string LIKE predicate specification for the given field, operator, and value.
     * @param field the field name
     * @param op the operator (should be "~")
     * @param value the string value to compare against
     * @param caseSensitive whether the comparison should be case-sensitive
     * @return the specification representing the predicate
     * @param <T> the entity type
     */
    public static <T> Specification<T> buildStringLikePredicate(String field, String op, String value, boolean caseSensitive) {
        if (!"~".equals(op)) {
            throw new IllegalArgumentException("Invalid operator for " + field + ": " + op);
        }
        if (caseSensitive) {
            return (root, query, cb) -> cb.like(root.get(field), "%" + value + "%");
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }
}
