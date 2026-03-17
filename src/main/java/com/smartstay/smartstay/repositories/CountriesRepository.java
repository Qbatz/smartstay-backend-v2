package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Countries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountriesRepository extends JpaRepository<Countries, Long> {
    Countries findByCountryName(String countryName);
}
