package team.nofantasy.recommendationsystem.profileservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.nofantasy.recommendationsystem.profileservice.dto.service.kafka.UserToCreate;
import team.nofantasy.recommendationsystem.profileservice.dto.service.profile.ProfileToUpdateServiceDto;
import team.nofantasy.recommendationsystem.profileservice.entity.Profile;
import team.nofantasy.recommendationsystem.profileservice.mapper.ProfileMapper;
import team.nofantasy.recommendationsystem.profileservice.mapper.ProfileMapperImpl;
import team.nofantasy.recommendationsystem.profileservice.repository.ProfileRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {
    @Mock
    private ProfileRepository profileRepository;

    private final ProfileMapper profileMapper = new ProfileMapperImpl();

    private ProfileService profileService() {
        return new ProfileService(profileRepository, profileMapper);
    }

    @Test
    void createsEmptyProfileForNewUser() {
        UUID userId = UUID.randomUUID();
        when(profileRepository.existsById(userId)).thenReturn(false);

        profileService().createProfile(new UserToCreate(userId, "founder"));

        ArgumentCaptor<Profile> saved = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo(userId);
        assertThat(saved.getValue().getNickname()).isEqualTo("founder");
        assertThat(saved.getValue().getOnboardingCompleted()).isFalse();
        assertThat(saved.getValue().getPreferredFormats()).isEmpty();
    }

    @Test
    void ignoresRepeatedCreateMessage() {
        UUID userId = UUID.randomUUID();
        when(profileRepository.existsById(userId)).thenReturn(true);

        profileService().createProfile(new UserToCreate(userId, "founder"));

        verify(profileRepository, never()).save(any());
    }

    @Test
    void updateSavesAnswersAndCompletesOnboarding() {
        UUID userId = UUID.randomUUID();
        Profile profile = Profile.builder().userId(userId).nickname("founder").onboardingCompleted(false).build();
        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.saveAndFlush(profile)).thenReturn(profile);

        var result = profileService().updateProfile(new ProfileToUpdateServiceDto(
                userId, "founder2", Profile.BusinessStage.LAUNCH, "retail", Profile.Experience.NONE,
                "find first customers", null
        ));

        assertThat(result.onboardingCompleted()).isTrue();
        assertThat(result.businessStage()).isEqualTo(Profile.BusinessStage.LAUNCH);
        assertThat(result.nickname()).isEqualTo("founder2");
        assertThat(result.preferredFormats()).as("skipped question becomes an empty list").isEmpty();
    }

    @Test
    void missingProfileIsNotFound() {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService().getProfile(userId)).isInstanceOf(EntityNotFoundException.class);
    }
}
