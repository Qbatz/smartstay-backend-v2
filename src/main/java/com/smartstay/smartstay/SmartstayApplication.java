package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.RolesV1;
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

}
