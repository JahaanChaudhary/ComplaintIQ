package com.complaintiq.analytics.dto;
import lombok.*;
import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HeatmapDTO {
    private List<HeatmapCell> cells;
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class HeatmapCell { private Integer hour; private Long complaintCount; private String label; }
}
