package com.c1se_01.roomiego.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
class FilterPredicateBuilderTest {

  @Mock
  private Root<Object> root;

  @Mock
  private CriteriaQuery<?> query;

  @Mock
  private CriteriaBuilder cb;

  @Test
  void testBuildRangePredicate_Equal() {
    // Given
    String field = "age";
    String op = ":";
    Integer value = 25;
    Path path = mock(Path.class);
    Predicate predicate = mock(Predicate.class);

    when(root.get(field)).thenReturn(path);
    when(cb.equal(path, value)).thenReturn(predicate);

    // When
    Specification<Object> spec = FilterPredicateBuilder.buildRangePredicate(field, op, value);
    Predicate result = spec.toPredicate(root, query, cb);

    // Then
    assertNotNull(spec);
    assertEquals(predicate, result);
    verify(cb).equal(path, value);
  }

  @Test
  void testBuildRangePredicate_GreaterThan() {
    // Given
    String field = "age";
    String op = ">";
    Integer value = 25;
    Path path = mock(Path.class);
    Predicate predicate = mock(Predicate.class);

    when(root.get(field)).thenReturn(path);
    when(cb.greaterThan(path, value)).thenReturn(predicate);

    // When
    Specification<Object> spec = FilterPredicateBuilder.buildRangePredicate(field, op, value);
    Predicate result = spec.toPredicate(root, query, cb);

    // Then
    assertNotNull(spec);
    assertEquals(predicate, result);
    verify(cb).greaterThan(path, value);
  }

  @Test
  void testBuildRangePredicate_LessThan() {
    // Given
    String field = "age";
    String op = "<";
    Integer value = 25;
    Path path = mock(Path.class);
    Predicate predicate = mock(Predicate.class);

    when(root.get(field)).thenReturn(path);
    when(cb.lessThan(path, value)).thenReturn(predicate);

    // When
    Specification<Object> spec = FilterPredicateBuilder.buildRangePredicate(field, op, value);
    Predicate result = spec.toPredicate(root, query, cb);

    // Then
    assertNotNull(spec);
    assertEquals(predicate, result);
    verify(cb).lessThan(path, value);
  }

  @Test
  void testBuildRangePredicate_GreaterThanOrEqual() {
    // Given
    String field = "age";
    String op = ":>";
    Integer value = 25;
    Path path = mock(Path.class);
    Predicate predicate = mock(Predicate.class);

    when(root.get(field)).thenReturn(path);
    when(cb.greaterThanOrEqualTo(path, value)).thenReturn(predicate);

    // When
    Specification<Object> spec = FilterPredicateBuilder.buildRangePredicate(field, op, value);
    Predicate result = spec.toPredicate(root, query, cb);

    // Then
    assertNotNull(spec);
    assertEquals(predicate, result);
    verify(cb).greaterThanOrEqualTo(path, value);
  }

  @Test
  void testBuildRangePredicate_LessThanOrEqual() {
    // Given
    String field = "age";
    String op = ":<";
    Integer value = 25;
    Path path = mock(Path.class);
    Predicate predicate = mock(Predicate.class);

    when(root.get(field)).thenReturn(path);
    when(cb.lessThanOrEqualTo(path, value)).thenReturn(predicate);

    // When
    Specification<Object> spec = FilterPredicateBuilder.buildRangePredicate(field, op, value);
    Predicate result = spec.toPredicate(root, query, cb);

    // Then
    assertNotNull(spec);
    assertEquals(predicate, result);
    verify(cb).lessThanOrEqualTo(path, value);
  }

  @Test
  void testBuildRangePredicate_InvalidOperator() {
    // When & Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> FilterPredicateBuilder.buildRangePredicate("age", "invalid", 25));
    assertTrue(exception.getMessage().contains("Invalid operator for age: invalid"));
  }

  @Test
  void testBuildStringEqualPredicate_Valid() {
    // Given
    String field = "name";
    String op = ":";
    String value = "Test";
    Path path = mock(Path.class);
    Expression lowerExpr = mock(Expression.class);
    Predicate predicate = mock(Predicate.class);

    when(root.get(field)).thenReturn(path);
    when(cb.lower(path)).thenReturn(lowerExpr);
    when(cb.equal(lowerExpr, value.toLowerCase())).thenReturn(predicate);

    // When
    Specification<Object> spec = FilterPredicateBuilder.buildStringEqualPredicate(field, op, value);
    Predicate result = spec.toPredicate(root, query, cb);

    // Then
    assertNotNull(spec);
    assertEquals(predicate, result);
    verify(cb).lower(path);
    verify(cb).equal(lowerExpr, "test");
  }

  @Test
  void testBuildStringEqualPredicate_InvalidOperator() {
    // When & Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> FilterPredicateBuilder.buildStringEqualPredicate("name", "~", "test"));
    assertTrue(exception.getMessage().contains("Invalid operator for name: ~"));
  }

  @Test
  void testBuildStringLikePredicate_Valid_CaseInsensitive() {
    // Given
    String field = "name";
    String op = "~";
    String value = "Test";
    Path path = mock(Path.class);
    Expression lowerExpr = mock(Expression.class);
    Predicate predicate = mock(Predicate.class);

    when(root.get(field)).thenReturn(path);
    when(cb.lower(path)).thenReturn(lowerExpr);
    when(cb.like(lowerExpr, "%" + value.toLowerCase() + "%")).thenReturn(predicate);

    // When
    Specification<Object> spec = FilterPredicateBuilder.buildStringLikePredicate(field, op, value);
    Predicate result = spec.toPredicate(root, query, cb);

    // Then
    assertNotNull(spec);
    assertEquals(predicate, result);
    verify(cb).lower(path);
    verify(cb).like(lowerExpr, "%test%");
  }

  @Test
  void testBuildStringLikePredicate_Valid_CaseSensitive_True() {
    // Given
    String field = "name";
    String op = "~";
    String value = "Test";
    boolean caseSensitive = true;
    Path path = mock(Path.class);
    Predicate predicate = mock(Predicate.class);

    when(root.get(field)).thenReturn(path);
    when(cb.like(path, "%" + value + "%")).thenReturn(predicate);

    // When
    Specification<Object> spec = FilterPredicateBuilder.buildStringLikePredicate(field, op, value, caseSensitive);
    Predicate result = spec.toPredicate(root, query, cb);

    // Then
    assertNotNull(spec);
    assertEquals(predicate, result);
    verify(cb, never()).lower(any());
    verify(cb).like(path, "%Test%");
  }

  @Test
  void testBuildStringLikePredicate_Valid_CaseSensitive_False() {
    // Given
    String field = "name";
    String op = "~";
    String value = "Test";
    boolean caseSensitive = false;
    Path path = mock(Path.class);
    Expression lowerExpr = mock(Expression.class);
    Predicate predicate = mock(Predicate.class);

    when(root.get(field)).thenReturn(path);
    when(cb.lower(path)).thenReturn(lowerExpr);
    when(cb.like(lowerExpr, "%" + value.toLowerCase() + "%")).thenReturn(predicate);

    // When
    Specification<Object> spec = FilterPredicateBuilder.buildStringLikePredicate(field, op, value, caseSensitive);
    Predicate result = spec.toPredicate(root, query, cb);

    // Then
    assertNotNull(spec);
    assertEquals(predicate, result);
    verify(cb).lower(path);
    verify(cb).like(lowerExpr, "%test%");
  }

  @Test
  void testBuildStringLikePredicate_InvalidOperator() {
    // When & Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> FilterPredicateBuilder.buildStringLikePredicate("name", ":", "test"));
    assertTrue(exception.getMessage().contains("Invalid operator for name: :"));
  }
}