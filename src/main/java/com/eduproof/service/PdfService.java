package com.eduproof.service;

import com.eduproof.model.Certificate;
import com.eduproof.model.Student;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

@Service
public class PdfService {
    public byte[] generate(Certificate certificate) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate(), 48, 48, 48, 48);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 26, new Color(15, 23, 42));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(71, 85, 105));
            Font nameFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 22, new Color(180, 83, 9));
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12, new Color(51, 65, 85));
            Font monoFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 11, new Color(15, 118, 110));

            Student student = certificate.getStudent();

            Paragraph university = new Paragraph("ANURAG UNIVERSITY", titleFont);
            university.setAlignment(Element.ALIGN_CENTER);
            document.add(university);

            Paragraph location = new Paragraph("Hyderabad, Telangana, India  •  Accredited by NAAC 'A+'", subtitleFont);
            location.setAlignment(Element.ALIGN_CENTER);
            location.setSpacingAfter(18);
            document.add(location);

            Paragraph heading = new Paragraph(
                    certificate.getTitle() == null ? "DIGITAL ACADEMIC CERTIFICATE" : certificate.getTitle(),
                    FontFactory.getFont(FontFactory.TIMES_BOLD, 16, new Color(30, 64, 175))
            );
            heading.setAlignment(Element.ALIGN_CENTER);
            heading.setSpacingAfter(16);
            document.add(heading);

            Paragraph certify = new Paragraph("This is to formally certify that", bodyFont);
            certify.setAlignment(Element.ALIGN_CENTER);
            document.add(certify);

            Paragraph name = new Paragraph(student != null ? student.getName() : "Student", nameFont);
            name.setAlignment(Element.ALIGN_CENTER);
            name.setSpacingBefore(8);
            name.setSpacingAfter(8);
            document.add(name);

            String roll = student != null ? student.getRollNumber() : "N/A";
            String cgpa = certificate.getCgpa() != null ? certificate.getCgpa() + " / 10.00" : "N/A";
            Paragraph body = new Paragraph(
                    "bearing Roll Number " + roll + " has been awarded this credential with a CGPA of " + cgpa + ".",
                    bodyFont
            );
            body.setAlignment(Element.ALIGN_CENTER);
            body.setSpacingAfter(16);
            document.add(body);

            Paragraph code = new Paragraph("Certificate ID: " + certificate.getUniqueCertificateCode(), monoFont);
            code.setAlignment(Element.ALIGN_CENTER);
            document.add(code);

            Paragraph issued = new Paragraph("Issued: " + certificate.getIssueDate() + "   Status: " + certificate.getStatus(), subtitleFont);
            issued.setAlignment(Element.ALIGN_CENTER);
            issued.setSpacingBefore(8);
            document.add(issued);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate certificate PDF", e);
        }
    }
}
