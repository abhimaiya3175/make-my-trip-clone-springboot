package com.makemytrip.common.api;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        long totalItems,
        int page,
        int size,
        int totalPages,
        boolean hasNext,
        boolean hasPrev
) {
}
