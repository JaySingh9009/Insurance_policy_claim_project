package com.insurance.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClaimDocumentResponse {

    private Long documentId;

    private String documentName;

    private String documentType;

    private String documentReference;
}