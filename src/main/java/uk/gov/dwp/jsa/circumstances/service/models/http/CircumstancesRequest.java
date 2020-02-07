package uk.gov.dwp.jsa.circumstances.service.models.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.dwp.jsa.adaptors.dto.claim.circumstances.Circumstances;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CircumstancesRequest extends Circumstances {

    @NotNull
    @Override
    public LocalDate getClaimStartDate() {
        return super.getClaimStartDate();
    }

    @NotNull
    @Override
    public LocalDate getDateOfClaim() {
        return super.getDateOfClaim();
    }


}
