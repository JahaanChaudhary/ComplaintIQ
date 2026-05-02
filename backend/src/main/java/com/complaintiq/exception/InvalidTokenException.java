package com.complaintiq.exception;
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) { super(message); }
    public InvalidTokenException() { super("Token is invalid or has expired."); }
}
