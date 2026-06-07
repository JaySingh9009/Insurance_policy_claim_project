package com.insurance.demo.service;

import java.util.List;

import com.insurance.demo.dto.ClaimDocumentRequest;
import com.insurance.demo.dto.ClaimDocumentResponse;

public interface ClaimDocumentService {

    ClaimDocumentResponse addDocument(
            ClaimDocumentRequest request);

    List<ClaimDocumentResponse>
    getDocuments(Long claimId);
}