package gr.citizenregistry.service.entity;

import gr.citizenregistry.domain.Citizen;
import gr.citizenregistry.domain.CitizenUpdate;
import gr.citizenregistry.domain.Gender;
import gr.citizenregistry.domain.validation.CitizenRules;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Δοκιμές μονάδων στις κλάσεις οντοτήτων: δομητές, μέθοδοι, επικύρωση δεδομένων. */
class CitizenEntityTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private static Citizen validCitizen() {
        return new Citizen("AB123456", "Νίκος", "Παπαδόπουλος", Gender.MALE,
                "12-11-2008", "123456789", "Ερμού 10, Αθήνα");
    }

    private static <T> Set<String> violatedProperties(T object) {
        return validator.validate(object).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    // ------------------------------------------------------------------ δομητές & μέθοδοι

    @Nested
    @DisplayName("Δομητές και μέθοδοι της Citizen")
    class ConstructorsAndMethods {

        @Test
        void noArgConstructorLeavesAllFieldsNull() {
            Citizen c = new Citizen();
            assertThat(c.getIdNumber()).isNull();
            assertThat(c.getFirstName()).isNull();
            assertThat(c.getLastName()).isNull();
            assertThat(c.getGender()).isNull();
            assertThat(c.getBirthDate()).isNull();
            assertThat(c.getAfm()).isNull();
            assertThat(c.getAddress()).isNull();
        }

        @Test
        void requiredFieldsConstructorSetsMandatoryFieldsOnly() {
            Citizen c = new Citizen("XY987654", "Μαρία", "Ιωάννου", Gender.FEMALE, "01-02-1990");
            assertThat(c.getIdNumber()).isEqualTo("XY987654");
            assertThat(c.getFirstName()).isEqualTo("Μαρία");
            assertThat(c.getLastName()).isEqualTo("Ιωάννου");
            assertThat(c.getGender()).isEqualTo(Gender.FEMALE);
            assertThat(c.getBirthDate()).isEqualTo("01-02-1990");
            assertThat(c.getAfm()).isNull();
            assertThat(c.getAddress()).isNull();
        }

        @Test
        void fullConstructorSetsAllFields() {
            Citizen c = validCitizen();
            assertThat(c.getIdNumber()).isEqualTo("AB123456");
            assertThat(c.getFirstName()).isEqualTo("Νίκος");
            assertThat(c.getLastName()).isEqualTo("Παπαδόπουλος");
            assertThat(c.getGender()).isEqualTo(Gender.MALE);
            assertThat(c.getBirthDate()).isEqualTo("12-11-2008");
            assertThat(c.getAfm()).isEqualTo("123456789");
            assertThat(c.getAddress()).isEqualTo("Ερμού 10, Αθήνα");
        }

        @Test
        void settersUpdateFields() {
            Citizen c = new Citizen();
            c.setIdNumber("CD111222");
            c.setFirstName("Γιώργος");
            c.setLastName("Νικολάου");
            c.setGender(Gender.MALE);
            c.setBirthDate("05-06-1985");
            c.setAfm("987654321");
            c.setAddress("Τσιμισκή 5");
            assertThat(c).extracting(Citizen::getIdNumber, Citizen::getFirstName, Citizen::getLastName,
                            Citizen::getGender, Citizen::getBirthDate, Citizen::getAfm, Citizen::getAddress)
                    .containsExactly("CD111222", "Γιώργος", "Νικολάου", Gender.MALE, "05-06-1985",
                            "987654321", "Τσιμισκή 5");
        }

        @Test
        void equalityIsBasedOnIdNumber() {
            Citizen a = validCitizen();
            Citizen b = new Citizen("AB123456", "Άλλο", "Όνομα", Gender.FEMALE, "01-01-2000");
            Citizen c = new Citizen("ZZ000000", "Νίκος", "Παπαδόπουλος", Gender.MALE, "12-11-2008");
            assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
            assertThat(a).isNotEqualTo(c);
            assertThat(a).isNotEqualTo(null).isNotEqualTo("AB123456");
            assertThat(a).isEqualTo(a);
        }

        @Test
        void citizensWithoutIdAreNotEqual() {
            assertThat(new Citizen()).isNotEqualTo(new Citizen());
        }

        @Test
        void toStringContainsKeyFields() {
            assertThat(validCitizen().toString()).contains("AB123456", "Νίκος", "Παπαδόπουλος", "MALE");
        }

        @Test
        void citizenUpdateConstructorsAndAccessors() {
            CitizenUpdate u = new CitizenUpdate("123456789", "Διεύθυνση 1");
            assertThat(u.getAfm()).isEqualTo("123456789");
            assertThat(u.getAddress()).isEqualTo("Διεύθυνση 1");
            CitizenUpdate empty = new CitizenUpdate();
            empty.setAfm("111111111");
            empty.setAddress("Νέα");
            assertThat(empty.getAfm()).isEqualTo("111111111");
            assertThat(empty.getAddress()).isEqualTo("Νέα");
        }
    }

    // ------------------------------------------------------------------ επικύρωση

    @Nested
    @DisplayName("Επικύρωση δεδομένων Citizen")
    class Validation_ {

        @Test
        void validCitizenHasNoViolations() {
            assertThat(validator.validate(validCitizen())).isEmpty();
        }

        @Test
        void optionalFieldsMayBeNull() {
            Citizen c = new Citizen("AB123456", "Νίκος", "Παπαδόπουλος", Gender.MALE, "12-11-2008");
            assertThat(validator.validate(c)).isEmpty();
        }

        @Test
        void emptyCitizenReportsAllMandatoryFields() {
            assertThat(violatedProperties(new Citizen()))
                    .containsExactlyInAnyOrder("idNumber", "firstName", "lastName", "gender", "birthDate");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "AB12345", "AB1234567", "AB12-456", "AB 12345", "12345678901"})
        void invalidIdNumberIsRejected(String id) {
            Citizen c = validCitizen();
            c.setIdNumber(id);
            assertThat(violatedProperties(c)).containsExactly("idNumber");
        }

        @ParameterizedTest
        @ValueSource(strings = {"AB123456", "12345678", "ΑΒ123456", "abcdefgh"})
        void validIdNumberIsAccepted(String id) {
            Citizen c = validCitizen();
            c.setIdNumber(id);
            assertThat(validator.validate(c)).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  "})
        void blankFirstNameIsRejected(String value) {
            Citizen c = validCitizen();
            c.setFirstName(value);
            assertThat(violatedProperties(c)).containsExactly("firstName");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  "})
        void blankLastNameIsRejected(String value) {
            Citizen c = validCitizen();
            c.setLastName(value);
            assertThat(violatedProperties(c)).containsExactly("lastName");
        }

        @Test
        void missingGenderIsRejected() {
            Citizen c = validCitizen();
            c.setGender(null);
            assertThat(violatedProperties(c)).containsExactly("gender");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"2008-11-12", "12/11/2008", "1-1-2008", "12-11-08", "31-02-2020",
                "29-02-2023", "00-01-2000", "12-13-2000", "32-01-2000", "ab-cd-efgh"})
        void invalidBirthDateIsRejected(String date) {
            Citizen c = validCitizen();
            c.setBirthDate(date);
            assertThat(violatedProperties(c)).containsExactly("birthDate");
        }

        @Test
        void futureBirthDateIsRejected() {
            Citizen c = validCitizen();
            c.setBirthDate(LocalDate.now().plusDays(1).format(CitizenRules.BIRTH_DATE_FORMATTER));
            assertThat(violatedProperties(c)).containsExactly("birthDate");
        }

        @ParameterizedTest
        @ValueSource(strings = {"29-02-2024", "01-01-1900", "31-12-1999"})
        void validBirthDateIsAccepted(String date) {
            Citizen c = validCitizen();
            c.setBirthDate(date);
            assertThat(validator.validate(c)).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "12345678", "1234567890", "12345678A", "ΑΦΜ123456", "123 45678"})
        void invalidAfmIsRejected(String afm) {
            Citizen c = validCitizen();
            c.setAfm(afm);
            assertThat(violatedProperties(c)).containsExactly("afm");
        }

        @Test
        void tooLongAddressIsRejected() {
            Citizen c = validCitizen();
            c.setAddress("x".repeat(256));
            assertThat(violatedProperties(c)).containsExactly("address");
        }

        @Test
        void violationMessagesAreDescriptive() {
            Citizen c = validCitizen();
            c.setAfm("123");
            Set<ConstraintViolation<Citizen>> v = validator.validate(c);
            assertThat(v).singleElement()
                    .extracting(ConstraintViolation::getMessage)
                    .isEqualTo("Το ΑΦΜ πρέπει να αποτελείται από ακριβώς 9 ψηφία");
        }

        @Test
        void citizenUpdateValidation() {
            assertThat(validator.validate(new CitizenUpdate("123456789", "Οδός"))).isEmpty();
            assertThat(validator.validate(new CitizenUpdate(null, null))).isEmpty();
            assertThat(violatedProperties(new CitizenUpdate("12AB", null))).containsExactly("afm");
        }
    }

    // ------------------------------------------------------------------ Gender & κανόνες

    @Nested
    @DisplayName("Gender και CitizenRules")
    class GenderAndRules {

        @ParameterizedTest
        @ValueSource(strings = {"MALE", "male", "M", "m", " Male "})
        void maleValuesAreParsed(String v) {
            assertThat(Gender.fromString(v)).isEqualTo(Gender.MALE);
        }

        @ParameterizedTest
        @ValueSource(strings = {"FEMALE", "female", "F", "f"})
        void femaleValuesAreParsed(String v) {
            assertThat(Gender.fromString(v)).isEqualTo(Gender.FEMALE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"X", "άνδρας", "unknown"})
        void invalidGenderThrows(String v) {
            assertThatThrownBy(() -> Gender.fromString(v)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rulesHelpers() {
            assertThat(CitizenRules.isValidIdNumber("AB123456")).isTrue();
            assertThat(CitizenRules.isValidIdNumber(null)).isFalse();
            assertThat(CitizenRules.isValidAfm("123456789")).isTrue();
            assertThat(CitizenRules.isValidAfm(null)).isFalse();
            assertThat(CitizenRules.isValidBirthDate("12-11-2008")).isTrue();
            assertThat(CitizenRules.isValidBirthDate(null)).isFalse();
            assertThat(CitizenRules.isNotBlank(" x ")).isTrue();
            assertThat(CitizenRules.isNotBlank("  ")).isFalse();
            assertThat(CitizenRules.isNotBlank(null)).isFalse();
        }
    }
}
