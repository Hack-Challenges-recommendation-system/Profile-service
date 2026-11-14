package team.nofantasy.recommendationsystem.profileservice.dto.service.kafka;

import java.util.UUID;

/**
 * Consumed from {@code users.to.delete}; auth-service publishes it when an account is deleted.
 */
public record UserToDelete(
        UUID userId
) {
}
