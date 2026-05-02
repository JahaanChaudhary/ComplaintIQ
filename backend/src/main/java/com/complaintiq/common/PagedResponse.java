package com.complaintiq.common;
import lombok.*;
import org.springframework.data.domain.Page;
import java.util.List;
@Getter @Builder
public class PagedResponse<T> {
    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    private final boolean isFirst;
    private final boolean isLast;
    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder().content(page.getContent()).pageNumber(page.getNumber())
            .pageSize(page.getSize()).totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages()).isFirst(page.isFirst()).isLast(page.isLast()).build();
    }
    public static <T> PagedResponse<T> from(Page<?> page, List<T> mappedContent) {
        return PagedResponse.<T>builder().content(mappedContent).pageNumber(page.getNumber())
            .pageSize(page.getSize()).totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages()).isFirst(page.isFirst()).isLast(page.isLast()).build();
    }
}
