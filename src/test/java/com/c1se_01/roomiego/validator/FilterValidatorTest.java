package com.c1se_01.roomiego.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FilterValidatorTest {

  private FilterValidator validator;

  @BeforeEach
  public void setUp() {
    validator = new FilterValidator();
  }

  // Happy cases
  @Test
  public void testNullValue() {
    assertTrue(validator.isValid(null, null));
  }

  @Test
  public void testEmptyString() {
    assertTrue(validator.isValid("", null));
  }

  @Test
  public void testValidSingleFilter() {
    assertTrue(validator.isValid("field:value", null));
  }

  @Test
  public void testValidSingleFilterWithSpaces() {
    assertTrue(validator.isValid(" field : value ", null));
  }

  @Test
  public void testValidMultipleFilters() {
    assertTrue(validator.isValid("field1:value1,field2:value2", null));
  }

  @Test
  public void testValidMultipleFiltersWithSpaces() {
    assertTrue(validator.isValid(" field1 : value1 , field2 : value2 ", null));
  }

  @Test
  public void testValidFilterWithGreaterThan() {
    assertTrue(validator.isValid("field>value", null));
  }

  @Test
  public void testValidFilterWithLessThan() {
    assertTrue(validator.isValid("field<value", null));
  }

  @Test
  public void testValidFilterWithGreaterThanOrEqual() {
    assertTrue(validator.isValid("field:>value", null));
  }

  @Test
  public void testValidFilterWithLessThanOrEqual() {
    assertTrue(validator.isValid("field:<value", null));
  }

  @Test
  public void testValidFilterWithTilde() {
    assertTrue(validator.isValid("field~value", null));
  }

  @Test
  public void testValidFilterWithEmptyPart() {
    assertTrue(validator.isValid("field:value,,field2:value2", null));
  }

  // Worse cases
  @Test
  public void testInvalidOperator() {
    assertFalse(validator.isValid("field=value", null));
  }

  @Test
  public void testEmptyField() {
    assertFalse(validator.isValid(":value", null));
  }

  @Test
  public void testEmptyValueInFilter() {
    assertFalse(validator.isValid("field:", null));
  }

  @Test
  public void testEmptyFieldWithSpaces() {
    assertFalse(validator.isValid("   :value", null));
  }

  @Test
  public void testEmptyValueWithSpaces() {
    assertFalse(validator.isValid("field:   ", null));
  }

  @Test
  public void testNoOperator() {
    assertFalse(validator.isValid("fieldvalue", null));
  }

  @Test
  public void testOnlyField() {
    assertFalse(validator.isValid("field", null));
  }

  @Test
  public void testOnlyValue() {
    assertFalse(validator.isValid("value", null));
  }

  @Test
  public void testValidExtraColonInValue() {
    assertTrue(validator.isValid("field:value:extra", null));
  }

  @Test
  public void testValidMultipleCommas() {
    assertTrue(validator.isValid("field:value,,", null));
  }

  @Test
  public void testValidEmptyAfterComma() {
    assertTrue(validator.isValid("field:value,", null));
  }

  @Test
  public void testValidCommaAtStart() {
    assertTrue(validator.isValid(",field:value", null));
  }

  @Test
  public void testValidOnlyComma() {
    assertTrue(validator.isValid(",", null));
  }

  @Test
  public void testInvalidMultipleInvalidParts() {
    assertFalse(validator.isValid("field=value,field2", null));
  }
}