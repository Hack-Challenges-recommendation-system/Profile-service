package team.nofantasy.recommendationsystem.profileservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * What recommendation-service knows about an entrepreneur. Holds no personal data: the email stays in auth-service.
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "profile")
public class Profile {
    /**
     * Stage of the user's business.
     */
    public enum BusinessStage {
        IDEA,
        LAUNCH,
        GROWTH,
        SCALING
    }

    /**
     * How much entrepreneurial experience the user has.
     */
    public enum Experience {
        NONE,
        BEGINNER,
        EXPERIENCED
    }

    @Setter(AccessLevel.NONE)
    @Id
    private UUID userId;

    @NotBlank
    @Size(max = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private BusinessStage businessStage;

    @Size(max = 64)
    private String businessSphere;

    @Enumerated(EnumType.STRING)
    private Experience experience;

    @Size(max = 500)
    private String goal;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> preferredFormats = new ArrayList<>();

    @NotNull
    private Boolean onboardingCompleted;

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    private Instant createdAt;

    @Setter(AccessLevel.NONE)
    @UpdateTimestamp
    private Instant updatedAt;
}
