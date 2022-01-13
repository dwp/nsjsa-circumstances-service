package uk.gov.dwp.jsa.circumstances.service.acceptance_tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.tools.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.dwp.jsa.adaptors.http.api.ApiError;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.adaptors.http.api.ApiSuccess;
import uk.gov.dwp.jsa.circumstances.service.AppInfo;
import uk.gov.dwp.jsa.circumstances.service.config.WithVersionUriComponentsBuilder;
import uk.gov.dwp.jsa.circumstances.service.controllers.CircumstancesController;
import uk.gov.dwp.jsa.circumstances.service.exceptions.CircumstancesAlreadyExistsException;
import uk.gov.dwp.jsa.circumstances.service.models.db.ClaimCircumstances;
import uk.gov.dwp.jsa.circumstances.service.models.http.CircumstancesRequest;
import uk.gov.dwp.jsa.circumstances.service.models.http.CircumstancesResponse;
import uk.gov.dwp.jsa.circumstances.service.services.CircumstancesService;
import uk.gov.dwp.jsa.security.WithMockUser;
import uk.gov.dwp.jsa.security.roles.Role;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(CircumstancesController.class)
public class CircumstancesControllerAcceptanceTest {

    private static final UUID VALID_CLAIM_CIRCUMSTANCES_ID = UUID.randomUUID();
    private static final UUID VALID_CLAIMANT_ID = UUID.randomUUID();
    private static final UUID UNKNOWN_CLAIMANT_ID = UUID.randomUUID();

    private static final String URI_BASE = "/nsjsa";

    private static final URI CLAIMANT_BASE_URL = URI.create(URI_BASE + "/citizen/" + VALID_CLAIMANT_ID + "/claim");
    private static final URI UNKNOWN_CLAIMANT_URL = URI.create(URI_BASE + "/citizen/" + UNKNOWN_CLAIMANT_ID + "/claim");
    private static final URI VALID_CLAIM_BY_ID_URL = URI.create(URI_BASE + "/claim/" + VALID_CLAIM_CIRCUMSTANCES_ID);
    private static final URI VALID_CLAIM_BY_ID_URL_WITH_VERSION = URI.create(URI_BASE + "/v1/claim/" + VALID_CLAIM_CIRCUMSTANCES_ID);
    private static final URI UNKNOWN_CLAIM_BY_ID_URL = URI.create(URI_BASE + "/claim/" + UUID.randomUUID());

    private static final CircumstancesRequest CIRCUMSTANCES_REQUEST = getCircumstancesRequest();

    private static final ClaimCircumstances CIRCUMSTANCES = new ClaimCircumstances(VALID_CLAIM_CIRCUMSTANCES_ID, CIRCUMSTANCES_REQUEST, "v1");

    private static final CircumstancesResponse CIRCUMSTANCES_RESPONSE = new CircumstancesResponse(CIRCUMSTANCES);

    private static final ApiSuccess<CircumstancesResponse> CIRCUMSTANCES_API_SUCCESS =
            new ApiSuccess<>(VALID_CLAIM_BY_ID_URL, CIRCUMSTANCES_RESPONSE);

    private static final ApiResponse<CircumstancesResponse> CIRCUMSTANCES_RESPONSE_API_SUCCESS =
            new ApiResponse<>(Collections.singletonList(CIRCUMSTANCES_API_SUCCESS));


    private static final ApiSuccess<CircumstancesResponse> CLAIMANT_CIRCUMSTANCES_API_SUCCESS =
            new ApiSuccess<>(CLAIMANT_BASE_URL, CIRCUMSTANCES_RESPONSE);

    private static final ApiResponse<CircumstancesResponse> CLAIMANT_CIRCUMSTANCES_RESPONSE_API_SUCCESS =
            new ApiResponse<>(Collections.singletonList(CLAIMANT_CIRCUMSTANCES_API_SUCCESS));

    private static final ApiResponse<CircumstancesResponse> NOT_FOUND_RESPONSE =
            new ApiResponse<>(new ApiError(HttpStatus.NOT_FOUND.toString(), HttpStatus.NOT_FOUND.getReasonPhrase()));


    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CircumstancesService service;

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "appInfo")
    private AppInfo appInfo;

    @MockBean
    private WithVersionUriComponentsBuilder uriBuilder;

    @Before
    public void setUp() {
        when(appInfo.getVersion()).thenReturn(StringUtils.EMPTY);
        when(uriBuilder.cloneBuilder()).thenReturn(new WithVersionUriComponentsBuilder(appInfo));
        when(service.save(any())).thenReturn(VALID_CLAIM_CIRCUMSTANCES_ID);
        when(service.getCircumstancesById(VALID_CLAIM_CIRCUMSTANCES_ID)).thenReturn(CIRCUMSTANCES_RESPONSE);
        when(service.getCircumstancesByClaimantId(VALID_CLAIMANT_ID)).thenReturn(CIRCUMSTANCES_RESPONSE);
        when(service.getCircumstancesByClaimantId(UNKNOWN_CLAIMANT_ID)).thenReturn(null);
    }

    @WithMockUser
    @Test
    public void GivenValidAndPopulatedRequest_ShouldCreateClaimantRecordAndReturnExpectedURL() throws Exception {
        mockMvc.perform(post(CLAIMANT_BASE_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(CIRCUMSTANCES_REQUEST)))
                .andExpect(content().string(containsString("/nsjsa/claim/" + VALID_CLAIM_CIRCUMSTANCES_ID.toString())))
                .andExpect(status().isCreated());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void GivenNoRequest_ShouldNotCreateClaimantRecordAndReturnExpectedStatus() throws Exception {
        mockMvc.perform(post(CLAIMANT_BASE_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void GivenInvalidRequestJson_Save_ShouldReturnBadRequest() throws Exception {
        when(service.save(any())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        mockMvc.perform(post(CLAIMANT_BASE_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void GivenAlreadyExistentRequest_ShouldNotSaveAndReturnConflict() throws Exception {
        when(service.save(any())).thenThrow(CircumstancesAlreadyExistsException.class);
        mockMvc.perform(post(CLAIMANT_BASE_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(CIRCUMSTANCES_REQUEST)))
                .andExpect(status().isConflict());
    }

    @WithMockUser(role = Role.SCA)
    @Test
    public void GivenValidClaimId_ShouldReturnCircumstancesResponse() throws Exception {

        mockMvc.perform(get(VALID_CLAIM_BY_ID_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(CIRCUMSTANCES_RESPONSE_API_SUCCESS)))
                .andExpect(status().isOk());
    }

    @WithMockUser(role = Role.WC)
    @Test
    public void GivenUnvalidClaimId_ShouldReturnNotFound() throws Exception {

        mockMvc.perform(get(UNKNOWN_CLAIM_BY_ID_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(role = Role.CCM)
    @Test
    public void GivenValidClaimantId_ShouldReturnCircumstancesResponseList() throws Exception {
        mockMvc.perform(get(CLAIMANT_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(CLAIMANT_CIRCUMSTANCES_RESPONSE_API_SUCCESS)))
                .andExpect(status().isOk());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void GivenUnvalidClaimantId_ShouldReturnNotFound() throws Exception {

        mockMvc.perform(get(UNKNOWN_CLAIMANT_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(role = Role.WC)
    @Test
    public void testGivenValidIdShouldDeleteAndReturnExpectedURL() throws Exception {
        mockMvc.perform(delete(VALID_CLAIM_BY_ID_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(VALID_CLAIM_BY_ID_URL.toString())))
                .andExpect(status().isOk());
    }

    @WithMockUser(role = Role.WC)
    @Test
    public void testGivenUnknownIdShouldReturnNotFoundAndReturnExpectedURL() throws Exception {
        mockMvc.perform(delete(UNKNOWN_CLAIM_BY_ID_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(NOT_FOUND_RESPONSE)))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(role = Role.CCM)
    @Test
    public void GivenValidAndPopulatedRequest_ShouldUpdateClaimantRecordAndReturnExpectedURL() throws Exception {
        mockMvc.perform(patch(VALID_CLAIM_BY_ID_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(CIRCUMSTANCES_REQUEST)))
                .andExpect(content().string(containsString("/nsjsa/claim/" + VALID_CLAIM_CIRCUMSTANCES_ID.toString())))
                .andExpect(status().isOk());
    }

    @WithMockUser(role = Role.CCM)
    @Test
    public void GivenRequestMissingClaimStartDate_ShouldReturnBadRequest() throws Exception {
        final CircumstancesRequest circumstancesRequest = getCircumstancesRequest();
        circumstancesRequest.setClaimStartDate(null);

        mockMvc.perform(patch(VALID_CLAIM_BY_ID_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(circumstancesRequest)))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(role = Role.WC)
    @Test
    public void GivenRequestMissingDateOfClaim_ShouldReturnBadRequest() throws Exception {
        final CircumstancesRequest circumstancesRequest = getCircumstancesRequest();
        circumstancesRequest.setDateOfClaim(null);
        mockMvc.perform(patch(VALID_CLAIM_BY_ID_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(circumstancesRequest)))
                .andExpect(status().isBadRequest());
    }


    private <T> String toJson(T objectToConverted) throws JsonProcessingException {
        return mapper.writeValueAsString(objectToConverted);
    }

    private static CircumstancesRequest getCircumstancesRequest() {
        final CircumstancesRequest circumstancesRequest = new CircumstancesRequest();
        final LocalDate now = LocalDate.now();
        circumstancesRequest.setDateOfClaim(now);
        circumstancesRequest.setClaimStartDate(now);
        return circumstancesRequest;
    }

}
