package team.nofantasy.recommendationsystem.profileservice.controller;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import team.nofantasy.recommendationsystem.profileservice.dto.service.profile.ProfileServiceDto;
import team.nofantasy.recommendationsystem.profileservice.dto.service.profile.ProfileToUpdateServiceDto;
import team.nofantasy.recommendationsystem.profileservice.entity.Profile;
import team.nofantasy.recommendationsystem.profileservice.mapper.ProfileMapperImpl;
import team.nofantasy.recommendationsystem.profileservice.service.ProfileService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import(ProfileMapperImpl.class)
class ProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    private static MockHttpServletRequestBuilder api(MockHttpServletRequestBuilder builder) {
        return builder.servletPath("/api/v1");
    }

    @Test
    void returnsProfileOfSignedInUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileService.getProfile(userId)).thenReturn(new ProfileServiceDto(
                userId, "founder", Profile.BusinessStage.IDEA, null, Profile.Experience.NONE, null, List.of(), false, Instant.now()
        ));

        mockMvc.perform(api(get("/api/v1/profile/me")).header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.businessStage").value("IDEA"))
                .andExpect(jsonPath("$.onboardingCompleted").value(false));
    }

    @Test
    void requiresUserIdFromGateway() throws Exception {
        mockMvc.perform(api(get("/api/v1/profile/me")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(profileService);
    }

    @Test
    void missingProfileIsNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileService.getProfile(userId)).thenThrow(new EntityNotFoundException("missing"));

        mockMvc.perform(api(get("/api/v1/profile/me")).header("X-User-Id", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePassesAnswersToService() throws Exception {
        UUID userId = UUID.randomUUID();
        when(profileService.updateProfile(any())).thenReturn(new ProfileServiceDto(
                userId, "founder", Profile.BusinessStage.LAUNCH, "retail", Profile.Experience.BEGINNER,
                "find first customers", List.of("VIDEO"), true, Instant.now()
        ));

        mockMvc.perform(api(put("/api/v1/profile/me"))
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname": "founder", "businessStage": "LAUNCH", "businessSphere": "retail",
                                 "experience": "BEGINNER", "goal": "find first customers", "preferredFormats": ["VIDEO"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onboardingCompleted").value(true));

        verify(profileService).updateProfile(new ProfileToUpdateServiceDto(
                userId, "founder", Profile.BusinessStage.LAUNCH, "retail", Profile.Experience.BEGINNER,
                "find first customers", List.of("VIDEO")
        ));
    }

    @Test
    void updateValidatesAnswers() throws Exception {
        mockMvc.perform(api(put("/api/v1/profile/me"))
                        .header("X-User-Id", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname": "", "goal": "%s"}
                                """.formatted("x".repeat(501))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nickname").exists())
                .andExpect(jsonPath("$.goal").exists());

        verifyNoInteractions(profileService);
    }

    @Test
    void updateRejectsUnknownStage() throws Exception {
        mockMvc.perform(api(put("/api/v1/profile/me"))
                        .header("X-User-Id", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname": "founder", "businessStage": "UNICORN"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(profileService);
    }
}
