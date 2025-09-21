package com.c1se_01.roomiego.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * The type Pageable custom.
 */
@Setter
@Getter
@Builder
public class PageableCustom {
    @JsonProperty("page")
    private int page;

    @JsonProperty("limit")
    private int limit;

    @JsonProperty("total_record")
    private Long totalRecords;

    @JsonProperty("total_page")
    private Long totalPage;

    /**
     * Instantiates a new Pageable custom.
     */
    public PageableCustom() {
        super();
    }

    /**
     * Instantiates a new Pageable custom.
     *
     * @param page  the page
     * @param limit the limit
     */
    public PageableCustom(final int page, final int limit) {
        super();
        this.page = page;
        this.limit = limit;
    }

    /**
     * Instantiates a new Pageable custom.
     *
     * @param page         the page
     * @param limit        the limit
     * @param totalRecords the total records
     * @param totalPage    the total page
     */
    public PageableCustom(
            final int page, final int limit, final Long totalRecords, final Long totalPage) {
        super();
        this.page = page;
        this.limit = limit;
        this.totalRecords = totalRecords;
        this.totalPage = totalPage;
    }

    /**
     * To pageable pageable.
     *
     * @return the pageable
     */
    public Pageable toPageable() {
        return PageRequest.of(page - 1, limit);
    }
}
