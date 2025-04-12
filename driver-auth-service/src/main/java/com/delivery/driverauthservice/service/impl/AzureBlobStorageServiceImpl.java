package com.delivery.driverauthservice.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.delivery.driverauthservice.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
public class AzureBlobStorageServiceImpl implements StorageService {

    private final BlobServiceClient blobServiceClient;
    private final String containerName;

    public AzureBlobStorageServiceImpl(
            BlobServiceClient blobServiceClient,
            @Value("${azure.storage.container-name}") String containerName) {

        this.blobServiceClient = blobServiceClient;
        this.containerName = containerName;

        // Ensure container exists
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!containerClient.exists()) {
            containerClient.create();
            log.info("Container '{}' created successfully", containerName);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String path) throws IOException {
        String blobName = path + "/" + generateUniqueFileName(file.getOriginalFilename());
        BlobClient blobClient = getBlobClient(blobName);

        // Set content type
        BlobHttpHeaders headers = new BlobHttpHeaders()
                .setContentType(file.getContentType());

        // Upload the file
        blobClient.upload(file.getInputStream(), file.getSize(), true);
        blobClient.setHttpHeaders(headers);

        return blobClient.getBlobUrl();
    }

    @Override
    public String uploadBytes(byte[] data, String filename, String contentType, String path) throws IOException {
        String blobName = path + "/" + generateUniqueFileName(filename);
        BlobClient blobClient = getBlobClient(blobName);

        // Set content type
        BlobHttpHeaders headers = new BlobHttpHeaders()
                .setContentType(contentType);

        // Upload the data
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            blobClient.upload(inputStream, data.length, true);
            blobClient.setHttpHeaders(headers);
        }

        return blobClient.getBlobUrl();
    }

    @Override
    public byte[] downloadFile(String fileUrl) throws IOException {
        String blobName = extractBlobNameFromUrl(fileUrl);
        BlobClient blobClient = getBlobClient(blobName);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.downloadStream(outputStream);

        return outputStream.toByteArray();
    }

    @Override
    public void deleteFile(String fileUrl) throws IOException {
        String blobName = extractBlobNameFromUrl(fileUrl);
        BlobClient blobClient = getBlobClient(blobName);

        blobClient.delete();
    }

    private BlobClient getBlobClient(String blobName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        return containerClient.getBlobClient(blobName);
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    private String extractBlobNameFromUrl(String fileUrl) {
        // Typical URL format: https://storageaccount.blob.core.windows.net/container/path/to/blob
        String[] parts = fileUrl.split(containerName + "/");
        if (parts.length > 1) {
            return parts[1];
        }
        throw new IllegalArgumentException("Invalid blob URL: " + fileUrl);
    }
}