package com.c1se_01.roomiego.service.specification;

import com.c1se_01.roomiego.dto.common.FilterCondition;
import com.c1se_01.roomiego.dto.common.FilterParam;
import com.c1se_01.roomiego.model.Room;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomSpecificationTest {

  @Mock
  private Root<Room> root;

  @Mock
  private CriteriaQuery<?> query;

  @Mock
  private CriteriaBuilder cb;

  @Mock
  private Path<String> stringPath;

  @Mock
  private Path<BigDecimal> bigDecimalPath;

  @Mock
  private Path<Float> floatPath;

  @Mock
  private Expression<String> expression;

  @Mock
  private Predicate predicate;

  // Helper method to create FilterParam with conditions
  private FilterParam createFilterParam(String search, List<FilterCondition> conditions) {
    FilterParam param = new FilterParam();
    param.setSearch(search);
    param.setConditions(conditions);
    return param;
  }

  @Test
  void buildSpecification_noSearchNoConditions() {
    // Given
    FilterParam param = createFilterParam(null, null);

    // When
    Specification<Room> spec = RoomSpecification.buildSpecification(param);

    // Then
    assertNotNull(spec);
    Predicate result = spec.toPredicate(root, query, cb);
    assertNull(result); // Specification.where(null) returns null predicate
  }

  @Test
  void buildSpecification_emptySearchNoConditions() {
    // Given
    FilterParam param = createFilterParam("", null);

    // When
    Specification<Room> spec = RoomSpecification.buildSpecification(param);

    // Then
    assertNotNull(spec);
    Predicate result = spec.toPredicate(root, query, cb);
    assertNull(result);
  }

  @Test
  void buildSpecification_withSearchNoConditions() {
    // Given
    FilterParam param = createFilterParam("test", null);
    doReturn(stringPath).when(root).get("title");
    when(cb.lower(stringPath)).thenReturn(expression);
    when(cb.like(expression, "%test%")).thenReturn(predicate);

    // When
    Specification<Room> spec = RoomSpecification.buildSpecification(param);

    // Then
    assertNotNull(spec);
    Predicate result = spec.toPredicate(root, query, cb);
    assertNotNull(result);
    verify(cb).like(expression, "%test%");
  }

  @Test
  void buildSpecification_noSearchWithEmptyConditions() {
    // Given
    FilterParam param = createFilterParam(null, List.of());

    // When
    Specification<Room> spec = RoomSpecification.buildSpecification(param);

    // Then
    assertNotNull(spec);
    Predicate result = spec.toPredicate(root, query, cb);
    assertNull(result);
  }

  @Test
  void buildSpecification_noSearchWithPriceCondition() {
    // Given
    FilterParam param = createFilterParam(null, List.of(new FilterCondition("price", ":", "100")));
    doReturn(bigDecimalPath).when(root).get("price");
    when(cb.equal(bigDecimalPath, new BigDecimal("100"))).thenReturn(predicate);

    // When
    Specification<Room> spec = RoomSpecification.buildSpecification(param);

    // Then
    assertNotNull(spec);
    Predicate result = spec.toPredicate(root, query, cb);
    assertNotNull(result);
    verify(cb).equal(bigDecimalPath, new BigDecimal("100"));
  }

  @Test
  void buildSpecification_noSearchWithSizeCondition() {
    // Given
    FilterParam param = createFilterParam(null, List.of(new FilterCondition("size", ">", "50.0")));
    doReturn(floatPath).when(root).get("roomSize");
    when(cb.greaterThan(floatPath, 50.0f)).thenReturn(predicate);

    // When
    Specification<Room> spec = RoomSpecification.buildSpecification(param);

    // Then
    assertNotNull(spec);
    Predicate result = spec.toPredicate(root, query, cb);
    assertNotNull(result);
    verify(cb).greaterThan(floatPath, 50.0f);
  }

  @Test
  void buildSpecification_noSearchWithCityCondition() {
    // Given
    FilterParam param = createFilterParam(null, List.of(new FilterCondition("city", ":", "Hanoi")));
    doReturn(stringPath).when(root).get("city");
    when(cb.lower(stringPath)).thenReturn(expression);
    when(cb.equal(expression, "hanoi")).thenReturn(predicate);

    // When
    Specification<Room> spec = RoomSpecification.buildSpecification(param);

    // Then
    assertNotNull(spec);
    Predicate result = spec.toPredicate(root, query, cb);
    assertNotNull(result);
    verify(cb).equal(expression, "hanoi");
  }

  @Test
  void buildSpecification_noSearchWithDistrictCondition() {
    // Given
    FilterParam param = createFilterParam(null, List.of(new FilterCondition("district", ":", "Ba Dinh")));
    doReturn(stringPath).when(root).get("district");
    when(cb.lower(stringPath)).thenReturn(expression);
    when(cb.equal(expression, "ba dinh")).thenReturn(predicate);

    // When
    Specification<Room> spec = RoomSpecification.buildSpecification(param);

    // Then
    assertNotNull(spec);
    Predicate result = spec.toPredicate(root, query, cb);
    assertNotNull(result);
    verify(cb).equal(expression, "ba dinh");
  }

  @Test
  void buildSpecification_noSearchWithWardCondition() {
    // Given
    FilterParam param = createFilterParam(null, List.of(new FilterCondition("ward", ":", "Ward 1")));
    doReturn(stringPath).when(root).get("ward");
    when(cb.lower(stringPath)).thenReturn(expression);
    when(cb.equal(expression, "ward 1")).thenReturn(predicate);

    // When
    Specification<Room> spec = RoomSpecification.buildSpecification(param);

    // Then
    assertNotNull(spec);
    Predicate result = spec.toPredicate(root, query, cb);
    assertNotNull(result);
    verify(cb).equal(expression, "ward 1");
  }

  @Test
  void buildSpecification_noSearchWithStreetCondition() {
    // Given
    FilterParam param = createFilterParam(null, List.of(new FilterCondition("street", ":", "Main St")));
    doReturn(stringPath).when(root).get("street");
    when(cb.lower(stringPath)).thenReturn(expression);
    when(cb.equal(expression, "main st")).thenReturn(predicate);

    // When
    Specification<Room> spec = RoomSpecification.buildSpecification(param);

    // Then
    assertNotNull(spec);
    Predicate result = spec.toPredicate(root, query, cb);
    assertNotNull(result);
    verify(cb).equal(expression, "main st");
  }

  @Test
  void buildSpecification_withSearchAndConditions() {
    // Given
    FilterParam param = createFilterParam("room", List.of(new FilterCondition("price", ":<", "200")));
    doReturn(stringPath).when(root).get("title");
    when(cb.lower(stringPath)).thenReturn(expression);
    when(cb.like(expression, "%room%")).thenReturn(predicate);
    @SuppressWarnings("unchecked")
    Path<BigDecimal> pricePathLocal = mock(Path.class);
    doReturn(pricePathLocal).when(root).get("price");
    Predicate pricePredicate = mock(Predicate.class);
    when(cb.lessThanOrEqualTo(pricePathLocal, new BigDecimal("200"))).thenReturn(pricePredicate);
    when(cb.and(predicate, pricePredicate)).thenReturn(mock(Predicate.class));

    // When
    Specification<Room> spec = RoomSpecification.buildSpecification(param);

    // Then
    assertNotNull(spec);
    Predicate result = spec.toPredicate(root, query, cb);
    assertNotNull(result);
    verify(cb).like(expression, "%room%");
    verify(cb).lessThanOrEqualTo(pricePathLocal, new BigDecimal("200"));
    verify(cb).and(predicate, pricePredicate);
  }

  @Test
  void buildSpecification_multipleConditions() {
    // Given
    FilterParam param = createFilterParam(null, List.of(
        new FilterCondition("price", ">", "100"),
        new FilterCondition("city", ":", "Hanoi")));
    @SuppressWarnings("unchecked")
    Path<BigDecimal> pricePathLocal = mock(Path.class);
    @SuppressWarnings("unchecked")
    Path<String> cityPathLocal = mock(Path.class);
    doReturn(pricePathLocal).when(root).get("price");
    doReturn(cityPathLocal).when(root).get("city");
    Predicate pricePred = mock(Predicate.class);
    Predicate cityPred = mock(Predicate.class);
    Predicate combinedPred = mock(Predicate.class);
    when(cb.greaterThan(pricePathLocal, new BigDecimal("100"))).thenReturn(pricePred);
    when(cb.lower(cityPathLocal)).thenReturn(expression);
    when(cb.equal(expression, "hanoi")).thenReturn(cityPred);
    when(cb.and(pricePred, cityPred)).thenReturn(combinedPred);

    // When
    Specification<Room> spec = RoomSpecification.buildSpecification(param);

    // Then
    assertNotNull(spec);
    Predicate result = spec.toPredicate(root, query, cb);
    assertNotNull(result);
    verify(cb).greaterThan(pricePathLocal, new BigDecimal("100"));
    verify(cb).equal(expression, "hanoi");
    verify(cb).and(pricePred, cityPred);
  }

  @Test
  void buildSpecification_invalidField_throwsException() {
    // Given
    FilterParam param = createFilterParam(null, List.of(new FilterCondition("invalidField", ":", "value")));

    // When & Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> RoomSpecification.buildSpecification(param));
    assertEquals("Unknown field: invalidField", exception.getMessage());
  }

  @Test
  void buildSpecification_invalidOperatorForStringField_throwsException() {
    // Given
    FilterParam param = createFilterParam(null, List.of(new FilterCondition("city", ">", "Hanoi")));

    // When & Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> RoomSpecification.buildSpecification(param));
    assertEquals("Invalid operator for city: >", exception.getMessage());
  }

  @Test
  void buildSpecification_invalidOperatorForRangeField_throwsException() {
    // Given
    FilterParam param = createFilterParam(null, List.of(new FilterCondition("price", "~", "100")));

    // When & Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> RoomSpecification.buildSpecification(param));
    assertEquals("Invalid operator for price: ~", exception.getMessage());
  }

  @Test
  void buildSpecification_invalidValueForPrice_throwsException() {
    // Given
    FilterParam param = createFilterParam(null, List.of(new FilterCondition("price", ":", "notANumber")));

    // When & Then
    assertThrows(NumberFormatException.class,
        () -> RoomSpecification.buildSpecification(param));
  }

  @Test
  void buildSpecification_invalidValueForSize_throwsException() {
    // Given
    FilterParam param = createFilterParam(null, List.of(new FilterCondition("size", ":", "notANumber")));

    // When & Then
    assertThrows(NumberFormatException.class,
        () -> RoomSpecification.buildSpecification(param));
  }
}