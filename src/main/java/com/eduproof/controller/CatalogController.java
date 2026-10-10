package com.eduproof.controller;

import com.eduproof.model.CertificateType;
import com.eduproof.model.Department;
import com.eduproof.repository.CertificateTypeRepository;
import com.eduproof.repository.DepartmentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final DepartmentRepository departmentRepository;
    private final CertificateTypeRepository certificateTypeRepository;

    public CatalogController(DepartmentRepository departmentRepository,
                             CertificateTypeRepository certificateTypeRepository) {
        this.departmentRepository = departmentRepository;
        this.certificateTypeRepository = certificateTypeRepository;
    }

    @GetMapping("/departments")
    public List<Department> departments() {
        return departmentRepository.findAll();
    }

    @GetMapping("/certificate-types")
    public List<CertificateType> certificateTypes() {
        return certificateTypeRepository.findAll();
    }
}
