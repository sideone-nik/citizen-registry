package gr.citizenregistry.service.repository;

import gr.citizenregistry.domain.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Πρόσβαση στη βάση δεδομένων (σειριακοποίηση / αποσειριακοποίηση των εγγραφών). */
@Repository
public interface CitizenRepository extends JpaRepository<Citizen, String> {
}
