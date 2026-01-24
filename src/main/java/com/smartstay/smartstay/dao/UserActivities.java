package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.handlers.StringListConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserActivities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;
    private String description;
    //logged in user ids
    private String userId;
    private Date loggedAt;
    private Date createdAt;
    private String parentId;
    //from Activity source enum
    private String source;
    private String sourceId;
//    from activity type enum
    private String activityType;
    private String hostelId;
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> tenantIds;
}
