//package com.company.calendar.exceptions.user;
//
//import com.company.calendar.controller.UserController;
//import com.company.calendar.dto.user.UserResponse;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.util.stream.Collectors;
//
//@RestControllerAdvice(basePackageClasses = UserController.class)
//public class UserExceptionHandler {
//
//    @ExceptionHandler(UserAlreadyExistsException.class)
//    public ResponseEntity<UserResponse<Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
//        UserResponse<Object> response = UserResponse.builder()
//                .success(false)
//                .message(ex.getMessage())
//                .build();
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//    }
//
//    @ExceptionHandler(UserNotFoundException.class)
//    public ResponseEntity<UserResponse<Object>> handleUserNotFound(UserNotFoundException ex) {
//        UserResponse<Object> response = UserResponse.builder()
//                .success(false)
//                .message(ex.getMessage())
//                .build();
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<UserResponse<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
//        String errorMessages = ex.getBindingResult().getFieldErrors()
//                .stream()
//                .map(err -> err.getField() + ": " + err.getDefaultMessage())
//                .collect(Collectors.joining("; "));
//
//        UserResponse<Object> response = UserResponse.builder()
//                .success(false)
//                .message("Validation failed: " + errorMessages)
//                .build();
//
//        return ResponseEntity.badRequest().body(response);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<UserResponse<Object>> handleGeneric(Exception ex) {
//        UserResponse<Object> response = UserResponse.builder()
//                .success(false)
//                .message("Unexpected error: " + ex.getMessage())
//                .build();
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//    }
//}
