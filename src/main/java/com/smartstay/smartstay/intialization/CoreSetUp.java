package com.smartstay.smartstay.intialization;

import com.smartstay.smartstay.SmartstayApplication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.FilterOptionsModule;
import com.smartstay.smartstay.ennum.PlanType;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.util.Utils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class CoreSetUp {
    @Bean
    CommandLineRunner createDefaultRole(RolesRepository rolesRepository) {
        return args -> {
			List<RolesV1> superAdminRole = rolesRepository.findByRoleName("Smartstay - Super Admin");
			if (superAdminRole == null || superAdminRole.isEmpty()) {
				RolesV1 superAdminRole1 = new RolesV1();
				superAdminRole1.setRoleName("Smartstay - Super Admin");
				superAdminRole1.setIsEditable(false);

				List<RolesPermission> permissions1 = new ArrayList<>();
				for (int i = 1; i <= 25; i++) {
					RolesPermission perm = new RolesPermission();
					perm.setModuleId(i);
					perm.setCanRead(true);
					perm.setCanWrite(true);
					perm.setCanUpdate(true);
					perm.setCanDelete(true);
					permissions1.add(perm);
				}
				superAdminRole1.setPermissions(permissions1);

				rolesRepository.save(superAdminRole1);
			}

			List<RolesV1> adminRole = rolesRepository.findByRoleName("Smartstay - Admin");
			if (adminRole == null || adminRole.isEmpty()) {
				RolesV1 adminRole1 = new RolesV1();
				adminRole1.setRoleName("Smartstay - Admin");
				adminRole1.setIsEditable(false);

				List<RolesPermission> permissions2 = new ArrayList<>();
				for (int i = 1; i <= 25; i++) {
					RolesPermission perm = new RolesPermission();
					perm.setModuleId(i);
					perm.setCanRead(true);
					perm.setCanWrite(true);
					perm.setCanUpdate(false);
					perm.setCanDelete(false);
					permissions2.add(perm);
				}
				adminRole1.setPermissions(permissions2);

				rolesRepository.save(adminRole1);
			}
			List<RolesV1> readOnlyRoles = rolesRepository.findByRoleName("ReadOnly");
			if (readOnlyRoles == null || readOnlyRoles.isEmpty()) {
				RolesV1 readOnlyRole = new RolesV1();
				readOnlyRole.setRoleName("ReadOnly");
				readOnlyRole.setIsEditable(false);

				List<RolesPermission> permissions3 = new ArrayList<>();
				for (int i = 1; i <= 25; i++) {
					RolesPermission perm = new RolesPermission();
					perm.setModuleId(i);
					perm.setCanRead(true);
					perm.setCanWrite(false);
					perm.setCanUpdate(false);
					perm.setCanDelete(false);
					permissions3.add(perm);
				}
				readOnlyRole.setPermissions(permissions3);

				rolesRepository.save(readOnlyRole);
			}

			List<RolesV1> writeOnlyRoles = rolesRepository.findByRoleName("WriteOnly");
			if (writeOnlyRoles == null || writeOnlyRoles.isEmpty()) {
				RolesV1 writeOnlyRole = new RolesV1();
				writeOnlyRole.setRoleName("WriteOnly");
				writeOnlyRole.setIsEditable(false);

				List<RolesPermission> permissions4 = new ArrayList<>();
				for (int i = 1; i <= 25; i++) {
					RolesPermission perm = new RolesPermission();
					perm.setModuleId(i);
					perm.setCanRead(true);
					perm.setCanWrite(true);
					perm.setCanUpdate(false);
					perm.setCanDelete(false);
					permissions4.add(perm);
				}
				writeOnlyRole.setPermissions(permissions4);

				rolesRepository.save(writeOnlyRole);
			}
        };
    }

    @Bean
    CommandLineRunner loadCountryData(CountriesRepository countriesRepository) {
        return args -> {
			Countries countries = countriesRepository.findByCountryName("India");
            if (countries == null) {
                countries = new Countries();
                countries.setCountryCode("91");
                countries.setCountryName("India");
                countries.setCurrency("INR");

                countriesRepository.save(countries);
            }

        };
    }

    @Bean
    CommandLineRunner addAddressType(AddressTypeRepository addressTypeRepository) {
        return args -> {
            AddressTypes presentAddressType = addressTypeRepository.findByType("Present");
            if (presentAddressType == null) {
                AddressTypes addressTypes = new AddressTypes();
                addressTypes.setType("Present");
                addressTypeRepository.save(addressTypes);
            }

            AddressTypes permanentAddressType = addressTypeRepository.findByType("Permanent");
            if (permanentAddressType == null) {
                AddressTypes addressTypes = new AddressTypes();
                addressTypes.setType("Permanent");
                addressTypeRepository.save(addressTypes);
            }
        };
    }


    @Bean
    CommandLineRunner addHotelType(HotelTypeRepository hotelTypeRepository) {
        return args -> {

            HotelType pgType = hotelTypeRepository.findByType("PG");
            if (pgType == null) {
                HotelType type1 = new HotelType();
                type1.setActive(true);
                type1.setType("PG");

                hotelTypeRepository.save(type1);
            }

            HotelType hotelType = hotelTypeRepository.findByType("Hotel");
            if (hotelType == null) {
                HotelType type2 = new HotelType();
                type2.setActive(true);
                type2.setType("Hotel");

                hotelTypeRepository.save(type2);
            }
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
            Modules modules1 = modulesRepository.findByModuleName("Dashboard");
            if (modules1 == null) {
                modules1 = new Modules();
                modules1.setModuleName("Dashboard");
                modulesRepository.save(modules1);
            }
            Modules modules2 = modulesRepository.findByModuleName("Announcement");
            if (modules2 == null) {
                modules2 = new Modules();
                modules2.setModuleName("Announcement");
                modulesRepository.save(modules2);
            }

			Modules modules3 = modulesRepository.findByModuleName("Updates");
			if (modules3 == null) {
				modules3 = new Modules();
				modules3.setModuleName("Updates");
				modulesRepository.save(modules3);
			}

			Modules modules4 = modulesRepository.findByModuleName("Paying Guests");
			if (modules4 == null) {
				modules4 = new Modules();
				modules4.setModuleName("Paying Guests");
				modulesRepository.save(modules4);
			}

			Modules modules5 = modulesRepository.findByModuleName("Customers");
			if (modules5 == null) {
				modules5 = new Modules();
				modules5.setModuleName("Customers");
				modulesRepository.save(modules5);
			}
			Modules modules6 = modulesRepository.findByModuleName("Booking");
			if (modules6 == null) {
				modules6 = new Modules();
				modules6.setModuleName("Booking");
				modulesRepository.save(modules6);
			}
			Modules modules7 = modulesRepository.findByModuleName("Checkout");
			if (modules7 == null) {
				modules7 = new Modules();
				modules7.setModuleName("Checkout");
				modulesRepository.save(modules7);
			}

			Modules modules8 =  modulesRepository.findByModuleName("Walk in");
			if (modules8 == null) {
				modules8 = new Modules();
				modules8.setModuleName("Walk in");
				modulesRepository.save(modules8);
			}

			Modules modules9 =  modulesRepository.findByModuleName("Assets");
			if (modules9 == null) {
				modules9 = new Modules();
				modules9.setModuleName("Assets");
				modulesRepository.save(modules9);
			}

			Modules modules10 = modulesRepository.findByModuleName("Vendor");
			if (modules10 == null) {
				modules10 = new Modules();
				modules10.setModuleName("Vendor");
				modulesRepository.save(modules10);
			}

			Modules modules11 = modulesRepository.findByModuleName("Bills");
			if (modules11 == null) {
				modules11 = new Modules();
				modules11.setModuleName("Bills");
				modulesRepository.save(modules11);
			}

			Modules modules12 = modulesRepository.findByModuleName("Recurring bills");
			if (modules12 == null) {
				modules12 = new Modules();
				modules12.setModuleName("Recurring bills");
				modulesRepository.save(modules12);
			}

			Modules modules13 = modulesRepository.findByModuleName("Complaints");
			if (modules13 == null) {
				modules13 = new Modules();
				modules13.setModuleName("Complaints");
				modulesRepository.save(modules13);
			}

			Modules modules14 = modulesRepository.findByModuleName("Electricity");
			if (modules14 == null) {
				modules14 = new Modules();
				modules14.setModuleName("Electricity");
				modulesRepository.save(modules14);
			}

			Modules modules15 = modulesRepository.findByModuleName("Expense");
			if (modules15 == null) {
				modules15 = new Modules();
				modules15.setModuleName("Expense");
				modulesRepository.save(modules15);
			}

			Modules modules16 = modulesRepository.findByModuleName("Reports");
			if (modules16 == null) {
				modules16 = new Modules();
				modules16.setModuleName("Reports");
				modulesRepository.save(modules16);
			}

			Modules modules17 = modulesRepository.findByModuleName("Banking");
			if (modules17 == null) {
				modules17 = new Modules();
				modules17.setModuleName("Banking");
				modulesRepository.save(modules17);
			}

			Modules modules18 = modulesRepository.findByModuleName("Profile");
			if (modules18 == null) {
				modules18 = new Modules();
				modules18.setModuleName("Profile");
				modulesRepository.save(modules18);
			}

			Modules modules19 = modulesRepository.findByModuleName("Amenities");
			if (modules19 == null) {
				modules19 = new Modules();
				modules19.setModuleName("Amenities");
				modulesRepository.save(modules19);
			}

			Modules modules20 = modulesRepository.findByModuleName("Receipt");
			if (modules20 == null) {
				modules20 = new Modules();
				modules20.setModuleName("Receipt");
				modulesRepository.save(modules20);
			}
			Modules modules21 = modulesRepository.findByModuleName("Invoice");
			if (modules21 == null) {
				modules21 = new Modules();
				modules21.setModuleName("Invoice");
				modulesRepository.save(modules21);
			}

			Modules modules22 = modulesRepository.findByModuleName("User");
			if (modules22 == null) {
				modules22 = new Modules();
				modules22.setModuleName("User");
				modulesRepository.save(modules22);
			}

			Modules modules23 = modulesRepository.findByModuleName("Role");
			if (modules23 == null) {
				modules23 = new Modules();
				modules23.setModuleName("Role");
				modulesRepository.save(modules23);
			}

			Modules modules24 = modulesRepository.findByModuleName("Agreement");
			if (modules24 == null) {
				modules24 = new Modules();
				modules24.setModuleName("Agreement");
				modulesRepository.save(modules24);
			}
			Modules modules25 = modulesRepository.findByModuleName("Subscription");
			if (modules25 == null) {
				modules25 = new Modules();
				modules25.setModuleName("Subscription");
				modulesRepository.save(modules25);
			}

        };
    }

    @Bean
    CommandLineRunner addPlans(PlansRepository plansRepository) {
        return args -> {
			Plans trialPlan = plansRepository.findPlanByPlanTypeAndIsActiveTrue(PlanType.TRIAL.name());
			if (trialPlan == null) {
				String planCode1 = Utils.generatePlanCode();

				Plans planTrial = new Plans();
				planTrial.setPlanName("Trial");
				planTrial.setDuration(30l);
				planTrial.setPrice(0.0);
				planTrial.setDiscounts(0.0);
				planTrial.setCgstAmount(0.0);
				planTrial.setSgstAmount(0.0);
				planTrial.setGstAmount(0.0);
				planTrial.setGst(18.0);
				planTrial.setSgst(9.0);
				planTrial.setCgst(9.0);
				planTrial.setFinalPrice(0.0);
				planTrial.setPlanType(PlanType.TRIAL.name());
				planTrial.setPlanCode(planCode1);
				planTrial.setShouldShow(false);
				planTrial.setActive(true);
				planTrial.setCanCustomize(false);
				planTrial.setCreatedAt(new Date());
				planTrial.setUpdatedAt(new Date());

				List<PlanFeatures> listFeatures = new ArrayList<>();
				PlanFeatures planFeatures = new PlanFeatures();
				planFeatures.setActive(true);
				planFeatures.setPrice(0.0);
				planFeatures.setFeatureName("Tenant Management");
				planFeatures.setPlan(planTrial);

				PlanFeatures planFeatures2 = new PlanFeatures();
				planFeatures2.setActive(true);
				planFeatures2.setPrice(0.0);
				planFeatures2.setFeatureName("PG Management");
				planFeatures2.setPlan(planTrial);

				PlanFeatures planFeatures3 = new PlanFeatures();
				planFeatures3.setActive(true);
				planFeatures3.setPrice(0.0);
				planFeatures3.setFeatureName("Account Management");
				planFeatures3.setPlan(planTrial);

				PlanFeatures planFeatures4 = new PlanFeatures();
				planFeatures4.setActive(true);
				planFeatures4.setPrice(0.0);
				planFeatures4.setFeatureName("Expense Management");
				planFeatures4.setPlan(planTrial);



				listFeatures.add(planFeatures);
				listFeatures.add(planFeatures2);
				listFeatures.add(planFeatures3);
				listFeatures.add(planFeatures4);

				planTrial.setFeaturesList(listFeatures);

				plansRepository.save(planTrial);
			}

			Plans planBasic = plansRepository.findPlanByPlanTypeAndIsActiveTrue(PlanType.BASIC.name());
			if (planBasic == null) {
				double gstPercentage = 18;
				double cgstPercentage = gstPercentage/2;
				double sgstPercentage = gstPercentage/2;

				double gstAmount = 0.0;
				double cgstAmount = 0.0;
				double sgstAmount = 0.0;

				gstAmount = (gstPercentage/100) * 599;
				cgstAmount = (cgstPercentage/100) * 599;
				sgstAmount = (sgstPercentage/100) * 599;

				String planCode3 = Utils.generatePlanCode();
				planBasic = new Plans();
				planBasic.setPlanName("Basic");
				planBasic.setDuration(30l);
				planBasic.setPrice(599.0);
				planBasic.setDiscounts(0.0);
				planBasic.setCgstAmount(Utils.roundOffWithTwoDigit(cgstAmount));
				planBasic.setSgstAmount(Utils.roundOffWithTwoDigit(sgstAmount));
				planBasic.setGstAmount(Utils.roundOffWithTwoDigit(gstAmount));
				planBasic.setGst(18.0);
				planBasic.setSgst(9.0);
				planBasic.setCgst(9.0);
				planBasic.setFinalPrice(Utils.roundOffWithTwoDigit(599+gstAmount));
				planBasic.setPlanType(PlanType.BASIC.name());
				planBasic.setPlanCode(planCode3);
				planBasic.setShouldShow(true);
				planBasic.setActive(true);
				planBasic.setCanCustomize(false);
				planBasic.setCreatedAt(new Date());
				planBasic.setUpdatedAt(new Date());

				List<PlanFeatures> listFeatures21 = new ArrayList<>();
				PlanFeatures planFeatures21 = new PlanFeatures();
				planFeatures21.setActive(true);
				planFeatures21.setPrice(0.0);
				planFeatures21.setFeatureName("Tenant Management");
				planFeatures21.setPlan(planBasic);

				PlanFeatures planFeatures22 = new PlanFeatures();
				planFeatures22.setActive(true);
				planFeatures22.setPrice(0.0);
				planFeatures22.setFeatureName("PG Management");
				planFeatures22.setPlan(planBasic);

				PlanFeatures planFeatures23 = new PlanFeatures();
				planFeatures23.setActive(true);
				planFeatures23.setPrice(0.0);
				planFeatures23.setFeatureName("Account Management");
				planFeatures23.setPlan(planBasic);

				PlanFeatures planFeatures24 = new PlanFeatures();
				planFeatures24.setActive(true);
				planFeatures24.setPrice(0.0);
				planFeatures24.setFeatureName("Expense Management");
				planFeatures24.setPlan(planBasic);



				listFeatures21.add(planFeatures21);
				listFeatures21.add(planFeatures22);
				listFeatures21.add(planFeatures23);
				listFeatures21.add(planFeatures24);

				planBasic.setFeaturesList(listFeatures21);
				plansRepository.save(planBasic);
			}

			Plans planAdvance = plansRepository.findPlanByPlanTypeAndIsActiveTrue(PlanType.ADVANCED.name());
			if (planAdvance == null) {
				double gstPercentage = 18;
				double cgstPercentage = gstPercentage/2;
				double sgstPercentage = gstPercentage/2;

				double gstAmount = 0.0;
				double cgstAmount = 0.0;
				double sgstAmount = 0.0;

				gstAmount = (gstPercentage/100) * 999;
				cgstAmount = (cgstPercentage/100) * 999;
				sgstAmount = (sgstPercentage/100) * 999;

				String planCode2 = Utils.generatePlanCode();
				planAdvance = new Plans();
				planAdvance.setPlanName("Advance");
				planAdvance.setDuration(30l);
				planAdvance.setPrice(999.0);
				planAdvance.setDiscounts(0.0);
				planAdvance.setCgstAmount(Utils.roundOffWithTwoDigit(cgstAmount));
				planAdvance.setSgstAmount(Utils.roundOffWithTwoDigit(sgstAmount));
				planAdvance.setGstAmount(Utils.roundOffWithTwoDigit(gstAmount));
				planAdvance.setGst(18.0);
				planAdvance.setSgst(9.0);
				planAdvance.setCgst(9.0);
				planAdvance.setFinalPrice(Utils.roundOffWithTwoDigit(999+gstAmount));
				planAdvance.setPlanType(PlanType.ADVANCED.name());
				planAdvance.setPlanCode(planCode2);
				planAdvance.setShouldShow(true);
				planAdvance.setActive(true);
				planAdvance.setCanCustomize(false);
				planAdvance.setCreatedAt(new Date());
				planAdvance.setUpdatedAt(new Date());

				List<PlanFeatures> listFeatures31 = new ArrayList<>();
				PlanFeatures planFeatures31 = new PlanFeatures();
				planFeatures31.setActive(true);
				planFeatures31.setPrice(0.0);
				planFeatures31.setFeatureName("Tenant Management");
				planFeatures31.setPlan(planAdvance);

				PlanFeatures planFeatures32 = new PlanFeatures();
				planFeatures32.setActive(true);
				planFeatures32.setPrice(0.0);
				planFeatures32.setFeatureName("PG Management");
				planFeatures32.setPlan(planAdvance);

				PlanFeatures planFeatures33 = new PlanFeatures();
				planFeatures33.setActive(true);
				planFeatures33.setPrice(0.0);
				planFeatures33.setFeatureName("Account Management");
				planFeatures33.setPlan(planAdvance);

				PlanFeatures planFeatures34 = new PlanFeatures();
				planFeatures34.setActive(true);
				planFeatures34.setPrice(0.0);
				planFeatures34.setFeatureName("Expense Management");
				planFeatures34.setPlan(planAdvance);



				listFeatures31.add(planFeatures31);
				listFeatures31.add(planFeatures32);
				listFeatures31.add(planFeatures33);
				listFeatures31.add(planFeatures34);

				planAdvance.setFeaturesList(listFeatures31);
				plansRepository.save(planAdvance);
			}
        };
    }

	@Bean
	CommandLineRunner addTenantBasicFilterOptions(FilterOptionsRepositories filterOptionsRepositories) {
		return args -> {
			FilterOptions tenantFilterOptions = filterOptionsRepositories.findTenantFilterOption();
			if (tenantFilterOptions == null) {
				tenantFilterOptions = new FilterOptions();
				tenantFilterOptions.setModuleName(FilterOptionsModule.MODULE_TENANT.name());
				tenantFilterOptions.setIsActive(true);
				tenantFilterOptions.setCreatedAt(new Date());
				List<ColumnFilters> filters = new ArrayList<>();

				ColumnFilters filters1 = new ColumnFilters();
				filters1.setSelected(true);
				filters1.setFieldName("Profile Pic");
				filters1.setOrder(1);

				ColumnFilters filters2 = new ColumnFilters();
				filters2.setSelected(true);
				filters2.setFieldName("Full Name");
				filters2.setOrder(2);

				ColumnFilters filters3 = new ColumnFilters();
				filters3.setSelected(true);
				filters3.setFieldName("Status");
				filters3.setOrder(3);

				ColumnFilters filters4 = new ColumnFilters();
				filters4.setSelected(true);
				filters4.setFieldName("Joining Date");
				filters4.setOrder(4);

				ColumnFilters filters5 = new ColumnFilters();
				filters5.setSelected(true);
				filters5.setFieldName("Mobile No");
				filters5.setOrder(5);

				ColumnFilters filters6 = new ColumnFilters();
				filters6.setSelected(true);
				filters6.setFieldName("Floor");
				filters6.setOrder(6);

				ColumnFilters filters7 = new ColumnFilters();
				filters7.setSelected(true);
				filters7.setFieldName("Room");
				filters7.setOrder(7);

				ColumnFilters filters8 = new ColumnFilters();
				filters8.setSelected(true);
				filters8.setFieldName("Bed");
				filters8.setOrder(8);

				ColumnFilters filters9 = new ColumnFilters();
				filters9.setSelected(false);
				filters9.setFieldName("Email ID");
				filters9.setOrder(9);

				ColumnFilters filters10 = new ColumnFilters();
				filters10.setSelected(false);
				filters10.setFieldName("Booking Date");
				filters10.setOrder(10);

				ColumnFilters filters11 = new ColumnFilters();
				filters11.setSelected(false);
				filters11.setFieldName("Monthly Rent");
				filters11.setOrder(11);

				ColumnFilters filters12 = new ColumnFilters();
				filters12.setSelected(false);
				filters12.setFieldName("Advance");
				filters12.setOrder(12);

				ColumnFilters filters13 = new ColumnFilters();
				filters13.setSelected(false);
				filters13.setFieldName("Booking Amount");
				filters13.setOrder(13);

				filters.add(filters1);
				filters.add(filters2);
				filters.add(filters3);
				filters.add(filters4);
				filters.add(filters5);
				filters.add(filters6);
				filters.add(filters7);
				filters.add(filters8);
				filters.add(filters9);
				filters.add(filters10);
				filters.add(filters11);
				filters.add(filters12);
				filters.add(filters13);

				tenantFilterOptions.setFilterOptions(filters);

				filterOptionsRepositories.save(tenantFilterOptions);
			}
		};
	}

	@Bean
	CommandLineRunner addBookingFilterOptions(FilterOptionsRepositories filterOptionsRepositories) {
		return args -> {
			FilterOptions bookingsFilterOptions = filterOptionsRepositories.findBookingsFilterOptions();
			if (bookingsFilterOptions == null) {
				bookingsFilterOptions = new FilterOptions();
				bookingsFilterOptions.setModuleName(FilterOptionsModule.MODULE_BOOKINGS.name());
				bookingsFilterOptions.setIsActive(true);
				bookingsFilterOptions.setCreatedAt(new Date());

				List<ColumnFilters> defaultColumnFilters = new ArrayList<>();
				ColumnFilters filters1 = new ColumnFilters();
				filters1.setSelected(true);
				filters1.setFieldName("Inv No");
				filters1.setOrder(1);


				ColumnFilters filters2 = new ColumnFilters();
				filters2.setSelected(true);
				filters2.setFieldName("Booking Date");
				filters2.setOrder(2);

				ColumnFilters filters3 = new ColumnFilters();
				filters3.setSelected(true);
				filters3.setFieldName("Tenant Name");
				filters3.setOrder(3);

				ColumnFilters filters4 = new ColumnFilters();
				filters4.setSelected(true);
				filters4.setFieldName("Profile Pic");
				filters4.setOrder(4);


				ColumnFilters filters5 = new ColumnFilters();
				filters5.setSelected(true);
				filters5.setFieldName("Mobile No");
				filters5.setOrder(5);

				ColumnFilters filters6 = new ColumnFilters();
				filters6.setSelected(true);
				filters6.setFieldName("Amount");
				filters6.setOrder(6);

//                ColumnFilters filters7 = new ColumnFilters();
//                filters7.setSelected(true);
//                filters7.setFieldName("Status");
//                filters7.setOrder(7);

				ColumnFilters filters7 = new ColumnFilters();
				filters7.setSelected(true);
				filters7.setFieldName("Joining Date");
				filters7.setOrder(7);

				ColumnFilters filters8 = new ColumnFilters();
				filters8.setSelected(true);
				filters8.setFieldName("Floor Name");
				filters8.setOrder(8);

				ColumnFilters filters9 = new ColumnFilters();
				filters9.setSelected(true);
				filters9.setFieldName("Room Name");
				filters9.setOrder(9);

				ColumnFilters filters10 = new ColumnFilters();
				filters10.setSelected(false);
				filters10.setFieldName("Bed Name");
				filters10.setOrder(10);

				ColumnFilters filters11 = new ColumnFilters();
				filters11.setSelected(false);
				filters11.setFieldName("Status");
				filters11.setOrder(11);

				ColumnFilters filters12 = new ColumnFilters();
				filters12.setSelected(false);
				filters12.setFieldName("Available Amount");
				filters12.setOrder(12);

				defaultColumnFilters.add(filters1);
				defaultColumnFilters.add(filters2);
				defaultColumnFilters.add(filters3);
				defaultColumnFilters.add(filters4);
				defaultColumnFilters.add(filters5);
				defaultColumnFilters.add(filters6);
				defaultColumnFilters.add(filters7);
				defaultColumnFilters.add(filters8);
				defaultColumnFilters.add(filters9);
				defaultColumnFilters.add(filters10);
				defaultColumnFilters.add(filters11);
				defaultColumnFilters.add(filters12);

				bookingsFilterOptions.setFilterOptions(defaultColumnFilters);

				filterOptionsRepositories.save(bookingsFilterOptions);

			}
		};
	}
}
