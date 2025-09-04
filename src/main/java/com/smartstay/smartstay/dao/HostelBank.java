package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HostelBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String hostelId;
    String bankAccountId;

}
