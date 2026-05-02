package com.complaintiq.analytics.dto;
import lombok.*;
import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintTrendDTO {
    private String period; private List<TrendPoint> dataPoints;
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TrendPoint { private String date; private Long count; }
}
