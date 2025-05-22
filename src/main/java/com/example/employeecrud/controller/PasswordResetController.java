package com.example.employeecrud.controller;

import com.example.employeecrud.model.User;
import com.example.employeecrud.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<?> processForgotPassword(@RequestParam("email") String email) {
        passwordResetService.sendResetToken(email);
        return ResponseEntity.ok("Reset link sent to email");
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam("token") String token) {
        try {
            User user = passwordResetService.validateToken(token);
            return ResponseEntity.ok().body("Valid token");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> handleResetPassword(@RequestParam("token") String token,
                                                 @RequestParam("password") String password) {
        try {
            User user = passwordResetService.validateToken(token);
            passwordResetService.updatePassword(user, password);
            return ResponseEntity.ok("Password reset successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed to reset password");
        }
    }
}
