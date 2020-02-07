package uk.gov.dwp.jsa.circumstances.service.models.db;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.junit.Test;
import uk.gov.dwp.jsa.circumstances.service.models.http.CircumstancesRequest;

import javax.persistence.Column;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ClaimCircumstancesTest {

    public static final UUID ID = UUID.randomUUID();
    public static final CircumstancesRequest CLAIM_CIRCUMSTANCES_JSON = new CircumstancesRequest();
    public static final String VERSION = "VERSION";
    public static final String LOCALE = "LOCALE";
    public static final String SOURCE = "SOURCE";
    public static final String HASH = "HASH";
    public static final String CLAIMANT_ID = UUID.randomUUID().toString();
    private static final LocalDateTime CREATED_TIME_STAMP = LocalDateTime.now();
    private static final LocalDateTime UPDATED_TIME_STAMP = LocalDateTime.now();

    private ClaimCircumstances claimCircumstances;

    @Test
    public void setsClaimCircumstancesJson() {
        givenADefaultClaimCircumstances();
        claimCircumstances.setClaimCircumstancesJson(CLAIM_CIRCUMSTANCES_JSON);
        assertThat(CLAIM_CIRCUMSTANCES_JSON, is(claimCircumstances.getClaimCircumstancesJson()));
    }

    @Test
    public void updates() {
        givenADefaultClaimCircumstances();
        claimCircumstances.update(CLAIM_CIRCUMSTANCES_JSON, UUID.fromString(CLAIMANT_ID), HASH, SOURCE, VERSION, LOCALE);
        assertThat(CLAIMANT_ID, is(claimCircumstances.getClaimantId()));
        assertThat(CLAIM_CIRCUMSTANCES_JSON, is(claimCircumstances.getClaimCircumstancesJson()));
        assertThat(VERSION, is(claimCircumstances.getServiceVersion()));
        assertThat(HASH, is(claimCircumstances.getHash()));
        assertThat(SOURCE, is(claimCircumstances.getSource()));
        assertThat(Locale.forLanguageTag(LOCALE), is(claimCircumstances.getLocale()));
    }

    @Test
    public void setsId() {
        givenADefaultClaimCircumstances();
        claimCircumstances.setId(ID);
        assertThat(ID, is(claimCircumstances.getId()));
    }

    @Test
    public void setsCreatedTimestamp() {
        givenADefaultClaimCircumstances();
        claimCircumstances.setCreatedTimestamp(CREATED_TIME_STAMP);
        assertThat(CREATED_TIME_STAMP, is(claimCircumstances.getCreatedTimestamp()));
    }

    @Test
    public void setsUpdatedTimestamp() {
        givenADefaultClaimCircumstances();
        claimCircumstances.setUpdatedTimestamp(UPDATED_TIME_STAMP);
        assertThat(UPDATED_TIME_STAMP, is(claimCircumstances.getUpdatedTimestamp()));
    }

    @Test
    public void setsClaimantId() {
        givenADefaultClaimCircumstances();
        claimCircumstances.setClaimantId(CLAIMANT_ID);
        assertThat(CLAIMANT_ID, is(claimCircumstances.getClaimantId()));
    }

    @Test
    public void setsHash() {
        givenADefaultClaimCircumstances();
        claimCircumstances.setHash(HASH);
        assertThat(HASH, is(claimCircumstances.getHash()));
    }

    @Test
    public void setsVersion() {
        givenADefaultClaimCircumstances();
        claimCircumstances.setServiceVersion(VERSION);
        assertThat(VERSION, is(claimCircumstances.getServiceVersion()));
    }

    @Test
    public void setsSource() {
        givenADefaultClaimCircumstances();
        claimCircumstances.setSource(SOURCE);
        assertThat(SOURCE, is(claimCircumstances.getSource()));
    }

    @Test
    public void setsLocale() {
        givenADefaultClaimCircumstances();
        claimCircumstances.setLocale(Locale.forLanguageTag(LOCALE));
        assertThat(Locale.forLanguageTag(LOCALE), is(claimCircumstances.getLocale()));
    }

    @Test
    public void hasDefaultConstructor() {
        givenADefaultClaimCircumstances();
        thenNothingIsInitialized();
    }

    @Test
    public void constructorSetsFields() {
        givenAClaimCircumstances(ID,
                                 CLAIM_CIRCUMSTANCES_JSON,
                                 VERSION);
        assertThat(ID, is(claimCircumstances.getId()));
        assertThat(CLAIM_CIRCUMSTANCES_JSON, is(claimCircumstances.getClaimCircumstancesJson()));
        assertThat(VERSION, is(claimCircumstances.getServiceVersion()));
    }

    @Test
    public void secondConstructorSetsFields() {

        givenAClaimCircumstances(CLAIM_CIRCUMSTANCES_JSON,
                                 CLAIMANT_ID,
                                 HASH,
                                 SOURCE,
                                 VERSION,
                                 LOCALE);
        assertThat(CLAIMANT_ID, is(claimCircumstances.getClaimantId()));
        assertThat(CLAIM_CIRCUMSTANCES_JSON, is(claimCircumstances.getClaimCircumstancesJson()));
        assertThat(VERSION, is(claimCircumstances.getServiceVersion()));
        assertThat(HASH, is(claimCircumstances.getHash()));
        assertThat(SOURCE, is(claimCircumstances.getSource()));
        assertThat(Locale.forLanguageTag(LOCALE), is(claimCircumstances.getLocale()));
    }

    private void givenAClaimCircumstances(final CircumstancesRequest claimCircumstancesJson, final String claimantId, final String hash, final String source, final String version, final String locale) {
        claimCircumstances = new ClaimCircumstances(claimCircumstancesJson,
                                                    claimantId,
                                                    hash,
                                                    source,
                                                    version,
                                                    locale);
    }

    private void givenAClaimCircumstances(final UUID pId, final CircumstancesRequest pClaimCircumstancesJson, final String pServiceVersion) {
        claimCircumstances = new ClaimCircumstances(pId,
                                                    pClaimCircumstancesJson,
                                                    pServiceVersion);
    }


    private void givenADefaultClaimCircumstances() {
        claimCircumstances = new ClaimCircumstances();
    }

    private void thenNothingIsInitialized() {
        // do nothing
    }

}