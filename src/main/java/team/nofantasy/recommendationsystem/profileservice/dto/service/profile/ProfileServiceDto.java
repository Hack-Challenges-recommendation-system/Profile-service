package team.nofantasy.recommendationsystem.profileservice.dto.service.profile;

import team.nofantasy.recommendationsystem.profileservice.entity.Profile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfileServiceDto(
        UUID userId,
        String nickname,
        Profile.BusinessStage businessStage,
        String businessSphere,
        Profile.Experience experience,
        String goal,
        List<String> preferredFormats,
        Boolean onboardingCompleted,
        Instant updatedAt
) {
}
