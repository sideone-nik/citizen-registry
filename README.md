# Μητρώο Πολιτών — RESTful υπηρεσία (Spring Boot / Maven)

Σύνθετο Maven έργο με 4 υπο-έργα:

| Υπο-έργο | Περιεχόμενο |
|---|---|
| `registry-domain` | Κλάσεις πεδίου / οντοτήτων (`Citizen`, `CitizenUpdate`, `Gender`, `ApiError`, κανόνες επικύρωσης) |
| `registry-service` | RESTful υπηρεσία (Spring Boot, Spring Data JPA, H2) + δοκιμές μονάδων, ρύθμισης και ORM (JUnit 5) |
| `registry-client` | Πελάτης κονσόλας με επαναληπτικό μενού (Spring Boot, RestTemplate) |
| `registry-integration-tests` | Δοκιμές ενοποίησης Rest-Assured (failsafe) και για τις 5 μεθόδους |

Απαιτήσεις: JDK 17+, Maven 3.6.3+.

## Build και δοκιμές

```bash
mvn clean verify          # compile + δοκιμές μονάδων (surefire) + δοκιμές ενοποίησης (failsafe)
mvn clean install -DskipTests
```

## Εκτέλεση

```bash
# 1. Υπηρεσία (θύρα 8080, δεδομένα στο ./data/citizen-registry.mv.db)
java -jar registry-service/target/registry-service-1.0.0-exec.jar

# 2. Πελάτης (σε άλλο τερματικό)
java -jar registry-client/target/registry-client-1.0.0.jar
```

Κονσόλα βάσης: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/citizen-registry`, χρήστης `sa`).

## REST API

| Μέθοδος | URL | Λειτουργία | Κωδικοί |
|---|---|---|---|
| POST | `/api/citizens` | Εισαγωγή | 201, 400, 409 |
| DELETE | `/api/citizens/{at}` | Διαγραφή | 204, 400, 404 |
| PUT | `/api/citizens/{at}` | Ενημέρωση ΑΦΜ/διεύθυνσης | 200, 400, 404 |
| GET | `/api/citizens?idNumber=&firstName=&lastName=&gender=&birthDate=&afm=&address=` | Αναζήτηση (οποιοσδήποτε συνδυασμός) | 200, 400 |
| GET | `/api/citizens/{at}` | Εμφάνιση | 200, 400, 404 |

Παράδειγμα σώματος εισαγωγής:

```json
{
  "idNumber": "AB123456",
  "firstName": "Νίκος",
  "lastName": "Παπαδόπουλος",
  "gender": "MALE",
  "birthDate": "12-11-2008",
  "afm": "123456789",
  "address": "Ερμού 10, Αθήνα"
}
```

Κανόνες: ΑΤ ακριβώς 8 χαρακτήρες (γράμματα/ψηφία)· όνομα, επίθετο, φύλο (`MALE`/`FEMALE`), ημερομηνία γέννησης υποχρεωτικά·
ημερομηνία `ΗΗ-ΜΜ-ΕΕΕΕ`, υπαρκτή και όχι μελλοντική· ΑΦΜ προαιρετικό, ακριβώς 9 ψηφία. Στην αναζήτηση, όνομα/επίθετο
ταιριάζουν ακριβώς χωρίς διάκριση πεζών-κεφαλαίων και η διεύθυνση με «περιέχει». Τα λάθη επιστρέφονται ως
`{"timestamp","status","error","messages":[...]}`.

## Δοκιμές

- `registry-service/src/test/.../entity/CitizenEntityTest` — δοκιμές μονάδων οντοτήτων (δομητές, μέθοδοι, επικύρωση)
- `registry-service/src/test/.../config/ApplicationConfigurationTest` — δοκιμές ρύθμισης
- `registry-service/src/test/.../orm/CitizenOrmTest` — δοκιμές ORM (`@DataJpaTest`)
- `registry-integration-tests/src/test/.../it/CitizenApiIT` — δοκιμές ενοποίησης Rest-Assured (5/5 μέθοδοι)
