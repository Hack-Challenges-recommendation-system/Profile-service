package team.nofantasy.recommendationsystem.profileservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import team.nofantasy.recommendationsystem.profileservice.dto.service.kafka.UserToCreate;
import team.nofantasy.recommendationsystem.profileservice.dto.service.kafka.UserToDelete;

/**
 * Keeps profiles in step with accounts in auth-service.
 */
@SuppressWarnings(value = {"unused"})
@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaConsumerService {
    private static final String PAYLOAD_TYPE = "spring.json.value.default.type=";

    private final ProfileService profileService;

    @KafkaListener(
            topics = "${app.kafka.topics.users-to-create}",
            groupId = "${spring.application.name}",
            properties = PAYLOAD_TYPE + "team.nofantasy.recommendationsystem.profileservice.dto.service.kafka.UserToCreate"
    )
    public void receiveUserToCreate(UserToCreate userToCreate) {
        log.atInfo()
                .setMessage("Message received")
                .addKeyValue("topic", "users.to.create")
                .addKeyValue("userId", userToCreate.userId())
                .log();

        profileService.createProfile(userToCreate);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.users-to-delete}",
            groupId = "${spring.application.name}",
            properties = PAYLOAD_TYPE + "team.nofantasy.recommendationsystem.profileservice.dto.service.kafka.UserToDelete"
    )
    public void receiveUserToDelete(UserToDelete userToDelete) {
        log.atInfo()
                .setMessage("Message received")
                .addKeyValue("topic", "users.to.delete")
                .addKeyValue("userId", userToDelete.userId())
                .log();

        profileService.deleteProfile(userToDelete);
    }
}
