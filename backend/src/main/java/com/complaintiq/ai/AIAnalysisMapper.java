package com.complaintiq.ai;
import com.complaintiq.complaint.dto.ComplaintResponseDTO;
import org.mapstruct.*;
@Mapper(componentModel="spring")
public interface AIAnalysisMapper {
    @Mapping(target="urgency", expression="java(analysis.getUrgency().name())")
    @Mapping(target="category", expression="java(analysis.getCategory().name())")
    @Mapping(target="sentiment", expression="java(analysis.getSentiment().name())")
    @Mapping(target="intent", expression="java(analysis.getIntent().name())")
    ComplaintResponseDTO.AIAnalysisInfo toAIAnalysisInfo(AIAnalysis analysis);
}
