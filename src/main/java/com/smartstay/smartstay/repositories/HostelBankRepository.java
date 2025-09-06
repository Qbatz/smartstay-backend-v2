package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.HostelBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HostelBankRepository extends JpaRepository<HostelBank, Integer> {

    List<HostelBank> findByHostelIdAndBankAccountIdIn(@Param("hostelId") String hostelId,
                                                     @Param("bankIds") List<String> bankIds);

    @Query("select hb.bankAccountId from HostelBank hb where hb.hostelId=:hostelId")
    List<String> findAllByHostelId(String hostelId);





}
