package com.insurance.demo.service;

import com.insurance.demo.dto.CreateAgentRequest;
import com.insurance.demo.dto.PagedResponse;
import com.insurance.demo.dto.UserResponse;

public interface UserService {
    UserResponse createAgent(CreateAgentRequest request);
    PagedResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDir);
    UserResponse activateUser(Long id);
    UserResponse deactivateUser(Long id, Long requestingUserId);
    UserResponse getUserById(Long id);
}