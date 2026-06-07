package com.insurance.demo.serviceImpl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.insurance.demo.dto.CreateAgentRequest;
import com.insurance.demo.dto.UserResponse;
import com.insurance.demo.entity.User;
import com.insurance.demo.enums.Role;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.repository.UserRepository;
import com.insurance.demo.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl
        implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createAgent(
            CreateAgentRequest request) {

        User agent =
                User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()))
                .mobileNumber(
                        request.getMobileNumber())
                .role(Role.AGENT)
                .active(true)
                .build();

        userRepository.save(agent);

        return mapToResponse(agent);
    }

    @Override
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public UserResponse activateUser(
            Long userId) {

        User user =
                userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User Not Found"));

        user.setActive(true);

        userRepository.save(user);

        return mapToResponse(user);
    }

    @Override
    public UserResponse deactivateUser(
            Long userId) {

        User user =
                userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User Not Found"));

        user.setActive(false);

        userRepository.save(user);

        return mapToResponse(user);
    }

    private UserResponse mapToResponse(
            User user) {

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole(),
                user.isActive());
    }
}