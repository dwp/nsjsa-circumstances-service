package uk.gov.dwp.jsa.circumstances.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.dwp.jsa.adaptors.enums.UserType;
import uk.gov.dwp.jsa.circumstances.service.exceptions.CircumstancesAlreadyExistsException;
import uk.gov.dwp.jsa.circumstances.service.models.db.ClaimCircumstances;
import uk.gov.dwp.jsa.circumstances.service.models.http.CircumstancesRequest;
import uk.gov.dwp.jsa.circumstances.service.models.http.CircumstancesResponse;
import uk.gov.dwp.jsa.circumstances.service.repositories.CircumstancesRepository;

import java.util.UUID;

@Service
public class CircumstancesService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CircumstancesService.class);

    private final CircumstancesRepository repository;
    private final ObjectMapper mapper;

    @Autowired
    public CircumstancesService(final CircumstancesRepository repository, final ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public CircumstancesResponse getCircumstancesById(final UUID id) {
        return repository.findById(id).map(CircumstancesResponse::new).orElse(null);
    }

    public CircumstancesResponse getCircumstancesByClaimantId(final UUID claimantId) {
        return repository.findByClaimantId(claimantId.toString()).map(CircumstancesResponse::new).orElse(null);
    }

    public UUID save(final CircumstancesRequest circumstancesRequest) {
        final ClaimCircumstances claimCircumstances = createCircumstancesEntityWith(circumstancesRequest);

        try {
            return repository.save(claimCircumstances).getId();
        } catch (DataIntegrityViolationException e) {
            LOGGER.error("Error saving circumstances for claimantId: {}",
                        circumstancesRequest.getClaimantId(), e);
            throw new CircumstancesAlreadyExistsException();
        }

    }


    public UUID update(final UUID uuid, final CircumstancesRequest circumstancesRequest) {
        ClaimCircumstances claimCircumstances =
                repository.findById(uuid).orElseThrow(CircumstancesAlreadyExistsException::new);
        claimCircumstances.update(circumstancesRequest,
                                  circumstancesRequest.getClaimantId(),
                                  DigestUtils.sha256Hex(createJsonFor(circumstancesRequest)),
                                  UserType.CITIZEN.toString(),
                                  circumstancesRequest.getServiceVersion(),
                                  circumstancesRequest.getLocale());

        try {
            return repository.save(claimCircumstances).getId();
        } catch (DataIntegrityViolationException e) {
            LOGGER.error("Error updating circumstances for claimantId: {}",
                        circumstancesRequest.getClaimantId(), e);
            throw new CircumstancesAlreadyExistsException();
        }

    }

    private ClaimCircumstances createCircumstancesEntityWith(final CircumstancesRequest circumstancesRequest) {
        return new ClaimCircumstances(
                circumstancesRequest,
                circumstancesRequest.getClaimantId().toString(),
                DigestUtils.sha256Hex(createJsonFor(circumstancesRequest)),
                UserType.CITIZEN.toString(),
                circumstancesRequest.getServiceVersion(),
                circumstancesRequest.getLocale()
        );
    }

    private String createJsonFor(final CircumstancesRequest circumstancesRequest) {
        String requestJson;
        try {
            requestJson = mapper.writeValueAsString(circumstancesRequest);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error creating JSON for circumstances for claimantId: {}",
                        circumstancesRequest.getClaimantId(), e);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }
        return requestJson;
    }

    public void delete(final UUID id) {
        repository.deleteById(id);
    }
}
