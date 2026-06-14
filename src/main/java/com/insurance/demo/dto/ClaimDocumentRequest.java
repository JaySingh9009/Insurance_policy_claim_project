package com.insurance.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClaimDocumentRequest {

    @NotBlank(message = "Document name is required")
    private String documentName;

    @NotBlank(message = "Document type is required")
    private String documentType;

    /**
     * The Cloudinary URL returned from Step 1: POST /api/claim-documents/upload
     * Customers must upload their file first and paste the returned documentUrl here.
     */
    @NotBlank(message = "Document URL is required. Upload the file first via POST /api/claim-documents/upload")
    private String documentUrl;

    /**
     * The Cloudinary publicId returned from Step 1.
     */
    @NotBlank(message = "Public ID is required. Upload the file first via POST /api/claim-documents/upload")
    private String publicId;
}