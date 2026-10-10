package com.eduproof.repository;

import com.eduproof.model.CertificateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateTypeRepository extends JpaRepository<CertificateType, Long> {
    Optional<CertificateType> findByTypeName(String typeName);
}
