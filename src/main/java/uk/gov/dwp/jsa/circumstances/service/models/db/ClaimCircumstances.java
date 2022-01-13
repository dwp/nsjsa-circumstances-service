package uk.gov.dwp.jsa.circumstances.service.models.db;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import uk.gov.dwp.jsa.circumstances.service.models.http.CircumstancesRequest;
import uk.gov.dwp.jsa.security.encryption.SecuredJsonBinaryType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import static java.util.Locale.ENGLISH;

@Entity
@TypeDef(name = "jsonb", typeClass = SecuredJsonBinaryType.class)
public class ClaimCircumstances {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID id;

    private String claimantId;

    @CreationTimestamp
    private LocalDateTime createdTimestamp;

    @UpdateTimestamp
    private LocalDateTime updatedTimestamp;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private CircumstancesRequest claimCircumstancesJson;

    private String hash;
    private String source;
    private String serviceVersion;
    private Locale locale;
    private boolean encryptedJson;

    public ClaimCircumstances() {
    }

    public ClaimCircumstances(final UUID pId,
                              final CircumstancesRequest pClaimCircumstancesJson,
                              final String pServiceVersion) {
        this(pClaimCircumstancesJson, null, null, null, pServiceVersion, ENGLISH.getLanguage());
        this.id = pId;
    }

    public ClaimCircumstances(
            final CircumstancesRequest claimCircumstancesJson,
            final String claimantId,
            final String hash,
            final String source,
            final String version,
            final String locale
    ) {
        this.claimCircumstancesJson = claimCircumstancesJson;
        this.claimantId = claimantId;
        this.hash = hash;
        this.source = source;
        this.serviceVersion = version;
        this.locale = localeFromString(locale);
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public String getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(final String claimantId) {
        this.claimantId = claimantId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(final LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(final LocalDateTime updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public CircumstancesRequest getClaimCircumstancesJson() {
        return claimCircumstancesJson;
    }

    public void setClaimCircumstancesJson(final CircumstancesRequest claimCircumstancesJson) {
        this.claimCircumstancesJson = claimCircumstancesJson;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(final String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public boolean isEncryptedJson() {
        return encryptedJson;
    }

    public void setEncryptedJson(final boolean encryptedJson) {
        this.encryptedJson = encryptedJson;
    }

    public void update(final CircumstancesRequest circumstancesRequest, final UUID claimantId, final String hash,
                       final String source, final String version, final String locale) {
        this.claimCircumstancesJson = circumstancesRequest;
        this.claimantId = claimantId.toString();
        this.hash = hash;
        this.source = source;
        this.serviceVersion = version;
        this.locale = localeFromString(locale);
        this.encryptedJson = true;

    }

    private Locale localeFromString(final String locale) {
        if (StringUtils.isNotEmpty(locale)) {
            return Locale.forLanguageTag(locale);
        } else {
            return null;
        }
    }
}
