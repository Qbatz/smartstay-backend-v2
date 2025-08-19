package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class SmartstayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartstayApplication.class, args);
	}

	@Bean
	CommandLineRunner loadData(RolesRepository rolesRepository) {
		return args -> {
//			RolesV1 v1 = new RolesV1();
//			v1.setRoleName("Smartstay - Super Admin");
//			List<RolesPermission> listPermissions1 = new ArrayList<>();
//			for (int i=1; i<25; i++) {
//				RolesPermission pr1 = new RolesPermission();
//				pr1.setCanUpdate(true);
//				pr1.setCanRead(true);
//				pr1.setCanWrite(true);
//				pr1.setCanDelete(true);
//				pr1.setModuleId(i);
//				listPermissions1.add(pr1);
//			}
//			v1.setPermissions(listPermissions1);
//			rolesRepository.save(v1);
//
//			RolesV1 v2 = new RolesV1();
//			v2.setRoleName("Smartstay - Admin");
//			List<RolesPermission> listPermissions2 = new ArrayList<>();
//			for (int i=1; i<25; i++) {
//				RolesPermission pr2 = new RolesPermission();
//				pr2.setModuleId(i);
//				pr2.setCanUpdate(false);
//				pr2.setCanRead(true);
//				pr2.setCanWrite(true);
//				pr2.setCanDelete(false);
//				listPermissions2.add(pr2);
//			}
//			v2.setPermissions(listPermissions2);
//			rolesRepository.save(v2);

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

	@Bean
	CommandLineRunner addCredentials(CredentialsRepository credentialRepository) {
		return args -> {
//			Credentials credentials = new Credentials();
//			credentials.setAuthToken("1000.2306430c5b48cdb3dbc725a6038274cb.c60048d2830de00ef224e7d3a3e5e674");
//			credentials.setService("zoho");
//			credentials.setSecretValue("db20f122ac9e4c7e0d2f872a534ea6ce5f7b9f89f1");
//			credentials.setClientId("1000.VQXZ7ZTIN3P4H0BRD2EM1HY2GABFVC");
//			credentials.setRefreshToken("1000.20e3755f4209fc16b228915193d3c887.5b2530c277a68728f5156a53731cc701");
//			credentialRepository.save(credentials);
		};
	}

	@Bean
	CommandLineRunner addModules(ModulesRepository modulesRepository) {
		return args -> {
//			Modules modules1 = new Modules();
//			modules1.setModuleName("Dashboard");
//			modulesRepository.save(modules1);
//
//			Modules modules2 = new Modules();
//			modules2.setModuleName("Announcement");
//			modulesRepository.save(modules2);
//
//			Modules modules3 = new Modules();
//			modules3.setModuleName("Updates");
//			modulesRepository.save(modules3);
//
//			Modules modules4 = new Modules();
//			modules4.setModuleName("Paying Guests");
//			modulesRepository.save(modules4);
//
//			Modules modules5 = new Modules();
//			modules5.setModuleName("Customers");
//			modulesRepository.save(modules5);
//
//			Modules modules6 = new Modules();
//			modules6.setModuleName("Booking");
//			modulesRepository.save(modules6);
//
//			Modules modules7 = new Modules();
//			modules7.setModuleName("Checkout");
//			modulesRepository.save(modules7);
//
//			Modules modules8 = new Modules();
//			modules8.setModuleName("Walk in");
//			modulesRepository.save(modules8);
//
//			Modules modules9 = new Modules();
//			modules9.setModuleName("Assets");
//			modulesRepository.save(modules9);
//
//			Modules modules10 = new Modules();
//			modules10.setModuleName("Vendor");
//			modulesRepository.save(modules10);
//
//			Modules modules11 = new Modules();
//			modules11.setModuleName("Bills");
//			modulesRepository.save(modules11);
//
//			Modules modules12 = new Modules();
//			modules12.setModuleName("Recurring bills");
//			modulesRepository.save(modules12);
//
//			Modules modules13 = new Modules();
//			modules13.setModuleName("Complaints");
//			modulesRepository.save(modules13);
//
//			Modules modules14 = new Modules();
//			modules14.setModuleName("Electricity");
//			modulesRepository.save(modules14);
//
//			Modules modules15 = new Modules();
//			modules15.setModuleName("Expense");
//			modulesRepository.save(modules15);
//
//			Modules modules16 = new Modules();
//			modules16.setModuleName("Reports");
//			modulesRepository.save(modules16);
//
//			Modules modules17 = new Modules();
//			modules17.setModuleName("Banking");
//			modulesRepository.save(modules17);
//
//			Modules modules18 = new Modules();
//			modules18.setModuleName("Profile");
//			modulesRepository.save(modules18);
//
//			Modules modules19 = new Modules();
//			modules19.setModuleName("Amenities");
//			modulesRepository.save(modules19);
//
//			Modules modules20 = new Modules();
//			modules20.setModuleName("Receipt");
//			modulesRepository.save(modules20);
//
//			Modules modules21 = new Modules();
//			modules21.setModuleName("Invoice");
//			modulesRepository.save(modules21);
//
//			Modules modules22 = new Modules();
//			modules22.setModuleName("User");
//			modulesRepository.save(modules22);
//
//			Modules modules23 = new Modules();
//			modules23.setModuleName("Role");
//			modulesRepository.save(modules23);
//
//			Modules modules24 = new Modules();
//			modules24.setModuleName("Agreement");
//			modulesRepository.save(modules24);
		};
	}
	@Bean
	CommandLineRunner addPaymentStatus(PaymentStatusRepository paymentStatusRepository) {
		return args -> {
//			PaymentStatus paymentStatus = new PaymentStatus();
//			paymentStatus.setStatus("Paid");
//			paymentStatusRepository.save(paymentStatus);
//
//			PaymentStatus paymentStatus2 = new PaymentStatus();
//			paymentStatus2.setStatus("Partially Paid");
//			paymentStatusRepository.save(paymentStatus2);
//
//			PaymentStatus paymentStatus3 = new PaymentStatus();
//			paymentStatus3.setStatus("Pending");
//			paymentStatusRepository.save(paymentStatus3);
		};
	}

	@Bean
	CommandLineRunner addCustomersType(CustomerTypeRepository customerTypeRepository) {
		return args -> {
//			CustomersType customersType = new CustomersType();
//			customersType.setActive(true);
//			customersType.setType("Check in");
//			customerTypeRepository.save(customersType);
//
//			CustomersType customersType2 = new CustomersType();
//			customersType2.setActive(true);
//			customersType2.setType("Booked in");
//			customerTypeRepository.save(customersType2);
//
//			CustomersType customersType3 = new CustomersType();
//			customersType3.setActive(true);
//			customersType3.setType("Checked out");
//			customerTypeRepository.save(customersType3);
//
//			CustomersType customersType4 = new CustomersType();
//			customersType4.setActive(true);
//			customersType4.setType("Walk in");
//			customerTypeRepository.save(customersType4);
		};
	}

}
