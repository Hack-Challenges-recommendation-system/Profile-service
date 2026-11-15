package team.nofantasy.recommendationsystem.profileservice.configuration.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.kafka.topics")
public record KafkaTopicsProperties(
        String usersToCreate,
        String usersToDelete
) {
}
