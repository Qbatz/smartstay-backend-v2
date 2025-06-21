package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.responses.LoginUsersDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<Users, String> {


    Users findUserByEmailId(String emailId);

    Users findUserByUserId(String userId);

    @Query(value = """
                SELECT 
                    usr.user_id AS userId,
                    usr.first_name AS firstName,
                    usr.last_name AS lastName,
                    usr.mobile_no AS mobileNo,
                    usr.email_id AS mailId,
                    usr.role_id AS roleId,
                    roles.role_name AS roleName,
                    country.country_id AS countryId,
                    usr.email_authentication_status AS email_authentication_status,
                    usr.sms_authentication_status AS sms_authentication_status,
                    usr.two_step_verification_status AS two_step_verification_status,
                    country.country_name AS countryName,
                    country.currency AS currency,
                    country.country_code AS countryCode
                FROM smart_stay.users usr
                LEFT OUTER JOIN smart_stay.rolesv1 roles ON roles.role_id = usr.role_id
                LEFT OUTER JOIN smart_stay.countries country ON country.country_id = usr.country
                WHERE usr.user_id = :userId
            """, nativeQuery = true)
    LoginUsersDetails getLoginUserDetails(@Param("userId") String userId);


}
