package com.c1se_01.roomiego.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The type Page custom.
 *
 * @param <T> the type parameter
 */
public class PageCustom<T> {
    @JsonProperty("data")
    private List<T> data;

    @JsonProperty("page")
    private PageableCustom pageableCustom;

    /**
     * Instantiates a new Page custom.
     *
     * @param page           the page
     * @param pageableCustom the pageable custom
     */
    public PageCustom(final List<T> page, final PageableCustom pageableCustom) {
        this.data = page;
        this.pageableCustom = pageableCustom;
    }

    /**
     * Instantiates a new Page custom.
     */
    public PageCustom() {
    }

    /**
     * Gets data.
     *
     * @return the data
     */
    public List<T> getData() {
        return data;
    }

    /**
     * Sets data.
     *
     * @param data the data
     */
    public void setData(final List<T> data) {
        this.data = data;
    }

    /**
     * Gets pageable custom.
     *
     * @return the pageable custom
     */
    public PageableCustom getPageableCustom() {
        return pageableCustom;
    }

    /**
     * Sets pageable custom.
     *
     * @param pageableCustom the pageable custom
     */
    public void setPageableCustom(final PageableCustom pageableCustom) {
        this.pageableCustom = pageableCustom;
    }
}
