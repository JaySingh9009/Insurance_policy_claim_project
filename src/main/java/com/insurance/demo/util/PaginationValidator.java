package com.insurance.demo.util;

import com.insurance.demo.exception.InvalidPaginationException;
import com.insurance.demo.exception.InvalidSortFieldException;

import java.util.Set;

public class PaginationValidator {

    private static final int MAX_PAGE_SIZE = 100;

    private PaginationValidator() {}

    public static void validate(int page, int size, String sortBy, Set<String> allowedSortFields) {
        if (page < 0) {
            throw new InvalidPaginationException("Page number must be >= 0");
        }
        if (size <= 0) {
            throw new InvalidPaginationException("Page size must be > 0");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException("Page size must not exceed " + MAX_PAGE_SIZE);
        }
        if (sortBy != null && !sortBy.isBlank()) {
            if (allowedSortFields != null && !allowedSortFields.contains(sortBy)) {
                throw new InvalidSortFieldException(
                        "Invalid sort field '" + sortBy + "'. Allowed: " + allowedSortFields);
            }
        }
    }
}
