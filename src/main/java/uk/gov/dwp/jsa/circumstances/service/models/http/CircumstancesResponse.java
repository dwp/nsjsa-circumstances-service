package uk.gov.dwp.jsa.circumstances.service.models.http;

import org.springframework.beans.BeanUtils;
import uk.gov.dwp.jsa.circumstances.service.models.db.ClaimCircumstances;

import java.util.Objects;

public class CircumstancesResponse extends CircumstancesRequest {

    public CircumstancesResponse(final ClaimCircumstances claimCircumstances) {
        Objects.requireNonNull(claimCircumstances);
        Objects.requireNonNull(claimCircumstances.getClaimCircumstancesJson());
        BeanUtils.copyProperties(claimCircumstances.getClaimCircumstancesJson(), this);
        this.setId(claimCircumstances.getId());
    }
}
