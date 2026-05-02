package com.complaintiq.resolution;
import com.complaintiq.exception.ResourceNotFoundException;
import com.complaintiq.resolution.dto.ResolutionResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j @Service @RequiredArgsConstructor
public class ResolutionService {
    private final ResolutionRepository resolutionRepository;
    @Transactional(readOnly=true) public ResolutionResponseDTO getByTicketId(String ticketId) {
        return mapToDTO(resolutionRepository.findByComplaintTicketId(ticketId).orElseThrow(() -> new ResourceNotFoundException("Resolution","ticketId",ticketId)));
    }
    @Transactional(readOnly=true) public Double getAverageSatisfactionScore() { Double avg = resolutionRepository.findAverageSatisfactionScore(); return avg != null ? avg : 0.0; }
    @Transactional(readOnly=true) public List<ResolutionResponseDTO> getLowSatisfactionResolutions() { return resolutionRepository.findLowSatisfactionResolutions().stream().map(this::mapToDTO).collect(Collectors.toList()); }
    public ResolutionResponseDTO mapToDTO(Resolution resolution) {
        return ResolutionResponseDTO.builder().id(resolution.getId()).ticketId(resolution.getComplaint() != null ? resolution.getComplaint().getTicketId() : null).resolutionNote(resolution.getResolutionNote()).resolutionType(resolution.getResolutionType()).resolvedByName(resolution.getResolvedBy() != null ? resolution.getResolvedBy().getName() : null).resolvedAt(resolution.getResolvedAt()).satisfactionScore(resolution.getSatisfactionScore()).feedbackComment(resolution.getFeedbackComment()).build();
    }
}
