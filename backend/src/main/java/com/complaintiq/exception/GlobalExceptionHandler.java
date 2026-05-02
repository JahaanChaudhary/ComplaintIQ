package com.complaintiq.exception;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j @RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {} | path: {}", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponseDTO.builder().errorCode("RESOURCE_NOT_FOUND").message(ex.getMessage()).path(request.getRequestURI()).build());
    }
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponseDTO.builder().errorCode("DUPLICATE_RESOURCE").message(ex.getMessage()).path(request.getRequestURI()).build());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponseDTO.FieldErrorDTO> fieldErrors = ex.getBindingResult().getAllErrors().stream().map(error -> {
            if (error instanceof FieldError fe) return ErrorResponseDTO.FieldErrorDTO.builder().field(fe.getField()).rejectedValue(fe.getRejectedValue() != null ? fe.getRejectedValue().toString() : null).message(fe.getDefaultMessage()).build();
            return ErrorResponseDTO.FieldErrorDTO.builder().field(error.getObjectName()).message(error.getDefaultMessage()).build();
        }).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.builder().errorCode("VALIDATION_FAILED").message("Request validation failed.").path(request.getRequestURI()).fieldErrors(fieldErrors).build());
    }
    @ExceptionHandler(ComplaintStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleComplaintState(ComplaintStateException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.builder().errorCode("INVALID_STATE_TRANSITION").message(ex.getMessage()).path(request.getRequestURI()).build());
    }
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponseDTO.builder().errorCode("UNAUTHORIZED").message(ex.getMessage()).path(request.getRequestURI()).build());
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponseDTO.builder().errorCode("INVALID_CREDENTIALS").message("Invalid email or password.").path(request.getRequestURI()).build());
    }
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponseDTO.builder().errorCode("INVALID_TOKEN").message(ex.getMessage()).path(request.getRequestURI()).build());
    }
    @ExceptionHandler({DisabledException.class, LockedException.class})
    public ResponseEntity<ErrorResponseDTO> handleDisabledAccount(RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponseDTO.builder().errorCode("ACCOUNT_DISABLED").message("Your account is disabled or locked.").path(request.getRequestURI()).build());
    }
    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponseDTO> handleForbidden(RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponseDTO.builder().errorCode("FORBIDDEN").message("You do not have permission to perform this action.").path(request.getRequestURI()).build());
    }
    @ExceptionHandler(AIAnalysisException.class)
    public ResponseEntity<ErrorResponseDTO> handleAIAnalysis(AIAnalysisException ex, HttpServletRequest request) {
        log.error("AI analysis error: ticketId={} | {}", ex.getTicketId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponseDTO.builder().errorCode("AI_ANALYSIS_FAILED").message("AI analysis encountered an error. Complaint saved with default classification.").path(request.getRequestURI()).build());
    }
    @ExceptionHandler(AssignmentException.class)
    public ResponseEntity<ErrorResponseDTO> handleAssignment(AssignmentException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO.builder().errorCode("ASSIGNMENT_FAILED").message(ex.getMessage()).path(request.getRequestURI()).build());
    }
    @ExceptionHandler(SLAConfigNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleSlaConfig(SLAConfigNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponseDTO.builder().errorCode("SLA_CONFIG_NOT_FOUND").message(ex.getMessage()).path(request.getRequestURI()).build());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: path={} | error={}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponseDTO.builder().errorCode("INTERNAL_SERVER_ERROR").message("An unexpected error occurred. Please try again later.").path(request.getRequestURI()).build());
    }
}
