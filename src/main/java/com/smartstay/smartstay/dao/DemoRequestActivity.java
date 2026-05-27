package com.smartstay.smartstay.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DemoRequestActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;
    private String comment;
    private String description;
    private String status;
    private String createdByUserType;
    private String createdBy;
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "request_id")
    @JsonIgnore
    private DemoRequest demoRequest;
}
