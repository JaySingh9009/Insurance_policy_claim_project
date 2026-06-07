package com.insurance.demo.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimHistoryResponse {

    private String previousStatus;

    private String newStatus;

    private String remarks;

    private String updatedBy;

    private String updatedDate;
}