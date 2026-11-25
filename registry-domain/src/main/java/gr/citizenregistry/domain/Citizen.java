package gr.citizenregistry.domain;

import gr.citizenregistry.domain.validation.CitizenRules;
import gr.citizenregistry.domain.validation.ValidBirthDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/** Εγγραφή πολίτη στο μητρώο (κλάση πεδίου / οντότητας JPA). */
@Entity
@Table(name = "citizens")
public class Citizen {

    @Id
    @Column(name = "id_number", length = 8, nullable = false, updatable = false)
    @NotBlank(message = "Ο αριθμός ταυτότητας (ΑΤ) είναι υποχρεωτικός")
    @Pattern(regexp = CitizenRules.ID_NUMBER_REGEX,
            message = "Ο αριθμός ταυτότητας (ΑΤ) πρέπει να αποτελείται από ακριβώς 8 χαρακτήρες (γράμματα ή ψηφία)")
    private String idNumber;

    @Column(name = "first_name", length = 100, nullable = false)
    @NotBlank(message = "Το όνομα είναι υποχρεωτικό")
    @Size(max = 100, message = "Το όνομα δεν μπορεί να ξεπερνά τους 100 χαρακτήρες")
    private String firstName;

    @Column(name = "last_name", length = 100, nullable = false)
    @NotBlank(message = "Το επίθετο είναι υποχρεωτικό")
    @Size(max = 100, message = "Το επίθετο δεν μπορεί να ξεπερνά τους 100 χαρακτήρες")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10, nullable = false)
    @NotNull(message = "Το φύλο είναι υποχρεωτικό")
    private Gender gender;

    @Column(name = "birth_date", length = 10, nullable = false)
    @NotBlank(message = "Η ημερομηνία γέννησης είναι υποχρεωτική")
    @ValidBirthDate
    private String birthDate;

    @Column(name = "afm", length = 9)
    @Pattern(regexp = CitizenRules.AFM_REGEX, message = "Το ΑΦΜ πρέπει να αποτελείται από ακριβώς 9 ψηφία")
    private String afm;

    @Column(name = "address", length = 255)
    @Size(max = 255, message = "Η διεύθυνση δεν μπορεί να ξεπερνά τους 255 χαρακτήρες")
    private String address;

    /** Απαιτείται από JPA και Jackson. */
    public Citizen() {
    }

    /** Δομητής μόνο με τα υποχρεωτικά πεδία. */
    public Citizen(String idNumber, String firstName, String lastName, Gender gender, String birthDate) {
        this(idNumber, firstName, lastName, gender, birthDate, null, null);
    }

    /** Πλήρης δομητής. */
    public Citizen(String idNumber, String firstName, String lastName, Gender gender,
                   String birthDate, String afm, String address) {
        this.idNumber = idNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.afm = afm;
        this.address = address;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAfm() {
        return afm;
    }

    public void setAfm(String afm) {
        this.afm = afm;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /** Δύο εγγραφές θεωρούνται ίδιες όταν έχουν τον ίδιο ΑΤ. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Citizen other)) {
            return false;
        }
        return idNumber != null && idNumber.equals(other.idNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idNumber);
    }

    @Override
    public String toString() {
        return "Citizen{idNumber='" + idNumber + "', firstName='" + firstName + "', lastName='" + lastName
                + "', gender=" + gender + ", birthDate='" + birthDate + "', afm='" + afm
                + "', address='" + address + "'}";
    }
}
