package team.nofantasy.recommendationsystem.profileservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares the topics this service reads, exactly as auth-service declares them, before the listeners start.
 * Whichever service starts first creates them with 3 partitions; otherwise a consumer that subscribed first
 * would get one-partition topics and miss messages in the partitions added later.
 */
@Configuration
@EnableConfigurationProperties(KafkaTopicsProperties.class)
public class KafkaConfiguration {
    @Bean
    public NewTopic usersToCreateTopic(KafkaTopicsProperties topics) {
        return TopicBuilder
                .name(topics.usersToCreate())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic usersToDeleteTopic(KafkaTopicsProperties topics) {
        return TopicBuilder
                .name(topics.usersToDelete())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
