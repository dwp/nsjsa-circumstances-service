package uk.gov.dwp.jsa.circumstances.service.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.dwp.jsa.circumstances.service.models.db.ClaimCircumstances;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CircumstancesRepository extends CrudRepository<ClaimCircumstances, UUID> {

    Optional<ClaimCircumstances> findByClaimantId(String claimantId);

}
