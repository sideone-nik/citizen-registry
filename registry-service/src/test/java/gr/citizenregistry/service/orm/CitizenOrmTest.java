package gr.citizenregistry.service.orm;

import gr.citizenregistry.domain.Citizen;
import gr.citizenregistry.domain.Gender;
import gr.citizenregistry.service.repository.CitizenRepository;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Δοκιμές ORM: αντιστοίχιση οντότητας-πίνακα, CRUD, αναζήτηση, περιορισμοί. */
@DataJpaTest
class CitizenOrmTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CitizenRepository repository;

    private static Citizen citizen(String id, String first, String last, Gender g) {
        return new Citizen(id, first, last, g, "12-11-2008", "123456789", "Ερμού 10, Αθήνα");
    }

    @Test
    void persistAndReloadKeepsAllFields() {
        Citizen original = citizen("AB123456", "Νίκος", "Παπαδόπουλος", Gender.MALE);
        em.persistAndFlush(original);
        em.clear();

        Citizen loaded = em.find(Citizen.class, "AB123456");
        assertThat(loaded).isNotNull().isNotSameAs(original);
        assertThat(loaded).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    void entityIsMappedToExpectedTableAndColumns() {
        em.persistAndFlush(citizen("AB123456", "Νίκος", "Παπαδόπουλος", Gender.MALE));

        Object[] row = (Object[]) em.getEntityManager()
                .createNativeQuery("SELECT id_number, first_name, last_name, CAST(gender AS VARCHAR(10)), birth_date, afm, address "
                        + "FROM citizens WHERE id_number = ?1")
                .setParameter(1, "AB123456")
                .getSingleResult();

        assertThat(row).containsExactly("AB123456", "Νίκος", "Παπαδόπουλος", "MALE", "12-11-2008",
                "123456789", "Ερμού 10, Αθήνα");
    }

    @Test
    void genderIsStoredAsString() {
        em.persistAndFlush(citizen("CD000001", "Μαρία", "Ιωάννου", Gender.FEMALE));
        Object gender = em.getEntityManager()
                .createNativeQuery("SELECT CAST(gender AS VARCHAR(10)) FROM citizens WHERE id_number = ?1")
                .setParameter(1, "CD000001")
                .getSingleResult();
        assertThat(gender).isEqualTo("FEMALE");
    }

    @Test
    void optionalColumnsAcceptNull() {
        em.persistAndFlush(new Citizen("EF000001", "Γιώργος", "Νικολάου", Gender.MALE, "01-01-1980"));
        em.clear();
        Citizen loaded = em.find(Citizen.class, "EF000001");
        assertThat(loaded.getAfm()).isNull();
        assertThat(loaded.getAddress()).isNull();
    }

    @Test
    void updateIsPersisted() {
        em.persistAndFlush(citizen("AB123456", "Νίκος", "Παπαδόπουλος", Gender.MALE));
        em.clear();

        Citizen managed = repository.findById("AB123456").orElseThrow();
        managed.setAfm("999999999");
        managed.setAddress("Νέα διεύθυνση 1");
        repository.saveAndFlush(managed);
        em.clear();

        Citizen reloaded = em.find(Citizen.class, "AB123456");
        assertThat(reloaded.getAfm()).isEqualTo("999999999");
        assertThat(reloaded.getAddress()).isEqualTo("Νέα διεύθυνση 1");
        assertThat(reloaded.getFirstName()).isEqualTo("Νίκος");
    }

    @Test
    void deleteRemovesRow() {
        repository.saveAndFlush(citizen("AB123456", "Νίκος", "Παπαδόπουλος", Gender.MALE));
        assertThat(repository.existsById("AB123456")).isTrue();

        repository.deleteById("AB123456");
        repository.flush();
        em.clear();

        assertThat(em.find(Citizen.class, "AB123456")).isNull();
        assertThat(repository.count()).isZero();
    }

    @Test
    void queryByExampleFiltersOnAnyCombinationOfFields() {
        repository.saveAll(List.of(
                citizen("AA000001", "Νίκος", "Παπαδόπουλος", Gender.MALE),
                citizen("AA000002", "Μαρία", "Παπαδόπουλος", Gender.FEMALE),
                citizen("AA000003", "Νίκος", "Γεωργίου", Gender.MALE)));
        repository.flush();

        ExampleMatcher matcher = ExampleMatcher.matchingAll().withIgnoreNullValues().withIgnoreCase();

        Citizen byLastName = new Citizen();
        byLastName.setLastName("παπαδόπουλος");
        assertThat(repository.findAll(Example.of(byLastName, matcher)))
                .extracting(Citizen::getIdNumber).containsExactlyInAnyOrder("AA000001", "AA000002");

        Citizen byNameAndGender = new Citizen();
        byNameAndGender.setFirstName("Νίκος");
        byNameAndGender.setGender(Gender.MALE);
        assertThat(repository.findAll(Example.of(byNameAndGender, matcher)))
                .extracting(Citizen::getIdNumber).containsExactlyInAnyOrder("AA000001", "AA000003");
    }

    @Test
    void duplicatePrimaryKeyIsRejected() {
        em.persistAndFlush(citizen("AB123456", "Νίκος", "Παπαδόπουλος", Gender.MALE));
        em.clear();

        Citizen duplicate = citizen("AB123456", "Άλλος", "Πολίτης", Gender.MALE);
        assertThatThrownBy(() -> em.persistAndFlush(duplicate)).isInstanceOf(PersistenceException.class);
    }

    @Test
    void beanValidationIsAppliedOnPersist() {
        Citizen invalid = citizen("AB123456", "Νίκος", "Παπαδόπουλος", Gender.MALE);
        invalid.setAfm("123");

        assertThatThrownBy(() -> em.persistAndFlush(invalid))
                .satisfies(ex -> assertThat(hasCause(ex, ConstraintViolationException.class)).isTrue());
    }

    private static boolean hasCause(Throwable t, Class<? extends Throwable> type) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (type.isInstance(c)) {
                return true;
            }
        }
        return false;
    }
}
