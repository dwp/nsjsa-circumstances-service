package uk.gov.dwp.jsa.circumstances.service.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.circumstances.service.config.WithVersionUriComponentsBuilder;
import uk.gov.dwp.jsa.circumstances.service.models.http.CircumstancesRequest;
import uk.gov.dwp.jsa.circumstances.service.models.http.CircumstancesResponse;
import uk.gov.dwp.jsa.circumstances.service.services.CircumstancesService;
import uk.gov.dwp.jsa.circumstances.service.services.ResponseBuilder;
import uk.gov.dwp.jsa.security.roles.AnyRole;
import uk.gov.dwp.jsa.security.roles.WC;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromController;
import static uk.gov.dwp.jsa.circumstances.service.config.WithVersionUriComponentsBuilder.VERSION_SPEL;


@RestController
@RequestMapping("/nsjsa/" + VERSION_SPEL)
public class CircumstancesController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CircumstancesController.class);

    private final CircumstancesService circumstancesService;
    private final WithVersionUriComponentsBuilder uriBuilder;

    @Autowired
    public CircumstancesController(
            final CircumstancesService pCircumstancesService,
            final WithVersionUriComponentsBuilder uriBuilder
    ) {
        this.circumstancesService = pCircumstancesService;
        this.uriBuilder = uriBuilder;
    }

    @AnyRole
    @GetMapping("/claim/{id}")
    public ResponseEntity<ApiResponse<CircumstancesResponse>> getClaimCircumstancesById(
            @PathVariable final UUID id,
            final HttpServletRequest request
    ) {
        LOGGER.debug("Getting circumstances for id: {}", id);
        return generateResponse(
                request.getRequestURI(),
                circumstancesService.getCircumstancesById(id)
        );
    }

    @AnyRole
    @GetMapping("/citizen/{claimantId}/claim")
    public ResponseEntity<ApiResponse<CircumstancesResponse>> getClaimCircumstancesByClaimantId(
            @PathVariable final UUID claimantId,
            final HttpServletRequest request
    ) {
        LOGGER.debug("Getting circumstances for claimantId: {}", claimantId);
        return generateResponse(
                request.getRequestURI(),
                circumstancesService.getCircumstancesByClaimantId(claimantId)
        );
    }

    @PreAuthorize("!hasAnyAuthority('WC', 'SCA')")
    @PostMapping("/citizen/{claimantId}/claim")
    public ResponseEntity<ApiResponse<UUID>> createClaimCircumstances(
            @PathVariable("claimantId") final UUID claimantId,
            @RequestBody @Validated final CircumstancesRequest circumstancesRequest
    ) {
        LOGGER.debug("Creating circumstances for claimantId: {}",
                    claimantId);
        circumstancesRequest.setClaimantId(claimantId);
        final UUID resourceId = circumstancesService.save(circumstancesRequest);
        return buildSuccessfulResponse(
                buildResourceUriFor(resourceId),
                resourceId,
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasAnyAuthority('CCM', 'WC', 'CCA')")
    @PatchMapping("/claim/{id}")
    public ResponseEntity<ApiResponse<UUID>> updateClaimCircumstances(
            @PathVariable("id") final UUID id,
            @RequestBody @Validated final CircumstancesRequest circumstancesRequest
    ) {
        LOGGER.debug("Updating circumstances for id: {}", id);
        circumstancesService.update(id, circumstancesRequest);
        return buildSuccessfulResponse(
                buildResourceUriFor(id),
                id,
                HttpStatus.OK
        );
    }

    @WC
    @DeleteMapping("/claim/{id}")
    public ResponseEntity<ApiResponse<UUID>> deleteById(
            @PathVariable final UUID id
    ) {
        LOGGER.debug("Deleting circumstances for id: {}", id);
        if (nonNull(circumstancesService.getCircumstancesById(id))) {
            circumstancesService.delete(id);
            return generateResponse(
                    buildResourceUriFor(id),
                    id
            );
        } else {
            LOGGER.error("Error deleting circumstances for id: {}", id);
            return buildErrorResponse();
        }

    }

    private String buildResourceUriFor(final UUID resourceId) {
        return fromController(uriBuilder, getClass())
                .path("/claim/{id}")
                .buildAndExpand(resourceId)
                .toUri()
                .getPath();
    }

    private <T> ResponseEntity<ApiResponse<T>> generateResponse(final String path, final T objectToReturn) {
        if (objectToReturn == null) {
            return buildErrorResponse();

        } else {
            return buildSuccessfulResponse(
                    path,
                    objectToReturn,
                    HttpStatus.OK
            );
        }
    }

    private <T> ResponseEntity<ApiResponse<T>> buildSuccessfulResponse(
            final String path,
            final T objectToReturn,
            final HttpStatus status
    ) {
        return new ResponseBuilder<T>()
                .withStatus(status)
                .withSuccessData(URI.create(path), objectToReturn)
                .build();
    }

    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse() {
        return new ResponseBuilder<T>()
                .withStatus(HttpStatus.NOT_FOUND)
                .withApiError(HttpStatus.NOT_FOUND.toString(), HttpStatus.NOT_FOUND.getReasonPhrase())
                .build();
    }
}
