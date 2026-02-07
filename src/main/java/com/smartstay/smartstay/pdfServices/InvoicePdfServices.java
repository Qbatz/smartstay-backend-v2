package com.smartstay.smartstay.pdfServices;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;

@Service
public class InvoicePdfServices {
    private final TemplateEngine templateEngine;
    @Autowired
    private UploadFileToS3 uploadFileToS3;

    public InvoicePdfServices(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String generatePdf(String invoiceId, String templateName, Context context) {
        String html = templateEngine.process(templateName, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            File pdfFile = FilesConfig.writePdf(outputStream.toByteArray(), "invoice");
            return uploadFileToS3.uploadFileToS3(pdfFile, "invoice");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
