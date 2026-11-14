package team.nofantasy.recommendationsystem.profileservice;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import team.nofantasy.recommendationsystem.profileservice.repository.ProfileRepository;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Profiles follow the account events that auth-service publishes; runs on real Postgres and Kafka.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class ProfileIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private KafkaContainer kafkaContainer;

    private static MockHttpServletRequestBuilder api(MockHttpServletRequestBuilder builder) {
        return builder.servletPath("/api/v1");
    }

    /**
     * Sends a message the way auth-service does: UUID key, JSON value without type headers.
     */
    private void send(String topic, UUID key, String json) throws Exception {
        Map<String, Object> config = Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());

        try (KafkaProducer<UUID, String> producer = new KafkaProducer<>(config, new UUIDSerializer(), new StringSerializer())) {
            producer.send(new ProducerRecord<>(topic, key, json)).get();
        }
    }

    @Test
    void profileFollowsAccountLifecycle() throws Exception {
        UUID userId = UUID.randomUUID();

        send("users.to.create", userId, """
                {"userId": "%s", "nickname": "founder"}
                """.formatted(userId));
        await().atMost(Duration.ofSeconds(30)).until(() -> profileRepository.existsById(userId));

        mockMvc.perform(api(get("/api/v1/profiles/me")).header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("founder"))
                .andExpect(jsonPath("$.onboardingCompleted").value(false))
                .andExpect(jsonPath("$.preferredFormats.length()").value(0));

        mockMvc.perform(api(put("/api/v1/profiles/me"))
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname": "founder", "businessStage": "LAUNCH", "businessSphere": "retail",
                                 "experience": "NONE", "goal": "find first customers", "preferredFormats": ["VIDEO", "ARTICLE"]}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(api(get("/api/v1/profiles/me")).header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessStage").value("LAUNCH"))
                .andExpect(jsonPath("$.preferredFormats[1]").value("ARTICLE"))
                .andExpect(jsonPath("$.onboardingCompleted").value(true));

        send("users.to.delete", userId, """
                {"userId": "%s"}
                """.formatted(userId));
        await().atMost(Duration.ofSeconds(30)).until(() -> !profileRepository.existsById(userId));

        mockMvc.perform(api(get("/api/v1/profiles/me")).header("X-User-Id", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownUserHasNoProfile() throws Exception {
        mockMvc.perform(api(get("/api/v1/profiles/me")).header("X-User-Id", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
