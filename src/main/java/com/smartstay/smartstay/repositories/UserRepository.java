package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.responses.LoginUsersDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, String> {


    Users findUserByEmailId(String emailId);

    Users findUserByUserId(String userId);

    Users findUserByParentId(String parentId);

    List<Users> findAllByParentId(String parentId);

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

    Optional<Users> findByEmailIdOrMobileNo(String emailId, String mobileNo);
    boolean existsByEmailId(String emailId);
    boolean existsByMobileNo(String mobileNo);

    @Query(value = """
            select * from users where role_id=:roleId and is_active=true and is_deleted=false
            """, nativeQuery = true)
    Optional<Users> findUsersByActiveRoles(@Param("roleId") int roleId);

    List<Users> findByRoleIdAndIsActiveTrueAndIsDeletedFalse(int roleId);


}
