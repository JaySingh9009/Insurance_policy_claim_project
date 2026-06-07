package com.insurance.demo.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.insurance.demo.dto.ClaimDocumentRequest;
import com.insurance.demo.dto.ClaimDocumentResponse;
import com.insurance.demo.entity.Claim;
import com.insurance.demo.entity.ClaimDocument;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.repository.ClaimDocumentRepository;
import com.insurance.demo.repository.ClaimRepository;
import com.insurance.demo.service.ClaimDocumentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaimDocumentServiceImpl
        implements ClaimDocumentService {

    private final ClaimRepository claimRepository;

    private final ClaimDocumentRepository
            documentRepository;

    @Override
    public ClaimDocumentResponse addDocument(
            ClaimDocumentRequest request) {

        Claim claim =
                claimRepository.findById(
                        request.getClaimId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Claim Not Found"));

        ClaimDocument document =
                ClaimDocument.builder()
                .documentName(
                        request.getDocumentName())
                .documentType(
                        request.getDocumentType())
                .documentReference(
                        request.getDocumentReference())
                .claim(claim)
                .build();

        documentRepository.save(document);

        return new ClaimDocumentResponse(
                document.getDocumentId(),
                document.getDocumentName(),
                document.getDocumentType(),
                document.getDocumentReference());
    }

    @Override
    public List<ClaimDocumentResponse>
    getDocuments(Long claimId) {

        return documentRepository
                .findByClaimClaimId(claimId)
                .stream()
                .map(doc ->
                        new ClaimDocumentResponse(
                                doc.getDocumentId(),
                                doc.getDocumentName(),
                                doc.getDocumentType(),
                                doc.getDocumentReference()))
                .toList();
    }
}