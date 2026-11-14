package team.nofantasy.recommendationsystem.profileservice.dto.service.kafka;

import java.util.UUID;

/**
 * Consumed from {@code users.to.create}; auth-service publishes it after registration.
 */
public record UserToCreate(
        UUID userId,
        String nickname
) {
}
