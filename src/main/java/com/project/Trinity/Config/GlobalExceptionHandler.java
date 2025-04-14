package com.project.Trinity.Config;
import com.project.Trinity.Service.UsernameAlreadyExistsException;
import com.project.Trinity.Service.InvalidRefreshTokenException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UsernameAlreadyExistsException.class)
	public ResponseEntity<String> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
	    return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(InvalidRefreshTokenException.class)
	public ResponseEntity<String> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
	    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>("Erişim reddedildi", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return new ResponseEntity<>("Bir hata oluştu: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}