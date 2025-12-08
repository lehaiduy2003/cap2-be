package com.c1se_01.roomiego.dto.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort.Direction;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class FilterParamTest {

  @Test
  void testGetConditionsWhenFilterIsNull() {
    FilterParam fp = new FilterParam();
    fp.setFilter(null);
    List<FilterCondition> conditions = fp.getConditions();
    assertNull(conditions);
  }

  @Test
  void testGetConditionsWhenFilterIsEmpty() {
    FilterParam fp = new FilterParam();
    fp.setFilter("");
    List<FilterCondition> conditions = fp.getConditions();
    assertNotNull(conditions);
    assertTrue(conditions.isEmpty());
  }

  @Test
  void testGetConditionsWhenFilterIsBlank() {
    FilterParam fp = new FilterParam();
    fp.setFilter("   ");
    List<FilterCondition> conditions = fp.getConditions();
    assertNotNull(conditions);
    assertTrue(conditions.isEmpty());
  }

  @Test
  void testGetConditionsWithSingleValidCondition() {
    FilterParam fp = new FilterParam();
    fp.setFilter("name:John");
    List<FilterCondition> conditions = fp.getConditions();
    assertNotNull(conditions);
    assertEquals(1, conditions.size());
    FilterCondition fc = conditions.get(0);
    assertEquals("name", fc.field());
    assertEquals(":", fc.operator());
    assertEquals("John", fc.value());
  }

  @Test
  void testGetConditionsWithMultipleValidConditions() {
    FilterParam fp = new FilterParam();
    fp.setFilter("name:John,age>18");
    List<FilterCondition> conditions = fp.getConditions();
    assertNotNull(conditions);
    assertEquals(2, conditions.size());
    FilterCondition fc1 = conditions.get(0);
    assertEquals("name", fc1.field());
    assertEquals(":", fc1.operator());
    assertEquals("John", fc1.value());
    FilterCondition fc2 = conditions.get(1);
    assertEquals("age", fc2.field());
    assertEquals(">", fc2.operator());
    assertEquals("18", fc2.value());
  }

  @Test
  void testGetConditionsWithInvalidCondition() {
    FilterParam fp = new FilterParam();
    fp.setFilter("invalid");
    List<FilterCondition> conditions = fp.getConditions();
    assertNotNull(conditions);
    assertTrue(conditions.isEmpty());
  }

  @Test
  void testGetConditionsWithMixedValidAndInvalid() {
    FilterParam fp = new FilterParam();
    fp.setFilter("name:John,invalid,age>18");
    List<FilterCondition> conditions = fp.getConditions();
    assertNotNull(conditions);
    assertEquals(2, conditions.size());
    FilterCondition fc1 = conditions.get(0);
    assertEquals("name", fc1.field());
    assertEquals(":", fc1.operator());
    assertEquals("John", fc1.value());
    FilterCondition fc2 = conditions.get(1);
    assertEquals("age", fc2.field());
    assertEquals(">", fc2.operator());
    assertEquals("18", fc2.value());
  }

  @Test
  void testGetConditionsWithEmptyValue() {
    FilterParam fp = new FilterParam();
    fp.setFilter("name:");
    List<FilterCondition> conditions = fp.getConditions();
    assertNotNull(conditions);
    assertTrue(conditions.isEmpty());
  }

  @Test
  void testGetConditionsWithEmptyField() {
    FilterParam fp = new FilterParam();
    fp.setFilter(":value");
    List<FilterCondition> conditions = fp.getConditions();
    assertNotNull(conditions);
    assertTrue(conditions.isEmpty());
  }

  @Test
  void testGetConditionsWithTrimming() {
    FilterParam fp = new FilterParam();
    fp.setFilter(" name : John ");
    List<FilterCondition> conditions = fp.getConditions();
    assertNotNull(conditions);
    assertEquals(1, conditions.size());
    FilterCondition fc = conditions.get(0);
    assertEquals("name", fc.field());
    assertEquals(":", fc.operator());
    assertEquals("John", fc.value());
  }

  @Test
  void testGetConditionsCaching() {
    FilterParam fp = new FilterParam();
    fp.setFilter("name:John");
    List<FilterCondition> conditions1 = fp.getConditions();
    List<FilterCondition> conditions2 = fp.getConditions();
    assertSame(conditions1, conditions2);
  }

  @Test
  void testGetConditionsWhenConditionsAlreadySet() {
    FilterParam fp = new FilterParam();
    List<FilterCondition> customConditions = List.of(new FilterCondition("custom", "=", "value"));
    fp.setConditions(customConditions);
    fp.setFilter("name:John"); // This should not parse since conditions is not null
    List<FilterCondition> conditions = fp.getConditions();
    assertSame(customConditions, conditions);
  }

  @Test
  void testGetConditionsWithAllOperators() {
    FilterParam fp = new FilterParam();
    fp.setFilter("field1:val1,field2:>val2,field3:<val3,field4>val4,field5<val5,field6~val6");
    List<FilterCondition> conditions = fp.getConditions();
    assertNotNull(conditions);
    assertEquals(6, conditions.size());
    assertEquals(":", conditions.get(0).operator());
    assertEquals(":>", conditions.get(1).operator());
    assertEquals(":<", conditions.get(2).operator());
    assertEquals(">", conditions.get(3).operator());
    assertEquals("<", conditions.get(4).operator());
    assertEquals("~", conditions.get(5).operator());
  }

  @Test
  void testGetConditionsWithCommaSeparatedEmptyParts() {
    FilterParam fp = new FilterParam();
    fp.setFilter("name:John,,age>18,");
    List<FilterCondition> conditions = fp.getConditions();
    assertNotNull(conditions);
    assertEquals(2, conditions.size());
  }

  @Test
  void testDefaultValues() {
    FilterParam fp = new FilterParam();
    assertEquals(0, fp.getPage());
    assertEquals(10, fp.getSize());
    assertEquals("id", fp.getSort());
    assertEquals(Direction.ASC, fp.getOrder());
    assertNull(fp.getFilter());
    assertNull(fp.getConditions());
  }
}