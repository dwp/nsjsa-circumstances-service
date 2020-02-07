package uk.gov.dwp.jsa.circumstances.service.controllers;

import org.jooq.tools.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.circumstances.service.AppInfo;
import uk.gov.dwp.jsa.circumstances.service.config.WithVersionUriComponentsBuilder;
import uk.gov.dwp.jsa.circumstances.service.exceptions.CircumstancesAlreadyExistsException;
import uk.gov.dwp.jsa.circumstances.service.models.http.CircumstancesRequest;
import uk.gov.dwp.jsa.circumstances.service.models.http.CircumstancesResponse;
import uk.gov.dwp.jsa.circumstances.service.services.CircumstancesService;
import uk.gov.dwp.jsa.circumstances.service.services.ResponseBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CircumstancesControllerTest {

    private static final UUID EXPECTED_CLAIM_CIRCUMSTANCES_ID = UUID.randomUUID();
    private static final UUID UNKNOWN_CLAIM_CIRCUMSTANCES_ID = UUID.randomUUID();

    private static final UUID VALID_CLAIMANT_ID = UUID.randomUUID();
    private static final UUID UNKNOWN_CLAIMANT_ID = UUID.randomUUID();
    private static final String BASE_URL = "/nsjsa/claim/";
    private static final String NSJSA_CITIZEN_BASE_GET_URL = "http://localhost" + BASE_URL + EXPECTED_CLAIM_CIRCUMSTANCES_ID;
    private static final URI EXPECTED_RETURN_URL = URI.create(NSJSA_CITIZEN_BASE_GET_URL);

    private static final ResponseEntity<ApiResponse<UUID>> EXPECTED_RESPONSE_FOR_CREATE = new ResponseBuilder<UUID>()
            .withStatus(HttpStatus.CREATED)
            .withSuccessData(URI.create(BASE_URL + EXPECTED_CLAIM_CIRCUMSTANCES_ID), EXPECTED_CLAIM_CIRCUMSTANCES_ID)
            .build();


    private static final ResponseEntity<ApiResponse<UUID>> EXPECTED_RESPONSE_FOR_GET = new ResponseBuilder<UUID>()
            .withStatus(HttpStatus.OK)
            .withSuccessData(EXPECTED_RETURN_URL, EXPECTED_CLAIM_CIRCUMSTANCES_ID)
            .build();

    private static final ResponseEntity<ApiResponse<UUID>> EXPECTED_DELETE_RESPONSE = new ResponseBuilder<UUID>()
            .withStatus(HttpStatus.OK)
            .withSuccessData(URI.create(BASE_URL + EXPECTED_CLAIM_CIRCUMSTANCES_ID), EXPECTED_CLAIM_CIRCUMSTANCES_ID)
            .build();

    private static final ResponseEntity<ApiResponse<UUID>> EXPECTED_UNSUCCESSFUL_DELETE_RESPONSE = new ResponseBuilder<UUID>()
            .withStatus(HttpStatus.NOT_FOUND)
            .withApiError(HttpStatus.NOT_FOUND.toString(), HttpStatus.NOT_FOUND.getReasonPhrase())
            .build();

    private CircumstancesController sut;

    @Mock
    private CircumstancesService circumstancesService;

    @Mock
    private CircumstancesRequest circumstancesRequest;

    @Mock
    private CircumstancesResponse circumstancesResponseMock;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private AppInfo appInfo;


    @Before
    public void setUp() {
        when(appInfo.getVersion()).thenReturn(StringUtils.EMPTY);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        sut = new CircumstancesController(circumstancesService, new WithVersionUriComponentsBuilder(appInfo));
        when(circumstancesService.save(any())).thenReturn(EXPECTED_CLAIM_CIRCUMSTANCES_ID);
        when(circumstancesService.getCircumstancesById(EXPECTED_CLAIM_CIRCUMSTANCES_ID)).thenReturn(circumstancesResponseMock);
        when(circumstancesService.getCircumstancesByClaimantId(VALID_CLAIMANT_ID)).thenReturn(circumstancesResponseMock);
        when(httpServletRequest.getRequestURI()).thenReturn(EXPECTED_RETURN_URL.toString());

    }

    @Test
    public void givenValidRequest_Create_ShouldReturnExpectedUrl() {
        ResponseEntity<ApiResponse<UUID>> uriResponseEntity = sut.createClaimCircumstances(EXPECTED_CLAIM_CIRCUMSTANCES_ID, circumstancesRequest);
        assertEquals(EXPECTED_RESPONSE_FOR_CREATE.getStatusCode(), uriResponseEntity.getStatusCode());
        assertEquals(EXPECTED_RESPONSE_FOR_CREATE.getBody().getSuccess().get(0).getPath(), uriResponseEntity.getBody().getSuccess().get(0).getPath());
        assertEquals(EXPECTED_RESPONSE_FOR_CREATE.getBody().getSuccess().get(0).getData(), uriResponseEntity.getBody().getSuccess().get(0).getData());
    }

    @Test
    public void givenValidClaimId_getClaimCircumstancesById_ShouldReturnCircumstancesInformation() {
        ResponseEntity<ApiResponse<CircumstancesResponse>> circumstancesResponse = sut.getClaimCircumstancesById(EXPECTED_CLAIM_CIRCUMSTANCES_ID, httpServletRequest);
        assertEquals(EXPECTED_RESPONSE_FOR_GET.getStatusCode(), circumstancesResponse.getStatusCode());
        assertEquals(circumstancesResponseMock, circumstancesResponse.getBody().getSuccess().get(0).getData());
        assertEquals(EXPECTED_RESPONSE_FOR_GET.getBody().getSuccess().get(0).getPath(), circumstancesResponse.getBody().getSuccess().get(0).getPath());
    }

    @Test
    public void givenUnvalidClaimId_getClaimCircumstancesById_ShouldReturn404() {
        ResponseEntity<ApiResponse<CircumstancesResponse>> response = sut.getClaimCircumstancesById(UNKNOWN_CLAIM_CIRCUMSTANCES_ID, httpServletRequest);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.toString(), response.getBody().getError().getCode());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response.getBody().getError().getMessage());

    }

    @Test
    public void givenValidClaimantId_getClaimCircumstancesById_ShouldReturnCircumstancesInformation() {
        ResponseEntity<ApiResponse<CircumstancesResponse>> circumstancesResponse =
                sut.getClaimCircumstancesByClaimantId(VALID_CLAIMANT_ID, httpServletRequest);
        assertEquals(circumstancesResponseMock, circumstancesResponse.getBody().getSuccess().get(0).getData());
        assertEquals(HttpStatus.OK, circumstancesResponse.getStatusCode());
    }

    @Test
    public void givenUnvalidClaimantId_getClaimCircumstancesByClaimantId_ShouldReturn404() {
        ResponseEntity<ApiResponse<CircumstancesResponse>> response = sut.getClaimCircumstancesByClaimantId(UNKNOWN_CLAIMANT_ID, httpServletRequest);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response.getBody().getError().getMessage());
        assertEquals(HttpStatus.NOT_FOUND.toString(), response.getBody().getError().getCode());
    }

    @Test
    public void givenValidRequest_ServiceSave_ShouldBeCalledOnce() {
        sut.createClaimCircumstances(EXPECTED_CLAIM_CIRCUMSTANCES_ID, circumstancesRequest);

        ArgumentCaptor<CircumstancesRequest> captor = ArgumentCaptor.forClass(CircumstancesRequest.class);

        verify(circumstancesService, times(1)).save(captor.capture());

        assertThat(captor.getValue(), is(circumstancesRequest));
    }

    @Test(expected = CircumstancesAlreadyExistsException.class)
    public void givenExistentRequest_ServiceSave_ShouldThrowAlreadyExistentException() {
        when(circumstancesService.save(any())).thenThrow(CircumstancesAlreadyExistsException.class);
        sut.createClaimCircumstances(EXPECTED_CLAIM_CIRCUMSTANCES_ID, circumstancesRequest);
    }

    @Test
    public void testGivenValidIdDeleteShouldReturnExpectedUrl() {
        ResponseEntity<ApiResponse<UUID>> uriResponseEntity = sut.deleteById(EXPECTED_CLAIM_CIRCUMSTANCES_ID);
        assertEquals(EXPECTED_DELETE_RESPONSE.getStatusCode(), uriResponseEntity.getStatusCode());
        assertEquals(EXPECTED_DELETE_RESPONSE.getBody().getSuccess().get(0).getPath(), uriResponseEntity.getBody().getSuccess().get(0).getPath());
        assertEquals(EXPECTED_DELETE_RESPONSE.getBody().getSuccess().get(0).getData(), uriResponseEntity.getBody().getSuccess().get(0).getData());
    }

    @Test
    public void testGivenInValidIdDeleteShouldReturnExpectedUrl() {
        ResponseEntity<ApiResponse<UUID>> uriResponseEntity = sut.deleteById(UNKNOWN_CLAIM_CIRCUMSTANCES_ID);
        assertEquals(EXPECTED_UNSUCCESSFUL_DELETE_RESPONSE.getStatusCode(), uriResponseEntity.getStatusCode());
        assertEquals(EXPECTED_UNSUCCESSFUL_DELETE_RESPONSE.getBody().getError().getCode(), uriResponseEntity.getBody().getError().getCode());
        assertEquals(EXPECTED_UNSUCCESSFUL_DELETE_RESPONSE.getBody().getError().getMessage(), uriResponseEntity.getBody().getError().getMessage());
    }

    @Test
    public void givenValidRequest_Update_ShouldReturnExpectedUrl() {
        ResponseEntity<ApiResponse<UUID>> uriResponseEntity =
                sut.updateClaimCircumstances(EXPECTED_CLAIM_CIRCUMSTANCES_ID,
                        circumstancesRequest);
        assertEquals(EXPECTED_RESPONSE_FOR_GET.getStatusCode(), uriResponseEntity.getStatusCode());
        assertEquals(EXPECTED_RESPONSE_FOR_CREATE.getBody().getSuccess().get(0).getPath(), uriResponseEntity.getBody().getSuccess().get(0).getPath());
        assertEquals(EXPECTED_RESPONSE_FOR_CREATE.getBody().getSuccess().get(0).getData(), uriResponseEntity.getBody().getSuccess().get(0).getData());
    }
}
