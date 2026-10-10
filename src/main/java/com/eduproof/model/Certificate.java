package com.eduproof.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "certificates")
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long certificateId;

    @Column(unique = true, nullable = false)
    private String uniqueCertificateCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "certificate_type_id")
    private CertificateType certificateType;

    private String title;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Double cgpa;

    @Column(length = 2000)
    private String academicRemarks;

    private String status = "VALID";
    private String revokeReason;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String qrCodeBase64;

    public Certificate() {}

    public Long getCertificateId() { return certificateId; }
    public void setCertificateId(Long certificateId) { this.certificateId = certificateId; }
    public String getUniqueCertificateCode() { return uniqueCertificateCode; }
    public void setUniqueCertificateCode(String uniqueCertificateCode) { this.uniqueCertificateCode = uniqueCertificateCode; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public CertificateType getCertificateType() { return certificateType; }
    public void setCertificateType(CertificateType certificateType) { this.certificateType = certificateType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public Double getCgpa() { return cgpa; }
    public void setCgpa(Double cgpa) { this.cgpa = cgpa; }
    public String getAcademicRemarks() { return academicRemarks; }
    public void setAcademicRemarks(String academicRemarks) { this.academicRemarks = academicRemarks; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRevokeReason() { return revokeReason; }
    public void setRevokeReason(String revokeReason) { this.revokeReason = revokeReason; }
    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }
}
