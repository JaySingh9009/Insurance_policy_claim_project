//package com.insurance.demo.service;
//
//import org.springframework.web.multipart.MultipartFile;
//
//public interface CloudinaryService {
//
//    /**
//     * Uploads any allowed file (PDF, JPEG, PNG, WEBP) to Cloudinary.
//     * @param file   the file to upload
//     * @param folder the Cloudinary folder to store it in
//     * @return String[] where [0] = secure_url, [1] = public_id
//     */
//    String[] uploadFile(MultipartFile file, String folder);
//
//    /**
//     * Deletes a file from Cloudinary by its public_id.
//     * @param publicId the Cloudinary public_id
//     */
//    void deleteFile(String publicId);
//}

package com.insurance.demo.service;

import com.insurance.demo.enums.DocumentCategory;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    String[] uploadFile(MultipartFile file, String folder, DocumentCategory category);

    void deleteFile(String publicId);
}