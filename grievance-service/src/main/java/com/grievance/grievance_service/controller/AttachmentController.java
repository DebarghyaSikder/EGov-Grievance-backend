package com.grievance.grievance_service.controller;

import com.grievance.grievance_service.dto.AttachmentResponse;
import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.entity.GrievanceAttachment;
import com.grievance.grievance_service.repository.GrievanceAttachmentRepository;
import com.grievance.grievance_service.service.FileStorageService;
import com.grievance.grievance_service.service.GrievanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/grievances")
@RequiredArgsConstructor
public class AttachmentController {

    private final GrievanceService grievanceService;
    private final GrievanceAttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    @PostMapping("/{grievanceId}/attachments")
    public ResponseEntity<Map<String, Object>> uploadAttachment(
            @PathVariable Long grievanceId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId
    ) {
        Grievance grievance = grievanceService.getGrievanceById(grievanceId);

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

        Map<String, Object> result = Map.of(
                "success", true,
                "message", "Attachment uploaded successfully",
                "attachmentId", attachment.getId(),
                "fileName", attachment.getFileName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{grievanceId}/attachments")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(@PathVariable Long grievanceId) {
        List<GrievanceAttachment> attachments = attachmentRepository.findByGrievanceId(grievanceId);
        List<AttachmentResponse> responses = attachments.stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{grievanceId}/attachments/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable Long grievanceId,
            @PathVariable Long attachmentId
    ) {
        GrievanceAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        if (!attachment.getGrievance().getId().equals(grievanceId)) {
            throw new RuntimeException("Attachment does not belong to this grievance");
        }

        byte[] fileContent = fileStorageService.loadFile(attachment.getFilePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .body(fileContent);
    }

    @DeleteMapping("/{grievanceId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long grievanceId,
            @PathVariable Long attachmentId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        GrievanceAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        if (!attachment.getGrievance().getId().equals(grievanceId)) {
            throw new RuntimeException("Attachment does not belong to this grievance");
        }

        if (!attachment.getGrievance().getCitizenId().equals(userId)) {
            throw new RuntimeException("You can only delete attachments from your own grievances");
        }

        fileStorageService.deleteFile(attachment.getFilePath());
        attachmentRepository.delete(attachment);

        return ResponseEntity.noContent().build();
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