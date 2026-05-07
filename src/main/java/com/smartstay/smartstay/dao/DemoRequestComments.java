package com.smartstay.smartstay.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DemoRequestComments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String comment;
    private String createdByUserType;
    private String createdBy;
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "request_id")
    @JsonIgnore
    private DemoRequest demoRequest;
}
