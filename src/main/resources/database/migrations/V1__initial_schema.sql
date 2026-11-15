-- Business profile used for personalization. Created when auth-service publishes users.to.create.
CREATE TABLE profile(
    user_id              UUID         PRIMARY KEY,
    nickname             VARCHAR(30)  NOT NULL,
    business_stage       VARCHAR(16),
    business_sphere      VARCHAR(64),
    experience           VARCHAR(16),
    goal                 VARCHAR(500),
    preferred_formats    TEXT[]       NOT NULL DEFAULT '{}',
    onboarding_completed BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);
