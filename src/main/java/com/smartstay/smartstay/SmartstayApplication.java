package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.services.TemplatesService;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class SmartstayApplication {

	@Autowired
	TemplatesService templatesService;

	public static void main(String[] args) {
		SpringApplication.run(SmartstayApplication.class, args);
	}

	@Bean
	CommandLineRunner createDefaultRole(RolesRepository rolesRepository) {
		return args -> {
//				RolesV1 superAdminRole = new RolesV1();
//				superAdminRole.setRoleName("Smartstay - Super Admin");
//				superAdminRole.setIsEditable(false);
//
//				List<RolesPermission> permissions1 = new ArrayList<>();
//				for (int i = 1; i <= 24; i++) {
//					RolesPermission perm = new RolesPermission();
//					perm.setModuleId(i);
//					perm.setCanRead(true);
//					perm.setCanWrite(true);
//					perm.setCanUpdate(true);
//					perm.setCanDelete(true);
//					permissions1.add(perm);
//				}
//				superAdminRole.setPermissions(permissions1);
//
//				rolesRepository.save(superAdminRole);
//
//
//				RolesV1 adminRole = new RolesV1();
//				adminRole.setRoleName("Smartstay - Admin");
//				adminRole.setIsEditable(false);
//
//				List<RolesPermission> permissions2 = new ArrayList<>();
//				for (int i = 1; i <= 24; i++) {
//					RolesPermission perm = new RolesPermission();
//					perm.setModuleId(i);
//					perm.setCanRead(true);
//					perm.setCanWrite(true);
//					perm.setCanUpdate(false);
//					perm.setCanDelete(false);
//					permissions2.add(perm);
//				}
//				adminRole.setPermissions(permissions2);
//
//				rolesRepository.save(adminRole);
//
//
//				RolesV1 readOnlyRole = new RolesV1();
//				readOnlyRole.setRoleName("ReadOnly");
//				readOnlyRole.setIsEditable(false);
//
//				List<RolesPermission> permissions3 = new ArrayList<>();
//				for (int i = 1; i <= 24; i++) {
//					RolesPermission perm = new RolesPermission();
//					perm.setModuleId(i);
//					perm.setCanRead(true);
//					perm.setCanWrite(false);
//					perm.setCanUpdate(false);
//					perm.setCanDelete(false);
//					permissions3.add(perm);
//				}
//				readOnlyRole.setPermissions(permissions3);
//
//				rolesRepository.save(readOnlyRole);
//
//				RolesV1 writeOnlyRole = new RolesV1();
//				writeOnlyRole.setRoleName("WriteOnly");
//				writeOnlyRole.setIsEditable(false);
//
//				List<RolesPermission> permissions4 = new ArrayList<>();
//				for (int i = 1; i <= 24; i++) {
//					RolesPermission perm = new RolesPermission();
//					perm.setModuleId(i);
//					perm.setCanRead(true);
//					perm.setCanWrite(true);
//					perm.setCanUpdate(false);
//					perm.setCanDelete(false);
//					permissions4.add(perm);
//				}
//				writeOnlyRole.setPermissions(permissions4);
//
//				rolesRepository.save(writeOnlyRole);
//			}
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

	@Bean
	CommandLineRunner addBilltemplateToExistingHostels(BillTemplatesRepository billTemplatesRepository, HostelV1Repository hostelV1Repository) {
		return args -> {
			List<HostelV1> listHostels = hostelV1Repository.findAll();
			listHostels.forEach(item -> {
				BillTemplates billTemplates = billTemplatesRepository.getByHostelId(item.getHostelId());
				if (billTemplates == null) {
					com.smartstay.smartstay.payloads.templates.BillTemplates templates = new com.smartstay.smartstay.payloads.templates.BillTemplates(
							item.getHostelId(),
							item.getMobile(),
							item.getEmailId(),
							item.getHostelName()
					);
					templatesService.initialTemplateSetup(templates, item.getCreatedBy());
				}
			});
		};
	}

	@Bean
	CommandLineRunner addInvoiceDueDate(InvoicesV1Repository invoicesV1Repository) {
		return args -> {
			List<InvoicesV1> listInvoices = invoicesV1Repository.findAll();
			if (!listInvoices.isEmpty()) {
				listInvoices.stream().forEach(item -> {
					if (item.getInvoiceDueDate() == null) {
                        item.setInvoiceDueDate(Utils.addDaysToDate(item.getInvoiceGeneratedDate(), 5));
						invoicesV1Repository.save(item);
					}
				});
			}
		};
	}



}
