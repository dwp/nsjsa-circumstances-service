package uk.gov.dwp.jsa.circumstances.service.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.dwp.jsa.circumstances.service.models.db.ClaimCircumstances;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CircumstancesRepository extends CrudRepository<ClaimCircumstances, UUID> {

    Optional<ClaimCircumstances> findByClaimantId(String claimantId);

    @Query(value = "select id, claimant_id, null claim_circumstances_json, created_timestamp, updated_timestamp, "
            + "hash, source, service_version, locale, encrypted_json "
            + "from circumstances_schema.claim_circumstances where claimant_id = ?1",
            nativeQuery = true)
    Optional<ClaimCircumstances> findByClaimantIdWithoutJson(String claimantId);


    @Query(value = "select id, claimant_id, null claim_circumstances_json, created_timestamp, updated_timestamp, "
            + "hash, source, service_version, locale, encrypted_json "
            + "from circumstances_schema.claim_circumstances where id = ?1",
            nativeQuery = true)
    Optional<ClaimCircumstances> findByIdWithoutJson(UUID id);

    @Query(value = "select claimant_id from circumstances_schema.claim_circumstances "
            + "where encrypted_json = false limit ?1",
            nativeQuery = true)
    List<String> findUnencryptedClaimantIds(int limit);

    @Query(value = "select * from circumstances_schema.claim_circumstances "
            + "where encrypted_json = false order by created_timestamp asc limit ?1",
            nativeQuery = true)
    List<ClaimCircumstances> findUnencryptedCircumstances(int limit);

}
