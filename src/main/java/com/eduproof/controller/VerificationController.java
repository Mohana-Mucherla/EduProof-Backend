package com.eduproof.controller;

import com.eduproof.model.Certificate;
import com.eduproof.model.Student;
import com.eduproof.model.VerificationLog;
import com.eduproof.repository.CertificateRepository;
import com.eduproof.repository.VerificationLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VerificationController {

    private final CertificateRepository certificateRepository;
    private final VerificationLogRepository verificationLogRepository;

    public VerificationController(CertificateRepository certificateRepository,
                                  VerificationLogRepository verificationLogRepository) {
        this.certificateRepository = certificateRepository;
        this.verificationLogRepository = verificationLogRepository;
    }

    @GetMapping("/verify/{code}")
    public Map<String, Object> verify(@PathVariable String code,
                                      @RequestParam(value = "verifier", defaultValue = "Public Recruiter / Verifier") String verifier,
                                      HttpServletRequest request) {
        Certificate cert = certificateRepository.findByUniqueCertificateCodeIgnoreCase(code.trim())
                .orElseGet(() -> {
                    List<Certificate> found = certificateRepository.search(code.trim());
                    return found.isEmpty() ? null : found.get(0);
                });
        Map<String, Object> result = new HashMap<>();
        String status = cert == null ? "NOT_FOUND" : cert.getStatus();
        String actualCode = cert != null ? cert.getUniqueCertificateCode() : code.trim();

        VerificationLog log = new VerificationLog();
        log.setCertificate(cert);
        log.setCertificateCode(actualCode);
        log.setVerifierName(verifier);
        log.setIpAddress(request.getRemoteAddr());
        log.setVerificationStatus(status);
        log.setVerifiedAt(Instant.now());
        verificationLogRepository.save(log);

        result.put("status", status);
        result.put("verificationStatus", status);
        result.put("certificateCode", actualCode);
        result.put("uniqueCertificateCode", actualCode);
        result.put("verificationTimestamp", log.getVerifiedAt());
        result.put("verifiedAt", log.getVerifiedAt());
        result.put("collegeName", "Anurag University, Hyderabad");

        if (cert != null) {
            Student student = cert.getStudent();
            result.put("certificateId", cert.getCertificateId());
            result.put("certificateTitle", cert.getTitle());
            result.put("certificateType", cert.getCertificateType() != null ? cert.getCertificateType().getTypeName() : null);
            result.put("issueDate", cert.getIssueDate());
            result.put("cgpa", cert.getCgpa());
            result.put("academicRemarks", cert.getAcademicRemarks());
            result.put("qrCodeBase64", cert.getQrCodeBase64());
            if (student != null) {
                result.put("studentId", student.getStudentId());
                result.put("studentName", student.getName());
                result.put("rollNumber", student.getRollNumber());
                result.put("department", student.getDepartment() != null ? student.getDepartment().getDepartmentName() : null);
                result.put("course", student.getCourse());
                result.put("year", student.getYear());
            }
        }
        return result;
    }

    @GetMapping("/verify/student")
    public Map<String, Object> verifyByStudent(@RequestParam("name") String name,
                                               @RequestParam(value = "verifier", defaultValue = "Recruiter") String verifier,
                                               HttpServletRequest request) {
        String query = name != null ? name.trim() : "";
        List<Certificate> certs = certificateRepository.search(query);
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (Certificate cert : certs) {
            Map<String, Object> view = toCertView(cert);
            list.add(view);

            VerificationLog log = new VerificationLog();
            log.setCertificate(cert);
            log.setCertificateCode(cert.getUniqueCertificateCode());
            log.setVerifierName(verifier);
            log.setIpAddress(request.getRemoteAddr());
            log.setVerificationStatus(cert.getStatus());
            log.setVerifiedAt(Instant.now());
            verificationLogRepository.save(log);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("total", list.size());
        response.put("certificates", list);
        return response;
    }

    private Map<String, Object> toCertView(Certificate cert) {
        Map<String, Object> map = new HashMap<>();
        map.put("certificateId", cert.getCertificateId());
        map.put("uniqueCertificateCode", cert.getUniqueCertificateCode());
        map.put("certificateCode", cert.getUniqueCertificateCode());
        map.put("title", cert.getTitle());
        map.put("certificateTitle", cert.getTitle());
        map.put("status", cert.getStatus());
        map.put("verificationStatus", cert.getStatus());
        map.put("issueDate", cert.getIssueDate());
        map.put("cgpa", cert.getCgpa());
        map.put("qrCodeBase64", cert.getQrCodeBase64());
        map.put("academicRemarks", cert.getAcademicRemarks());
        map.put("certificateType", cert.getCertificateType() != null ? cert.getCertificateType().getTypeName() : null);
        map.put("collegeName", "Anurag University, Hyderabad");
        if (cert.getStudent() != null) {
            map.put("studentId", cert.getStudent().getStudentId());
            map.put("studentName", cert.getStudent().getName());
            map.put("rollNumber", cert.getStudent().getRollNumber());
            map.put("studentRollNumber", cert.getStudent().getRollNumber());
            map.put("department", cert.getStudent().getDepartment() != null ? cert.getStudent().getDepartment().getDepartmentName() : null);
            map.put("departmentName", cert.getStudent().getDepartment() != null ? cert.getStudent().getDepartment().getDepartmentName() : null);
            map.put("course", cert.getStudent().getCourse());
            map.put("year", cert.getStudent().getYear());
        }
        return map;
    }

    @GetMapping("/verification-logs")
    public List<Map<String, Object>> logs() {
        return verificationLogRepository.findAllByOrderByVerifiedAtDesc().stream()
                .map(this::toLogView)
                .toList();
    }

    private Map<String, Object> toLogView(VerificationLog log) {
        Map<String, Object> view = new HashMap<>();
        view.put("logId", log.getLogId());
        view.put("certificateCode", log.getCertificateCode());
        view.put("verifierName", log.getVerifierName());
        view.put("ipAddress", log.getIpAddress());
        view.put("verificationStatus", log.getVerificationStatus());
        view.put("verifiedAt", log.getVerifiedAt());
        return view;
    }
}
