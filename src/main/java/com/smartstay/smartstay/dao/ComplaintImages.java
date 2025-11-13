package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.*;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ComplaintImages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String imageUrl;
    private String createdBy;

    @ManyToOne
    @JoinColumn(name = "complaint_id")
    private ComplaintsV1 complaints;
}
