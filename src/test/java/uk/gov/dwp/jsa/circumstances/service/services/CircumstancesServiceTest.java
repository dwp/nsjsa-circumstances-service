package uk.gov.dwp.jsa.circumstances.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.dwp.jsa.circumstances.service.exceptions.CircumstancesAlreadyExistsException;
import uk.gov.dwp.jsa.circumstances.service.models.db.ClaimCircumstances;
import uk.gov.dwp.jsa.circumstances.service.models.http.CircumstancesRequest;
import uk.gov.dwp.jsa.circumstances.service.models.http.CircumstancesResponse;
import uk.gov.dwp.jsa.circumstances.service.repositories.CircumstancesRepository;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CircumstancesServiceTest {

    private static final String EXPECTED_JSON = "DUMMY_JSON";

    private static final UUID VALID_CLAIMANT_ID = UUID.randomUUID();

    private static final UUID EXPECTED_CLAIM_CIRCUMSTANCES_ID = UUID.randomUUID();

    private static final UUID UNKNOWN_CLAIM_CIRCUMSTANCES_ID = UUID.randomUUID();

    private static final CircumstancesRequest CIRCUMSTANCES_REQUEST = new CircumstancesRequest();

    private static final ClaimCircumstances EXPECTED_CLAIM_CIRCUMSTANCES = buildExpectedCircumstances();

    private static final CircumstancesResponse EXPECTED_CIRCUMSTANCES_RESPONSE = new CircumstancesResponse(EXPECTED_CLAIM_CIRCUMSTANCES);


    private CircumstancesService sut;

    @Mock
    private CircumstancesRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Before
    public void setUp() throws JsonProcessingException {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        sut = new CircumstancesService(repository, mapper);
        when(mapper.writeValueAsString(CIRCUMSTANCES_REQUEST)).thenReturn(EXPECTED_JSON);
        when(repository.save(any())).thenReturn(EXPECTED_CLAIM_CIRCUMSTANCES);
        when(repository.findById(EXPECTED_CLAIM_CIRCUMSTANCES_ID)).thenReturn(Optional.of(EXPECTED_CLAIM_CIRCUMSTANCES));
        when(repository.findByClaimantId(VALID_CLAIMANT_ID.toString())).thenReturn(Optional.of(EXPECTED_CLAIM_CIRCUMSTANCES));
    }

    @Test
    public void givenValidRequest_Save_ShouldReturnExpectedClaimCircumstancesId() {
        UUID resourceId = sut.save(CIRCUMSTANCES_REQUEST);
        assertEquals(EXPECTED_CLAIM_CIRCUMSTANCES_ID, resourceId);
    }

    @Test
    public void givenValidRequest_Save_ShouldSaveTheExpectedDataToRepository() {
        sut.save(CIRCUMSTANCES_REQUEST);
        ArgumentCaptor<ClaimCircumstances> captor = ArgumentCaptor.forClass(ClaimCircumstances.class);
        verify(repository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getClaimCircumstancesJson(), is(CIRCUMSTANCES_REQUEST));
    }

    @Test
    public void givenValidClaimId_getCircumstancesById_ShouldReturnExpectedCircumstances() {
        assertEquals(EXPECTED_CIRCUMSTANCES_RESPONSE.getId(),
                sut.getCircumstancesById(EXPECTED_CLAIM_CIRCUMSTANCES_ID).getId());
    }

    @Test
    public void givenUnvalidClaimId_getCircumstancesById_ShouldReturnNull() {
        assertNull(sut.getCircumstancesById(UNKNOWN_CLAIM_CIRCUMSTANCES_ID));
    }

    @Test
    public void givenValidClaimantId_getCircumstancesByClaimantId_ShouldReturnExpectedCircumstances() {
        assertEquals(VALID_CLAIMANT_ID,
                sut.getCircumstancesByClaimantId(VALID_CLAIMANT_ID).getClaimantId());
    }

    @Test
    public void givenUnvalidClaimantId_getCircumstancesById_ShouldReturnNull() {
        assertNull(sut.getCircumstancesById(UNKNOWN_CLAIM_CIRCUMSTANCES_ID));
    }

    @Test
    public void testGivenValidClaimantIdShouldDeleteTheExpectedData() {
        sut.delete(EXPECTED_CLAIM_CIRCUMSTANCES_ID);

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(repository, times(1)).deleteById(captor.capture());
        assertThat(captor.getValue(), is(EXPECTED_CLAIM_CIRCUMSTANCES_ID));
    }

    @Test
    public void givenValidRequest_Update_ShouldReturnExpectedClaimCircumstancesId() {
        UUID resourceId = sut.update(EXPECTED_CLAIM_CIRCUMSTANCES_ID, CIRCUMSTANCES_REQUEST);
        assertEquals(EXPECTED_CLAIM_CIRCUMSTANCES_ID, resourceId);
    }

    @Test
    public void givenValidRequest_Update_ShouldSaveTheExpectedDataToRepository() {
        sut.update(EXPECTED_CLAIM_CIRCUMSTANCES_ID, CIRCUMSTANCES_REQUEST);
        ArgumentCaptor<ClaimCircumstances> captor = ArgumentCaptor.forClass(ClaimCircumstances.class);
        verify(repository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getClaimCircumstancesJson(), is(CIRCUMSTANCES_REQUEST));
    }

    @Test(expected = CircumstancesAlreadyExistsException.class)
    public void givenWrongRequest_WhenSave_ThenException(){
        when(this.repository.save(any())).thenThrow(new DataIntegrityViolationException("error"));
        sut.save(CIRCUMSTANCES_REQUEST);
    }

    @Test(expected = CircumstancesAlreadyExistsException.class)
    public void givenWrongRequest_WhenUpdate_ThenException(){
        sut.update(UUID.randomUUID(),CIRCUMSTANCES_REQUEST);
    }

    private static ClaimCircumstances buildExpectedCircumstances() {
        ClaimCircumstances claimCircumstances = new ClaimCircumstances();
        claimCircumstances.setId(EXPECTED_CLAIM_CIRCUMSTANCES_ID);
        claimCircumstances.setClaimantId(VALID_CLAIMANT_ID.toString());
        claimCircumstances.setClaimCircumstancesJson(CIRCUMSTANCES_REQUEST);
        CIRCUMSTANCES_REQUEST.setClaimantId(VALID_CLAIMANT_ID);
        return claimCircumstances;
    }

}
