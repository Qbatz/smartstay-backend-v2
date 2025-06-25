package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.AddressTypes;
import com.smartstay.smartstay.dao.Countries;
import com.smartstay.smartstay.dao.HotelType;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.repositories.AddressTypeRepository;
import com.smartstay.smartstay.repositories.CountriesRepository;
import com.smartstay.smartstay.repositories.HotelTypeRepository;
import com.smartstay.smartstay.repositories.RolesRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SmartstayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartstayApplication.class, args);
	}

	@Bean
	CommandLineRunner loadData(RolesRepository rolesRepository) {
		return args -> {
//			RolesV1 v1 = new RolesV1();
//			v1.setRoleName("Admin");
//			rolesRepository.save(v1);
//
//			RolesV1 v2 = new RolesV1();
//			v2.setRoleName("Accountant");
//			rolesRepository.save(v2);
//
//			RolesV1 v3 = new RolesV1();
//			v3.setRoleName("User");
//			rolesRepository.save(v3);

		};
	}

	@Bean
	CommandLineRunner loadCountryData(CountriesRepository countriesRepository) {
		return args -> {
//			Countries countries = new Countries();
//			countries.setCountryCode("91");
//			countries.setCountryName("India");
//			countries.setCurrency("INR");
//
//			countriesRepository.save(countries);
		};
	}

	@Bean
	CommandLineRunner addAddressType(AddressTypeRepository addressTypeRepository) {
		return args -> {
//			AddressTypes addressTypes = new AddressTypes();
//			addressTypes.setType("Present");
//
//			addressTypeRepository.save(addressTypes);
//
//			AddressTypes addressTypes2 = new AddressTypes();
//			addressTypes2.setType("Permanent");
//
//			addressTypeRepository.save(addressTypes2);
		};
	}

	@Bean
	CommandLineRunner addHotelType(HotelTypeRepository hotelTypeRepository) {
		return args -> {
//			HotelType type1 = new HotelType();
//			type1.setActive(true);
//			type1.setType("PG");
//
//			hotelTypeRepository.save(type1);
//
//			HotelType type2 = new HotelType();
//			type2.setActive(true);
//			type2.setType("Hotel");
//
//			hotelTypeRepository.save(type2);
		};
	}

}
