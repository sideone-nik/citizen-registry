package gr.citizenregistry.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.citizenregistry.domain.ApiError;
import gr.citizenregistry.domain.Citizen;
import gr.citizenregistry.domain.CitizenUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Κλήσεις προς τη RESTful υπηρεσία, με επαναχρησιμοποίηση των κλάσεων πεδίου. */
@Component
public class RegistryClient {

    private final RestTemplate rest;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public RegistryClient(RestTemplateBuilder builder, ObjectMapper mapper,
                          @Value("${registry.base-url}") String baseUrl) {
        this.rest = builder.build();
        this.mapper = mapper;
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Citizen create(Citizen citizen) {
        return execute(() -> rest.postForObject(baseUrl, citizen, Citizen.class));
    }

    public Citizen get(String idNumber) {
        return execute(() -> rest.getForObject(baseUrl + "/{id}", Citizen.class, idNumber));
    }

    public List<Citizen> search(Map<String, String> criteria) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl);
        for (Map.Entry<String, String> e : criteria.entrySet()) {
            builder.queryParam(e.getKey(), e.getValue());
        }
        URI uri = builder.build().encode().toUri();
        Citizen[] result = execute(() -> rest.getForObject(uri, Citizen[].class));
        return result == null ? List.of() : List.of(result);
    }

    public Citizen update(String idNumber, CitizenUpdate update) {
        return execute(() -> rest.exchange(baseUrl + "/{id}", HttpMethod.PUT, new HttpEntity<>(update),
                Citizen.class, idNumber).getBody());
    }

    public void delete(String idNumber) {
        execute(() -> {
            rest.delete(baseUrl + "/{id}", idNumber);
            return null;
        });
    }

    private <T> T execute(Supplier<T> call) {
        try {
            return call.get();
        } catch (HttpStatusCodeException e) {
            throw new RegistryApiException(e.getStatusCode().value(), extractMessages(e));
        }
    }

    private List<String> extractMessages(HttpStatusCodeException e) {
        try {
            ApiError error = mapper.readValue(e.getResponseBodyAsString(StandardCharsets.UTF_8), ApiError.class);
            if (error.messages() != null && !error.messages().isEmpty()) {
                return error.messages();
            }
        } catch (Exception ignored) {
            // μη αναμενόμενη μορφή σώματος - επιστρέφουμε το γενικό μήνυμα
        }
        return List.of(e.getStatusCode().toString());
    }
}
