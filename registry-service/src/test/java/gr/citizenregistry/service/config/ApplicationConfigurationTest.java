package gr.citizenregistry.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.citizenregistry.domain.Citizen;
import gr.citizenregistry.domain.Gender;
import gr.citizenregistry.service.repository.CitizenRepository;
import gr.citizenregistry.service.service.CitizenService;
import gr.citizenregistry.service.web.CitizenController;
import gr.citizenregistry.service.web.GlobalExceptionHandler;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;

/** Δοκιμές ρύθμισης: φόρτωση context, beans, βάση δεδομένων, JPA, Jackson, Bean Validation. */
@SpringBootTest
class ApplicationConfigurationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Validator validator;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void applicationBeansAreRegistered() {
        assertThat(context.getBean(CitizenController.class)).isNotNull();
        assertThat(context.getBean(CitizenService.class)).isNotNull();
        assertThat(context.getBean(CitizenRepository.class)).isNotNull();
        assertThat(context.getBean(GlobalExceptionHandler.class)).isNotNull();
    }

    @Test
    void dataSourceIsInMemoryH2ForTests() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            assertThat(meta.getDatabaseProductName()).isEqualTo("H2");
            assertThat(meta.getURL()).startsWith("jdbc:h2:mem:");
        }
    }

    @Test
    void jpaPropertiesAreConfigured() {
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("create-drop");
        assertThat(environment.getProperty("spring.jpa.open-in-view")).isEqualTo("false");
    }

    @Test
    void citizenIsRegisteredAsJpaEntity() {
        EntityType<Citizen> type = entityManagerFactory.getMetamodel().entity(Citizen.class);
        assertThat(type.getName()).isEqualTo("Citizen");
        assertThat(type.getIdType().getJavaType()).isEqualTo(String.class);
    }

    @Test
    void citizensTableIsCreatedBySchemaGeneration() throws Exception {
        try (Connection connection = dataSource.getConnection();
             ResultSet tables = connection.getMetaData().getTables(null, null, "CITIZENS", null)) {
            assertThat(tables.next()).isTrue();
        }
    }

    @Test
    void jacksonSerializesAndDeserializesCitizen() throws Exception {
        Citizen c = new Citizen("AB123456", "Μαρία", "Ιωάννου", Gender.FEMALE, "01-02-1990", "123456789", "Οδός 1");
        String json = objectMapper.writeValueAsString(c);
        assertThat(json).contains("\"idNumber\":\"AB123456\"", "\"gender\":\"FEMALE\"", "\"birthDate\":\"01-02-1990\"");

        Citizen back = objectMapper.readValue(json, Citizen.class);
        assertThat(back).usingRecursiveComparison().isEqualTo(c);
    }

    @Test
    void beanValidationIsAvailable() {
        assertThat(validator.validate(new Citizen())).isNotEmpty();
    }
}
