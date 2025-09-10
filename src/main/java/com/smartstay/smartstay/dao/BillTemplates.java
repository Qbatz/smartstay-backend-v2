package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BillTemplates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int templateId;
    String hostelLogo;
    String hostelId;
    String mobile;
    String emailId;
    String digitalSignature;
    boolean isTemplateUpdated;
    Date updatedAt;
    Date createdAt;
    String createdBy;
    String updatedBy;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "templates")
    List<BillTemplateType> templateTypes;

}
