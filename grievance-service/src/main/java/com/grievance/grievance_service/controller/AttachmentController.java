package com.grievance.grievance_service.controller;

import com.grievance.grievance_service.dto.AttachmentResponse;
import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.entity.GrievanceAttachment;
import com.grievance.grievance_service.repository.GrievanceAttachmentRepository;
import com.grievance.grievance_service.service.FileStorageService;
import com.grievance.grievance_service.service.GrievanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grievances")
@RequiredArgsConstructor
public class AttachmentController {

    private final GrievanceService grievanceService;
    private final GrievanceAttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    @PostMapping("/{grievanceId}/attachments")
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @PathVariable Long grievanceId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId
    ) {
        Grievance grievance = grievanceService.getGrievanceById(grievanceId);

        // Verify the user owns this grievance
        if (!grievance.getCitizenId().equals(userId)) {
            throw new RuntimeException("You can only upload attachments to your own grievances");
        }

        String filePath = fileStorageService.storeFile(file, grievanceId);

        GrievanceAttachment attachment = GrievanceAttachment.builder()
                .grievance(grievance)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .filePath(filePath)
                .fileSize(file.getSize())
                .build();

        attachmentRepository.save(attachment);

        return ResponseEntity.ok(mapToResponse(attachment));
    }

    @GetMapping("/{grievanceId}/attachments")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(@PathVariable Long grievanceId) {
        List<GrievanceAttachment> attachments = attachmentRepository.findByGrievanceId(grievanceId);
        List<AttachmentResponse> responses = attachments.stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long attachmentId) {
        GrievanceAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        byte[] fileContent = fileStorageService.loadFile(attachment.getFilePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .body(fileContent);
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<String> deleteAttachment(
            @PathVariable Long attachmentId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        GrievanceAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        // Verify the user owns this grievance
        if (!attachment.getGrievance().getCitizenId().equals(userId)) {
            throw new RuntimeException("You can only delete attachments from your own grievances");
        }

        fileStorageService.deleteFile(attachment.getFilePath());
        attachmentRepository.delete(attachment);

        return ResponseEntity.ok("Attachment deleted successfully");
    }

    private AttachmentResponse mapToResponse(GrievanceAttachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .grievanceId(attachment.getGrievance().getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }
}