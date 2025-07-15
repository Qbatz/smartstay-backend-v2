package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.repositories.*;
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
//			v1.setRoleName("Smartstay - Admin");
//			rolesRepository.save(v1);
//
//			RolesV1 v2 = new RolesV1();
//			v2.setRoleName("Admin");
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
	CommandLineRunner addPermissions(RolesPermissionRepository rolesPermissionRepo) {
		return args -> {

//			RolesPermission rolesPermission101 = new RolesPermission();
//			rolesPermission101.setRoleId(1);
//			rolesPermission101.setModuleId(1);
//			rolesPermission101.setCanDelete(true);
//			rolesPermission101.setCanWrite(true);
//			rolesPermission101.setCanRead(true);
//			rolesPermission101.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission101);
//
//			RolesPermission rolesPermission102 = new RolesPermission();
//			rolesPermission102.setRoleId(1);
//			rolesPermission102.setModuleId(2);
//			rolesPermission102.setCanDelete(true);
//			rolesPermission102.setCanWrite(true);
//			rolesPermission102.setCanRead(true);
//			rolesPermission102.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission102);
//
//			RolesPermission rolesPermission103 = new RolesPermission();
//			rolesPermission103.setRoleId(1);
//			rolesPermission103.setModuleId(3);
//			rolesPermission103.setCanDelete(true);
//			rolesPermission103.setCanWrite(true);
//			rolesPermission103.setCanRead(true);
//			rolesPermission103.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission103);
//
//			RolesPermission rolesPermission104 = new RolesPermission();
//			rolesPermission104.setRoleId(1);
//			rolesPermission104.setModuleId(4);
//			rolesPermission104.setCanDelete(true);
//			rolesPermission104.setCanWrite(true);
//			rolesPermission104.setCanRead(true);
//			rolesPermission104.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission104);
//
//			RolesPermission rolesPermission105 = new RolesPermission();
//			rolesPermission105.setRoleId(1);
//			rolesPermission105.setModuleId(5);
//			rolesPermission105.setCanDelete(false);
//			rolesPermission105.setCanWrite(true);
//			rolesPermission105.setCanRead(true);
//			rolesPermission105.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission105);
//
//			RolesPermission rolesPermission106 = new RolesPermission();
//			rolesPermission106.setRoleId(1);
//			rolesPermission106.setModuleId(6);
//			rolesPermission106.setCanDelete(true);
//			rolesPermission106.setCanWrite(true);
//			rolesPermission106.setCanRead(true);
//			rolesPermission106.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission106);
//
//			RolesPermission rolesPermission107 = new RolesPermission();
//			rolesPermission107.setRoleId(1);
//			rolesPermission107.setModuleId(7);
//			rolesPermission107.setCanDelete(true);
//			rolesPermission107.setCanWrite(true);
//			rolesPermission107.setCanRead(true);
//			rolesPermission107.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission107);
//
//			RolesPermission rolesPermission108 = new RolesPermission();
//			rolesPermission108.setRoleId(1);
//			rolesPermission108.setModuleId(8);
//			rolesPermission108.setCanDelete(true);
//			rolesPermission108.setCanWrite(true);
//			rolesPermission108.setCanRead(true);
//			rolesPermission108.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission108);
//
//			RolesPermission rolesPermission109 = new RolesPermission();
//			rolesPermission109.setRoleId(1);
//			rolesPermission109.setModuleId(9);
//			rolesPermission109.setCanDelete(true);
//			rolesPermission109.setCanWrite(true);
//			rolesPermission109.setCanRead(true);
//			rolesPermission109.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission109);
//
//			RolesPermission rolesPermission110 = new RolesPermission();
//			rolesPermission110.setRoleId(1);
//			rolesPermission110.setModuleId(10);
//			rolesPermission110.setCanDelete(true);
//			rolesPermission110.setCanWrite(true);
//			rolesPermission110.setCanRead(true);
//			rolesPermission110.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission110);
//
//			RolesPermission rolesPermission111 = new RolesPermission();
//			rolesPermission111.setRoleId(1);
//			rolesPermission111.setModuleId(11);
//			rolesPermission111.setCanDelete(true);
//			rolesPermission111.setCanWrite(true);
//			rolesPermission111.setCanRead(true);
//			rolesPermission111.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission111);
//
//			RolesPermission rolesPermission112 = new RolesPermission();
//			rolesPermission112.setRoleId(1);
//			rolesPermission112.setModuleId(12);
//			rolesPermission112.setCanDelete(true);
//			rolesPermission112.setCanWrite(true);
//			rolesPermission112.setCanRead(true);
//			rolesPermission112.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission112);
//
//			RolesPermission rolesPermission113 = new RolesPermission();
//			rolesPermission113.setRoleId(1);
//			rolesPermission113.setModuleId(13);
//			rolesPermission113.setCanDelete(true);
//			rolesPermission113.setCanWrite(true);
//			rolesPermission113.setCanRead(true);
//			rolesPermission113.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission113);
//
//			RolesPermission rolesPermission114 = new RolesPermission();
//			rolesPermission114.setRoleId(1);
//			rolesPermission114.setModuleId(14);
//			rolesPermission114.setCanDelete(true);
//			rolesPermission114.setCanWrite(true);
//			rolesPermission114.setCanRead(true);
//			rolesPermission114.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission114);
//
//			RolesPermission rolesPermission115 = new RolesPermission();
//			rolesPermission115.setRoleId(1);
//			rolesPermission115.setModuleId(15);
//			rolesPermission115.setCanDelete(true);
//			rolesPermission115.setCanWrite(true);
//			rolesPermission115.setCanRead(true);
//			rolesPermission115.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission115);
//
//			RolesPermission rolesPermission116 = new RolesPermission();
//			rolesPermission116.setRoleId(1);
//			rolesPermission116.setModuleId(16);
//			rolesPermission116.setCanDelete(true);
//			rolesPermission116.setCanWrite(true);
//			rolesPermission116.setCanRead(true);
//			rolesPermission116.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission116);
//
//			RolesPermission rolesPermission117 = new RolesPermission();
//			rolesPermission117.setRoleId(1);
//			rolesPermission117.setModuleId(17);
//			rolesPermission117.setCanDelete(true);
//			rolesPermission117.setCanWrite(true);
//			rolesPermission117.setCanRead(true);
//			rolesPermission117.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission117);
//
//			RolesPermission rolesPermission118 = new RolesPermission();
//			rolesPermission118.setRoleId(1);
//			rolesPermission118.setModuleId(18);
//			rolesPermission118.setCanDelete(true);
//			rolesPermission118.setCanWrite(true);
//			rolesPermission118.setCanRead(true);
//			rolesPermission118.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission118);
//
//			RolesPermission rolesPermission119 = new RolesPermission();
//			rolesPermission119.setRoleId(1);
//			rolesPermission119.setModuleId(19);
//			rolesPermission119.setCanDelete(true);
//			rolesPermission119.setCanWrite(true);
//			rolesPermission119.setCanRead(true);
//			rolesPermission119.setCanUpdate(true);
//			rolesPermissionRepo.save(rolesPermission119);
//
//			RolesPermission rolesPermission = new RolesPermission();
//			rolesPermission.setRoleId(2);
//			rolesPermission.setModuleId(1);
//			rolesPermission.setCanDelete(false);
//			rolesPermission.setCanWrite(true);
//			rolesPermission.setCanRead(true);
//			rolesPermission.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission);
//
//			RolesPermission rolesPermission2 = new RolesPermission();
//			rolesPermission2.setRoleId(2);
//			rolesPermission2.setModuleId(2);
//			rolesPermission2.setCanDelete(false);
//			rolesPermission2.setCanWrite(true);
//			rolesPermission2.setCanRead(true);
//			rolesPermission2.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission2);
//
//			RolesPermission rolesPermission3 = new RolesPermission();
//			rolesPermission3.setRoleId(2);
//			rolesPermission3.setModuleId(3);
//			rolesPermission3.setCanDelete(false);
//			rolesPermission3.setCanWrite(true);
//			rolesPermission3.setCanRead(true);
//			rolesPermission3.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission3);
//
//			RolesPermission rolesPermission4 = new RolesPermission();
//			rolesPermission4.setRoleId(2);
//			rolesPermission4.setModuleId(4);
//			rolesPermission4.setCanDelete(false);
//			rolesPermission4.setCanWrite(true);
//			rolesPermission4.setCanRead(true);
//			rolesPermission4.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission4);
//
//			RolesPermission rolesPermission5 = new RolesPermission();
//			rolesPermission5.setRoleId(2);
//			rolesPermission5.setModuleId(5);
//			rolesPermission5.setCanDelete(false);
//			rolesPermission5.setCanWrite(true);
//			rolesPermission5.setCanRead(true);
//			rolesPermission5.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission5);
//
//			RolesPermission rolesPermission6 = new RolesPermission();
//			rolesPermission6.setRoleId(2);
//			rolesPermission6.setModuleId(6);
//			rolesPermission6.setCanDelete(false);
//			rolesPermission6.setCanWrite(true);
//			rolesPermission6.setCanRead(true);
//			rolesPermission6.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission6);
//
//			RolesPermission rolesPermission7 = new RolesPermission();
//			rolesPermission7.setRoleId(2);
//			rolesPermission7.setModuleId(7);
//			rolesPermission7.setCanDelete(false);
//			rolesPermission7.setCanWrite(true);
//			rolesPermission7.setCanRead(true);
//			rolesPermission7.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission7);
//
//			RolesPermission rolesPermission8 = new RolesPermission();
//			rolesPermission8.setRoleId(2);
//			rolesPermission8.setModuleId(8);
//			rolesPermission8.setCanDelete(false);
//			rolesPermission8.setCanWrite(true);
//			rolesPermission8.setCanRead(true);
//			rolesPermission8.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission8);
//
//			RolesPermission rolesPermission9 = new RolesPermission();
//			rolesPermission9.setRoleId(2);
//			rolesPermission9.setModuleId(9);
//			rolesPermission9.setCanDelete(false);
//			rolesPermission9.setCanWrite(true);
//			rolesPermission9.setCanRead(true);
//			rolesPermission9.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission9);
//
//			RolesPermission rolesPermission10 = new RolesPermission();
//			rolesPermission10.setRoleId(2);
//			rolesPermission10.setModuleId(10);
//			rolesPermission10.setCanDelete(false);
//			rolesPermission10.setCanWrite(true);
//			rolesPermission10.setCanRead(true);
//			rolesPermission10.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission10);
//
//			RolesPermission rolesPermission11 = new RolesPermission();
//			rolesPermission11.setRoleId(2);
//			rolesPermission11.setModuleId(11);
//			rolesPermission11.setCanDelete(false);
//			rolesPermission11.setCanWrite(true);
//			rolesPermission11.setCanRead(true);
//			rolesPermission11.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission11);
//
//			RolesPermission rolesPermission12 = new RolesPermission();
//			rolesPermission12.setRoleId(2);
//			rolesPermission12.setModuleId(12);
//			rolesPermission12.setCanDelete(false);
//			rolesPermission12.setCanWrite(true);
//			rolesPermission12.setCanRead(true);
//			rolesPermission12.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission12);
//
//			RolesPermission rolesPermission13 = new RolesPermission();
//			rolesPermission13.setRoleId(2);
//			rolesPermission13.setModuleId(13);
//			rolesPermission13.setCanDelete(false);
//			rolesPermission13.setCanWrite(true);
//			rolesPermission13.setCanRead(true);
//			rolesPermission13.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission13);
//
//			RolesPermission rolesPermission14 = new RolesPermission();
//			rolesPermission14.setRoleId(2);
//			rolesPermission14.setModuleId(14);
//			rolesPermission14.setCanDelete(false);
//			rolesPermission14.setCanWrite(true);
//			rolesPermission14.setCanRead(true);
//			rolesPermission14.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission14);
//
//			RolesPermission rolesPermission15 = new RolesPermission();
//			rolesPermission15.setRoleId(2);
//			rolesPermission15.setModuleId(15);
//			rolesPermission15.setCanDelete(false);
//			rolesPermission15.setCanWrite(true);
//			rolesPermission15.setCanRead(true);
//			rolesPermission15.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission15);
//
//			RolesPermission rolesPermission16 = new RolesPermission();
//			rolesPermission16.setRoleId(2);
//			rolesPermission16.setModuleId(16);
//			rolesPermission16.setCanDelete(false);
//			rolesPermission16.setCanWrite(true);
//			rolesPermission16.setCanRead(true);
//			rolesPermission16.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission16);
//
//			RolesPermission rolesPermission17 = new RolesPermission();
//			rolesPermission17.setRoleId(2);
//			rolesPermission17.setModuleId(17);
//			rolesPermission17.setCanDelete(false);
//			rolesPermission17.setCanWrite(true);
//			rolesPermission17.setCanRead(true);
//			rolesPermission17.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission17);
//
//			RolesPermission rolesPermission18 = new RolesPermission();
//			rolesPermission18.setRoleId(2);
//			rolesPermission18.setModuleId(18);
//			rolesPermission18.setCanDelete(false);
//			rolesPermission18.setCanWrite(true);
//			rolesPermission18.setCanRead(true);
//			rolesPermission18.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission18);
//
//			RolesPermission rolesPermission19 = new RolesPermission();
//			rolesPermission19.setRoleId(2);
//			rolesPermission19.setModuleId(19);
//			rolesPermission19.setCanDelete(false);
//			rolesPermission19.setCanWrite(true);
//			rolesPermission19.setCanRead(true);
//			rolesPermission19.setCanUpdate(false);
//			rolesPermissionRepo.save(rolesPermission19);

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
		};
	}

}
