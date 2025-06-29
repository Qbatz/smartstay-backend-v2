package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Credentials {

    @Id
    String service;
    String clientId;
    String authToken;
    String secretValue;
    String refreshToken;
    String otherSecrets;

}
