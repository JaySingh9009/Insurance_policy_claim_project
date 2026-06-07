package com.insurance.demo.service;

import java.util.List;

import com.insurance.demo.dto.CreateAgentRequest;
import com.insurance.demo.dto.UserResponse;

public interface UserService {

    UserResponse createAgent(
            CreateAgentRequest request);

    List<UserResponse> getAllUsers();

    UserResponse activateUser(Long userId);

    UserResponse deactivateUser(Long userId);
}