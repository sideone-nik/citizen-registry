package gr.citizenregistry.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "gr.citizenregistry.domain")
public class CitizenRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitizenRegistryApplication.class, args);
    }
}
