package com.example.employeecrud.controller;

import com.example.employeecrud.errorhandling.ExceptionDetails;
import com.example.employeecrud.errorhandling.RequestException;
import com.example.employeecrud.global.GlobalData;
import com.example.employeecrud.model.Role;
import com.example.employeecrud.model.User;
import com.example.employeecrud.repository.RoleRepository;
import com.example.employeecrud.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

@Controller
public class LoginController {

    private static final int DEFAULT_ROLE_ID = 2;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Displays the login page and clears the cart.
     */
    @GetMapping("/login")
    public String login() {
        GlobalData.cart.clear();
        return "login";
    }

    /**
     * Displays the registration form.
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /**
     * Handles user registration.
     */
    @PostMapping("/register")
    public String registerPost(@ModelAttribute("user") User user, HttpServletRequest request) {
        String rawPassword = user.getPassword();
        user.setPassword(bCryptPasswordEncoder.encode(rawPassword));

        Optional<Role> userRole = roleRepository.findById(DEFAULT_ROLE_ID);
        if (userRole.isEmpty()) {
            throw new RequestException("Role not found with id: " + DEFAULT_ROLE_ID);
        }

        user.setRoles(Collections.singletonList(userRole.get()));
        userRepository.save(user);

        try {
            request.login(user.getEmail(), rawPassword);
        } catch (Exception e) {
            throw new RequestException("Login failed for user: " + user.getEmail(), e);
        }

        return "redirect:/";
    }

    /**
     * Handles custom request exceptions.
     */
    @ExceptionHandler(RequestException.class)
    public String handleRequestException(RequestException e, Model model) {
        model.addAttribute("exceptionDetails", new ExceptionDetails(
                e.getMessage(),
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now()
        ));
        return "error";
    }

    /**
     * Handles any generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e, Model model) {
        model.addAttribute("errorTitle", "Unexpected Error");
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        model.addAttribute("timestamp", ZonedDateTime.now());
        return "error";
    }
}
