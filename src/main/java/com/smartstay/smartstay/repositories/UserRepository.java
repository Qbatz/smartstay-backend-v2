package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.Admin.UsersData;
import com.smartstay.smartstay.dto.LoginUsersDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, String> {


    Users findUserByEmailId(String emailId);

    Users findUserByUserId(String userId);
    Users findUserByUserIdAndParentId(String userId,String parentId);

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
                FROM users usr
                LEFT OUTER JOIN rolesv1 roles ON roles.role_id = usr.role_id
                LEFT OUTER JOIN countries country ON country.country_id = usr.country
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
                    country.country_code AS countryCode,
                    ad.pincode, ad.city, ad.house_no as houseNo, ad.land_mark as landmark, ad.state, ad.street,
                                        usr.profile_url as profilePic, usr.description
                FROM users usr
                LEFT OUTER JOIN rolesv1 roles ON roles.role_id = usr.role_id
                LEFT OUTER JOIN countries country ON country.country_id = usr.country
                LEFT OUTER JOIN address ad on ad.user_id=usr.user_id
                WHERE usr.role_id = :roleId and usr.parent_id =:parentId and usr.is_deleted=0 and usr.is_active=1
            """, nativeQuery = true)
    List<UsersData> getAdminUserList(@Param("roleId") int roleId, @Param("parentId") String parentId);

    @Query(value = """
               SELECT usr.user_id AS userId, usr.first_name AS firstName,
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
                                                          country.country_code AS countryCode,
                                                          ad.pincode AS pincode,
                                                          ad.city AS city,
                                                          ad.house_no AS houseNo,
                                                          ad.land_mark AS landmark,
                                                          ad.state AS state,
                                                          ad.street AS street,
                                                          usr.profile_url AS profilePic,
                                                          usr.description
                                                      FROM users usr
                                                      LEFT JOIN rolesv1 roles ON roles.role_id = usr.role_id
                                                      LEFT JOIN address ad ON ad.user_id = usr.user_id
                                                      LEFT JOIN countries country ON country.country_id = usr.country
                                                      LEFT JOIN user_hostel uh on uh.user_id=usr.user_id
                                                      WHERE uh.hostel_id=:hostelId AND usr.role_id NOT IN (1,2) and usr.is_active=1 and usr.is_deleted = 0
            """, nativeQuery = true)
    List<UsersData> getUserList(@Param("hostelId") String hostelId);

    @Query("SELECT count(u) FROM Users u where u.emailId=:emailId and u.userId !=:userId")
    int getUsersCountByEmail(String userId, String emailId);


}
