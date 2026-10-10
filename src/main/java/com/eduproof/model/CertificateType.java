package com.eduproof.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "certificate_types")
public class CertificateType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long certificateTypeId;
    private String typeName;

    public CertificateType() {}

    public Long getCertificateTypeId() { return certificateTypeId; }
    public void setCertificateTypeId(Long certificateTypeId) { this.certificateTypeId = certificateTypeId; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
}
