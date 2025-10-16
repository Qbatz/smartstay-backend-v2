package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillTemplates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int templateId;
    String hostelLogo;
    String hostelId;
    String mobile;
    Long countryCode;
    String emailId;
    String digitalSignature;
    boolean isTemplateUpdated;
    boolean isLogoCustomized;
    boolean isEmailCustomized;
    boolean isMobileCustomized;
    boolean isSignatureCustomized;
    Date updatedAt;
    Date createdAt;
    String createdBy;
    String updatedBy;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "templates", fetch = FetchType.EAGER)
    @ToString.Exclude
    List<BillTemplateType> templateTypes;

}
