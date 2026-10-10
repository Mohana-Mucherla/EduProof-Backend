package com.eduproof.controller;

import com.eduproof.dto.ApiError;
import com.eduproof.dto.CertificateResponse;
import com.eduproof.model.Certificate;
import com.eduproof.model.CertificateType;
import com.eduproof.model.Student;
import com.eduproof.model.User;
import com.eduproof.repository.CertificateRepository;
import com.eduproof.repository.CertificateTypeRepository;
import com.eduproof.repository.StudentRepository;
import com.eduproof.security.RequestAuth;
import com.eduproof.service.PdfService;
import com.eduproof.service.QrCodeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CertificateController {

    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;
    private final CertificateTypeRepository certificateTypeRepository;
    private final QrCodeService qrCodeService;
    private final PdfService pdfService;
    private final RequestAuth requestAuth;

    public CertificateController(CertificateRepository certificateRepository,
                                 StudentRepository studentRepository,
                                 CertificateTypeRepository certificateTypeRepository,
                                 QrCodeService qrCodeService,
                                 PdfService pdfService,
                                 RequestAuth requestAuth) {
        this.certificateRepository = certificateRepository;
        this.studentRepository = studentRepository;
        this.certificateTypeRepository = certificateTypeRepository;
        this.qrCodeService = qrCodeService;
        this.pdfService = pdfService;
        this.requestAuth = requestAuth;
    }

    @GetMapping("/certificates")
    public List<CertificateResponse> list(@RequestParam(value = "search", required = false) String search) {
        User user = requestAuth.currentUser();
        if (user != null && !requestAuth.isAdmin()) {
            return studentRepository.findByEmailIgnoreCase(user.getEmail())
                    .map(student -> certificateRepository.findByStudentOrderByIssueDateDesc(student)
                            .stream().map(CertificateResponse::from).toList())
                    .orElseGet(List::of);
        }
        List<Certificate> certs = (search == null || search.isBlank())
                ? certificateRepository.findAllByOrderByCertificateIdDesc()
                : certificateRepository.search(search.trim());
        return certs.stream().map(CertificateResponse::from).toList();
    }

    @GetMapping("/certificates/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        Certificate cert = certificateRepository.findById(id).orElse(null);
        if (cert == null) {
            return ResponseEntity.status(404).body(new ApiError("Certificate not found."));
        }
        return ResponseEntity.ok(CertificateResponse.from(cert));
    }

    @GetMapping("/certificates/student/{studentId}")
    public List<CertificateResponse> byStudent(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));
        return certificateRepository.findByStudentOrderByIssueDateDesc(student)
                .stream().map(CertificateResponse::from).toList();
    }

    @PostMapping("/certificates")
    public ResponseEntity<?> issue(@RequestBody Map<String, Object> body) {
        Long studentId = toLong(body.get("studentId"));
        Long typeId = toLong(body.get("certificateTypeId"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));
        CertificateType type = certificateTypeRepository.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("Certificate type not found."));

        Certificate cert = new Certificate();
        cert.setStudent(student);
        cert.setCertificateType(type);
        cert.setTitle(str(body.get("title")));
        cert.setIssueDate(parseDate(body.get("issueDate")));
        cert.setExpiryDate(parseDate(body.get("expiryDate")));
        cert.setCgpa(toDouble(body.get("cgpa")));
        cert.setAcademicRemarks(str(body.get("description")));
        cert.setStatus("VALID");
        cert.setUniqueCertificateCode(nextCode());
        cert = certificateRepository.save(cert);
        cert.setQrCodeBase64(qrCodeService.pngBase64("http://localhost:5173/verify/" + cert.getUniqueCertificateCode()));
        return ResponseEntity.ok(CertificateResponse.from(certificateRepository.save(cert)));
    }

    @PutMapping("/certificates/{id}/revoke")
    public ResponseEntity<?> revoke(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        Certificate cert = certificateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found."));
        cert.setStatus("REVOKED");
        if (body != null && body.get("reason") != null) {
            cert.setRevokeReason(body.get("reason"));
        }
        return ResponseEntity.ok(CertificateResponse.from(certificateRepository.save(cert)));
    }

    @GetMapping("/certificates/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        Certificate cert = certificateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found."));
        byte[] pdf = pdfService.generate(cert);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cert.getUniqueCertificateCode() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private String nextCode() {
        long seq = certificateRepository.count() + 1;
        return String.format("EDU-CERT-%d-%05d", Year.now().getValue(), seq);
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private Long toLong(Object value) {
        if (value == null) throw new IllegalArgumentException("Required identifier is missing.");
        return Long.parseLong(str(value));
    }

    private Double toDouble(Object value) {
        if (value == null || str(value).isBlank() || "null".equalsIgnoreCase(str(value))) return null;
        return Double.parseDouble(str(value));
    }

    private LocalDate parseDate(Object value) {
        if (value == null || str(value).isBlank() || "null".equalsIgnoreCase(str(value))) return null;
        return LocalDate.parse(str(value));
    }

}
