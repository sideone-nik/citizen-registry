package gr.citizenregistry.service.web;

import gr.citizenregistry.domain.Citizen;
import gr.citizenregistry.domain.CitizenUpdate;
import gr.citizenregistry.service.exception.InvalidInputException;
import gr.citizenregistry.service.service.CitizenService;
import gr.citizenregistry.service.service.SearchCriteria;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * RESTful διεπαφή του μητρώου πολιτών.
 * <pre>
 * POST   /api/citizens              εισαγωγή              201 / 400 / 409
 * DELETE /api/citizens/{at}         διαγραφή              204 / 400 / 404
 * PUT    /api/citizens/{at}         ενημέρωση ΑΦΜ/διεύθ.   200 / 400 / 404
 * GET    /api/citizens?πεδία...     αναζήτηση             200 / 400
 * GET    /api/citizens/{at}         εμφάνιση              200 / 400 / 404
 * </pre>
 */
@RestController
@RequestMapping("/api/citizens")
public class CitizenController {

    private final CitizenService service;

    public CitizenController(CitizenService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Citizen> create(@RequestBody Citizen citizen) {
        Citizen saved = service.create(citizen);
        return ResponseEntity.created(URI.create("/api/citizens/" + saved.getIdNumber())).body(saved);
    }

    @GetMapping("/{idNumber}")
    public Citizen getByIdNumber(@PathVariable String idNumber) {
        return service.getByIdNumber(idNumber);
    }

    @GetMapping
    public List<Citizen> search(@RequestParam(required = false) String idNumber,
                                @RequestParam(required = false) String firstName,
                                @RequestParam(required = false) String lastName,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String birthDate,
                                @RequestParam(required = false) String afm,
                                @RequestParam(required = false) String address) {
        return service.search(new SearchCriteria(idNumber, firstName, lastName, gender, birthDate, afm, address));
    }

    @PutMapping("/{idNumber}")
    public Citizen update(@PathVariable String idNumber, @RequestBody CitizenUpdate update) {
        return service.update(idNumber, update);
    }

    @DeleteMapping("/{idNumber}")
    public ResponseEntity<Void> delete(@PathVariable String idNumber) {
        service.delete(idNumber);
        return ResponseEntity.noContent().build();
    }

    /** Διαγραφή χωρίς ΑΤ -> 400. */
    @DeleteMapping
    public ResponseEntity<Void> deleteWithoutId() {
        throw new InvalidInputException("Ο αριθμός ταυτότητας (ΑΤ) είναι υποχρεωτικός");
    }
}
