package com.insurance.demo.dto;

import lombok.Data;

@Data
public class ClaimDocumentRequest {

    private Long claimId;

    private String documentName;

    private String documentType;

    private String documentReference;
}