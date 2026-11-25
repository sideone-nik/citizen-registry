package gr.citizenregistry.it;

import gr.citizenregistry.service.CitizenRegistryApplication;
import gr.citizenregistry.service.repository.CitizenRepository;
import io.restassured.RestAssured;
import io.restassured.config.DecoderConfig;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.nullValue;

/**
 * Δοκιμές ενοποίησης (Rest-Assured) και για τις 5 μεθόδους της RESTful υπηρεσίας:
 * εισαγωγή, διαγραφή, ενημέρωση, αναζήτηση, εμφάνιση.
 */
@SpringBootTest(
        classes = CitizenRegistryApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:citizens-it;DB_CLOSE_DELAY=-1",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.h2.console.enabled=false"
        })
class CitizenApiIT {

    private static final String BASE = "/api/citizens";

    @LocalServerPort
    private int port;

    @Autowired
    private CitizenRepository repository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.config = RestAssuredConfig.config()
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"))
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        repository.deleteAll();
    }

    private static Map<String, Object> citizen(String id, String first, String last, String gender,
                                               String birthDate, String afm, String address) {
        Map<String, Object> m = new HashMap<>();
        m.put("idNumber", id);
        m.put("firstName", first);
        m.put("lastName", last);
        m.put("gender", gender);
        m.put("birthDate", birthDate);
        m.put("afm", afm);
        m.put("address", address);
        return m;
    }

    private static Map<String, Object> nikos() {
        return citizen("AB123456", "Νίκος", "Παπαδόπουλος", "MALE", "12-11-2008", "123456789", "Ερμού 10, Αθήνα");
    }

    private static void insert(Map<String, Object> body) {
        given().contentType(ContentType.JSON).body(body)
                .when().post(BASE)
                .then().statusCode(201);
    }

    // =================================================================== 1. Εισαγωγή

    @Nested
    @DisplayName("POST /api/citizens - εισαγωγή")
    class Create {

        @Test
        void createsCitizen() {
            given().contentType(ContentType.JSON).body(nikos())
                    .when().post(BASE)
                    .then().statusCode(201)
                    .header("Location", endsWith("/api/citizens/AB123456"))
                    .body("idNumber", equalTo("AB123456"))
                    .body("firstName", equalTo("Νίκος"))
                    .body("lastName", equalTo("Παπαδόπουλος"))
                    .body("gender", equalTo("MALE"))
                    .body("birthDate", equalTo("12-11-2008"))
                    .body("afm", equalTo("123456789"))
                    .body("address", equalTo("Ερμού 10, Αθήνα"));
        }

        @Test
        void createsCitizenWithoutOptionalFields() {
            given().contentType(ContentType.JSON)
                    .body(citizen("XY000001", "Μαρία", "Ιωάννου", "FEMALE", "01-02-1990", null, null))
                    .when().post(BASE)
                    .then().statusCode(201)
                    .body("afm", nullValue())
                    .body("address", nullValue());
        }

        @Test
        void rejectsDuplicate() {
            insert(nikos());
            given().contentType(ContentType.JSON).body(nikos())
                    .when().post(BASE)
                    .then().statusCode(409)
                    .body("status", equalTo(409))
                    .body("messages[0]", containsString("AB123456"));
        }

        @Test
        void rejectsMissingMandatoryFields() {
            given().contentType(ContentType.JSON).body("{}")
                    .when().post(BASE)
                    .then().statusCode(400)
                    .body("status", equalTo(400))
                    .body("messages", hasItem(containsString("ΑΤ")))
                    .body("messages", hasItem(containsString("όνομα")))
                    .body("messages", hasItem(containsString("επίθετο")))
                    .body("messages", hasItem(containsString("φύλο")))
                    .body("messages", hasItem(containsString("ημερομηνία γέννησης")));
        }

        @Test
        void rejectsInvalidValues() {
            given().contentType(ContentType.JSON)
                    .body(citizen("AB12", "Νίκος", "Παπαδόπουλος", "MALE", "2008-11-12", "12AB", null))
                    .when().post(BASE)
                    .then().statusCode(400)
                    .body("messages", hasSize(3))
                    .body("messages", hasItem(containsString("8 χαρακτήρες")))
                    .body("messages", hasItem(containsString("ΗΗ-ΜΜ-ΕΕΕΕ")))
                    .body("messages", hasItem(containsString("9 ψηφία")));
            given().when().get(BASE).then().statusCode(200).body("$", empty());
        }

        @Test
        void rejectsInvalidGender() {
            Map<String, Object> body = nikos();
            body.put("gender", "UNKNOWN");
            given().contentType(ContentType.JSON).body(body)
                    .when().post(BASE)
                    .then().statusCode(400);
        }
    }

    // =================================================================== 2. Διαγραφή

    @Nested
    @DisplayName("DELETE /api/citizens/{at} - διαγραφή")
    class Delete {

        @Test
        void deletesExistingCitizen() {
            insert(nikos());
            given().when().delete(BASE + "/{id}", "AB123456").then().statusCode(204);
            given().when().get(BASE + "/{id}", "AB123456").then().statusCode(404);
        }

        @Test
        void nonExistingIdReturns404() {
            given().when().delete(BASE + "/{id}", "ZZ999999")
                    .then().statusCode(404)
                    .body("messages[0]", containsString("ZZ999999"));
        }

        @Test
        void invalidIdReturns400() {
            given().when().delete(BASE + "/{id}", "123")
                    .then().statusCode(400)
                    .body("messages[0]", containsString("8 χαρακτήρες"));
        }

        @Test
        void blankIdReturns400() {
            given().when().delete(BASE + "/{id}", "   ")
                    .then().statusCode(400)
                    .body("messages[0]", containsString("υποχρεωτικός"));
        }

        @Test
        void missingIdReturns400() {
            given().when().delete(BASE)
                    .then().statusCode(400)
                    .body("messages[0]", containsString("υποχρεωτικός"));
        }
    }

    // =================================================================== 3. Ενημέρωση

    @Nested
    @DisplayName("PUT /api/citizens/{at} - ενημέρωση ΑΦΜ / διεύθυνσης")
    class Update {

        @Test
        void updatesAfmAndAddressOnly() {
            insert(nikos());
            given().contentType(ContentType.JSON)
                    .body(Map.of("afm", "987654321", "address", "Τσιμισκή 5, Θεσσαλονίκη",
                            "firstName", "ΔενΠρεπείΝαΑλλάξει"))
                    .when().put(BASE + "/{id}", "AB123456")
                    .then().statusCode(200)
                    .body("afm", equalTo("987654321"))
                    .body("address", equalTo("Τσιμισκή 5, Θεσσαλονίκη"))
                    .body("firstName", equalTo("Νίκος"));

            given().when().get(BASE + "/{id}", "AB123456")
                    .then().statusCode(200)
                    .body("afm", equalTo("987654321"))
                    .body("address", equalTo("Τσιμισκή 5, Θεσσαλονίκη"))
                    .body("lastName", equalTo("Παπαδόπουλος"))
                    .body("birthDate", equalTo("12-11-2008"));
        }

        @Test
        void invalidAfmIsRejectedAndNothingChanges() {
            insert(nikos());
            given().contentType(ContentType.JSON)
                    .body(Map.of("afm", "12345", "address", "Άλλη"))
                    .when().put(BASE + "/{id}", "AB123456")
                    .then().statusCode(400)
                    .body("messages[0]", containsString("9 ψηφία"));

            given().when().get(BASE + "/{id}", "AB123456")
                    .then().body("afm", equalTo("123456789"))
                    .body("address", equalTo("Ερμού 10, Αθήνα"));
        }

        @Test
        void nonExistingCitizenReturns404() {
            given().contentType(ContentType.JSON).body(Map.of("afm", "987654321"))
                    .when().put(BASE + "/{id}", "ZZ999999")
                    .then().statusCode(404);
        }

        @Test
        void invalidIdReturns400() {
            given().contentType(ContentType.JSON).body(Map.of("afm", "987654321"))
                    .when().put(BASE + "/{id}", "AB1")
                    .then().statusCode(400);
        }
    }

    // =================================================================== 4. Αναζήτηση

    @Nested
    @DisplayName("GET /api/citizens?... - αναζήτηση")
    class Search {

        @BeforeEach
        void seed() {
            insert(citizen("AA000001", "Νίκος", "Παπαδόπουλος", "MALE", "12-11-2008", "111111111", "Ερμού 10, Αθήνα"));
            insert(citizen("AA000002", "Μαρία", "Παπαδόπουλος", "FEMALE", "01-02-1990", "222222222", "Τσιμισκή 5, Θεσσαλονίκη"));
            insert(citizen("AA000003", "Νίκος", "Γεωργίου", "MALE", "01-02-1990", null, "Ερμού 22, Αθήνα"));
        }

        @Test
        void noCriteriaReturnsAll() {
            given().when().get(BASE)
                    .then().statusCode(200)
                    .body("idNumber", contains("AA000001", "AA000002", "AA000003"));
        }

        @Test
        void singleFieldReturnsMultipleMatches() {
            given().queryParam("lastName", "Παπαδόπουλος")
                    .when().get(BASE)
                    .then().statusCode(200)
                    .body("idNumber", containsInAnyOrder("AA000001", "AA000002"));
        }

        @Test
        void combinationOfFields() {
            given().queryParam("firstName", "Νίκος").queryParam("birthDate", "01-02-1990")
                    .when().get(BASE)
                    .then().statusCode(200)
                    .body("idNumber", contains("AA000003"));
        }

        @Test
        void byGenderAndAddressFragment() {
            given().queryParam("gender", "MALE").queryParam("address", "αθήνα")
                    .when().get(BASE)
                    .then().statusCode(200)
                    .body("idNumber", containsInAnyOrder("AA000001", "AA000003"));
        }

        @Test
        void byAfmAndId() {
            given().queryParam("afm", "222222222").when().get(BASE)
                    .then().statusCode(200).body("idNumber", contains("AA000002"));
            given().queryParam("idNumber", "AA000001").when().get(BASE)
                    .then().statusCode(200).body("idNumber", contains("AA000001"));
        }

        @Test
        void noMatchesReturnsEmptyList() {
            given().queryParam("lastName", "Ανύπαρκτος")
                    .when().get(BASE)
                    .then().statusCode(200)
                    .body("$", empty());
        }

        @Test
        void invalidValuesReturn400() {
            given().queryParam("afm", "12").queryParam("birthDate", "31-02-2000").queryParam("gender", "X")
                    .when().get(BASE)
                    .then().statusCode(400)
                    .body("messages", hasSize(3));
            given().queryParam("idNumber", "123").when().get(BASE).then().statusCode(400);
            given().queryParam("firstName", " ").when().get(BASE).then().statusCode(400);
        }
    }

    // =================================================================== 5. Εμφάνιση

    @Nested
    @DisplayName("GET /api/citizens/{at} - εμφάνιση")
    class Show {

        @Test
        void returnsCitizen() {
            insert(nikos());
            given().when().get(BASE + "/{id}", "AB123456")
                    .then().statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("idNumber", equalTo("AB123456"))
                    .body("firstName", equalTo("Νίκος"))
                    .body("gender", equalTo("MALE"));
        }

        @Test
        void nonExistingReturns404() {
            given().when().get(BASE + "/{id}", "ZZ999999")
                    .then().statusCode(404)
                    .body("status", equalTo(404))
                    .body("messages", not(empty()));
        }

        @Test
        void invalidIdReturns400() {
            given().when().get(BASE + "/{id}", "ABC")
                    .then().statusCode(400)
                    .body("messages[0]", containsString("8 χαρακτήρες"));
        }
    }
}
