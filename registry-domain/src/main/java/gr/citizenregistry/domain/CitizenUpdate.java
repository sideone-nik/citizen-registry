package gr.citizenregistry.domain;

import gr.citizenregistry.domain.validation.CitizenRules;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Σώμα αιτήματος ενημέρωσης: μόνο ΑΦΜ και διεύθυνση κατοικίας.
 * Τιμή null (ή κενή) σημαίνει διαγραφή της αντίστοιχης τιμής.
 */
public class CitizenUpdate {

    @Pattern(regexp = CitizenRules.AFM_REGEX, message = "Το ΑΦΜ πρέπει να αποτελείται από ακριβώς 9 ψηφία")
    private String afm;

    @Size(max = 255, message = "Η διεύθυνση δεν μπορεί να ξεπερνά τους 255 χαρακτήρες")
    private String address;

    public CitizenUpdate() {
    }

    public CitizenUpdate(String afm, String address) {
        this.afm = afm;
        this.address = address;
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

    @Override
    public String toString() {
        return "CitizenUpdate{afm='" + afm + "', address='" + address + "'}";
    }
}
