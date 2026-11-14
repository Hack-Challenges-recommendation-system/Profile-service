package team.nofantasy.recommendationsystem.profileservice.dto.controller.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;
import team.nofantasy.recommendationsystem.profileservice.entity.Profile.BusinessStage;
import team.nofantasy.recommendationsystem.profileservice.entity.Profile.Experience;

import java.util.List;

/**
 * Onboarding answers. Every answer except the nickname is optional, so a user can skip questions.
 */
public record ProfileToUpdateControllerDto(
        @NotBlank(message = "Nickname cannot be blank")
        @Size(min = 3, max = 30, message = "Nickname must be between 3 and 30 characters")
        String nickname,

        @Nullable
        BusinessStage businessStage,

        @Nullable
        @Size(max = 64, message = "Business sphere must be at most 64 characters")
        String businessSphere,

        @Nullable
        Experience experience,

        @Nullable
        @Size(max = 500, message = "Goal must be at most 500 characters")
        String goal,

        @Nullable
        @Size(max = 10, message = "Choose at most 10 formats")
        List<@NotBlank @Size(max = 32) String> preferredFormats
) {
}
