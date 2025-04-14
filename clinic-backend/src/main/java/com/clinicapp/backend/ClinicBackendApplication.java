package com.clinicapp.backend;

import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
@SpringBootApplication
@RequiredArgsConstructor // Add for dependency injection
public class ClinicBackendApplication {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(ClinicBackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner createDefaultAdminUser() {
		return args -> {
			String adminEmail = "admin@gmail.com";
			if (userRepository.findByEmail(adminEmail).isEmpty()) {
				User adminUser = User.builder()
						.firstName("Admin")
						.lastName("User")
						.username("admin")
						.email(adminEmail)
							.password(passwordEncoder.encode("password")) // Encode the password
						.role(Role.ADMIN)
						.build();
				userRepository.save(adminUser);
				System.out.println(">>> Default admin user created: " + adminEmail);
			} else {
				System.out.println(">>> Admin user already exists.");
			}
		};
	}
}
