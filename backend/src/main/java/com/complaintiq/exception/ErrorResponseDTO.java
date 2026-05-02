package com.complaintiq.exception;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
    private boolean success = false;
    private String errorCode;
    private String message;
    private String path;
    @Builder.Default private LocalDateTime timestamp = LocalDateTime.now();
    private List<FieldErrorDTO> fieldErrors;
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FieldErrorDTO {
        private String field; private String rejectedValue; private String message;
    }
}
