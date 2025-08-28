package com.RestaurantSystem.Infra.Exceptions;

import com.RestaurantSystem.Infra.Exceptions.ExceptionsToThrow.EmailAlreadyConfirmedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> runtimeException(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(EmailAlreadyConfirmedException.class)
    public ResponseEntity<String> handleEmailAlreadyConfirmed(EmailAlreadyConfirmedException e) {
        return ResponseEntity.unprocessableEntity().body(e.getMessage());
    }

//    @ExceptionHandler(DateTimeParseException.class)
//    public ResponseEntity<?> handleParseValidation() {
//        Map<String, String> errors = new HashMap<>();
//        errors.put("dataNascimento", "utilize o formato AAAA-MM-DD");
//        return new ResponseEntity<>(errors, new HttpHeaders(), HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(NoSuchElementException.class)
//    public ResponseEntity<?> handleEntityNotFoundException() {
//        Map<String, String> errors = new HashMap<>();
//        errors.put("pessoa", "nenhum usuário com o ID informado.");
//        return new ResponseEntity<>(errors, new HttpHeaders(), HttpStatus.NOT_FOUND);
//    }
}
