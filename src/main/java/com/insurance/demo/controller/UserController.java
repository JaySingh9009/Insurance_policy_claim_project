package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.insurance.demo.dto.CreateAgentRequest;
import com.insurance.demo.dto.UserResponse;
import com.insurance.demo.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/agents")
    public ResponseEntity<UserResponse>
    createAgent(
            @RequestBody
            CreateAgentRequest request) {

        return ResponseEntity.ok(
                userService.createAgent(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponse>>
    getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/activate")
    public ResponseEntity<UserResponse>
    activateUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.activateUser(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse>
    deactivateUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.deactivateUser(id));
    }
}