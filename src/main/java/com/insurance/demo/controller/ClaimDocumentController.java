package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.insurance.demo.dto.ClaimDocumentRequest;
import com.insurance.demo.dto.ClaimDocumentResponse;
import com.insurance.demo.service.ClaimDocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/claim-documents")
@RequiredArgsConstructor
public class ClaimDocumentController {

    private final ClaimDocumentService
            claimDocumentService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<ClaimDocumentResponse>
    addDocument(
            @RequestBody
            ClaimDocumentRequest request) {

        return ResponseEntity.ok(
                claimDocumentService
                .addDocument(request));
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<
            List<ClaimDocumentResponse>>
    getDocuments(
            @PathVariable
            Long claimId) {

        return ResponseEntity.ok(
                claimDocumentService
                .getDocuments(claimId));
    }
}