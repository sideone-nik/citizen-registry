package gr.citizenregistry.service.service;

/** Κριτήρια αναζήτησης· κάθε πεδίο είναι προαιρετικό (null = δεν χρησιμοποιείται). */
public record SearchCriteria(String idNumber,
                             String firstName,
                             String lastName,
                             String gender,
                             String birthDate,
                             String afm,
                             String address) {

    public static SearchCriteria empty() {
        return new SearchCriteria(null, null, null, null, null, null, null);
    }
}
