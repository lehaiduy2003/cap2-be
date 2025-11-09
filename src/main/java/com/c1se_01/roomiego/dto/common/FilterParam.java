package com.c1se_01.roomiego.dto.common;

import com.c1se_01.roomiego.annotation.ValidFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.swing.SortOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class FilterParam {
    private String search;
    private Integer page = 0;
    private Integer size = 10;
    private String sort = "id";
    private SortOrder order = SortOrder.ASCENDING;
    @ValidFilter
    private String filter;
    @JsonIgnore()
    private List<FilterCondition> conditions;

    public List<FilterCondition> getConditions() {
        if (conditions == null && filter != null) {
            conditions = parseFilter(filter);
        }
        return conditions;
    }

    // operator is captured in group 2
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^\\s*([^:<>~,]+?)\\s*(:>|:<|:|>|<|~)\\s*(.+)$");

    private List<FilterCondition> parseFilter(String filter) {
        List<FilterCondition> list = new ArrayList<>();
        if (filter == null || filter.isBlank()) return list;
        String[] parts = filter.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            Matcher m = TOKEN_PATTERN.matcher(part);
            if (m.matches()) {
                String field = m.group(1).trim();
                String operator = m.group(2).trim();
                String value = m.group(3).trim();

                if (!field.isEmpty() && !value.isEmpty()) {
                    list.add(new FilterCondition(field, operator, value));
                }
            }
        }
        return list;
    }
}
