package team.nofantasy.recommendationsystem.profileservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.nofantasy.recommendationsystem.profileservice.dto.service.kafka.UserToCreate;
import team.nofantasy.recommendationsystem.profileservice.dto.service.kafka.UserToDelete;
import team.nofantasy.recommendationsystem.profileservice.dto.service.profile.ProfileServiceDto;
import team.nofantasy.recommendationsystem.profileservice.dto.service.profile.ProfileToUpdateServiceDto;
import team.nofantasy.recommendationsystem.profileservice.entity.Profile;
import team.nofantasy.recommendationsystem.profileservice.mapper.ProfileMapper;
import team.nofantasy.recommendationsystem.profileservice.repository.ProfileRepository;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    /**
     * Creates an empty profile for a newly registered user. A repeated message for the same user is ignored.
     */
    @Transactional
    public void createProfile(UserToCreate userToCreate) {
        if (profileRepository.existsById(userToCreate.userId())) {
            return;
        }

        profileRepository.save(
                Profile.builder()
                        .userId(userToCreate.userId())
                        .nickname(userToCreate.nickname())
                        .onboardingCompleted(false)
                        .build()
        );
    }

    /**
     * Deletes the profile of a deleted account. A profile that doesn't exist is ignored.
     */
    @Transactional
    public void deleteProfile(UserToDelete userToDelete) {
        profileRepository.deleteById(userToDelete.userId());
    }

    /**
     * @throws EntityNotFoundException if the profile does not exist.
     */
    @Transactional(readOnly = true)
    public ProfileServiceDto getProfile(UUID userId) {
        return profileMapper.profileEntityToServiceDto(findProfile(userId));
    }

    /**
     * Saves the onboarding answers and marks onboarding as completed.
     *
     * @throws EntityNotFoundException if the profile does not exist.
     */
    @Transactional
    public ProfileServiceDto updateProfile(ProfileToUpdateServiceDto dto) {
        Profile profile = findProfile(dto.userId());

        profile.setNickname(dto.nickname());
        profile.setBusinessStage(dto.businessStage());
        profile.setBusinessSphere(dto.businessSphere());
        profile.setExperience(dto.experience());
        profile.setGoal(dto.goal());
        profile.setPreferredFormats(dto.preferredFormats() == null ? new ArrayList<>() : new ArrayList<>(dto.preferredFormats()));
        profile.setOnboardingCompleted(true);

        return profileMapper.profileEntityToServiceDto(profileRepository.saveAndFlush(profile));
    }

    private Profile findProfile(UUID userId) {
        return profileRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Profile is not found; userId=" + userId)
        );
    }
}
