package com.makemytrip.modules.pricing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makemytrip.modules.pricing.controller.PricingController;
import com.makemytrip.modules.pricing.dto.FreezeRequest;
import com.makemytrip.modules.pricing.dto.FreezeResponse;
import com.makemytrip.modules.pricing.service.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PricingControllerTest {

    @Mock
    private PricingService pricingService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        PricingController controller = new PricingController(pricingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void freezePrice_returns401_whenUnauthenticated() throws Exception {
        FreezeRequest req = new FreezeRequest();
        req.setEntityId("fl-1");
        req.setEntityType("FLIGHT");
        req.setUserId("someone-else");
        req.setTravelDate(LocalDate.now().plusDays(2));

        mockMvc.perform(post("/api/pricing/freeze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void freezePrice_overridesUserId_withAuthenticatedPrincipal() throws Exception {
        FreezeRequest req = new FreezeRequest();
        req.setEntityId("fl-1");
        req.setEntityType("FLIGHT");
        req.setUserId("client-supplied");
        req.setTravelDate(LocalDate.now().plusDays(2));

        when(pricingService.freezePrice(any(FreezeRequest.class)))
                .thenReturn(FreezeResponse.builder().id("frz-1").entityId("fl-1").build());

        TestingAuthenticationToken auth = new TestingAuthenticationToken("auth-user", null);

        mockMvc.perform(post("/api/pricing/freeze")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        ArgumentCaptor<FreezeRequest> captor = ArgumentCaptor.forClass(FreezeRequest.class);
        verify(pricingService).freezePrice(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("auth-user");
    }

    @Test
    void getUserFreezes_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/pricing/freeze/user/ignored"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserFreezes_usesAuthenticatedUserId() throws Exception {
        when(pricingService.getUserFreezes("auth-user")).thenReturn(Collections.emptyList());
        TestingAuthenticationToken auth = new TestingAuthenticationToken("auth-user", null);

        mockMvc.perform(get("/api/pricing/freeze/user/ignored").principal(auth))
                .andExpect(status().isOk());

        verify(pricingService).getUserFreezes("auth-user");
    }
}
