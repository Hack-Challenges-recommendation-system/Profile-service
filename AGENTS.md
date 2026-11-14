# profile-service

The business profile of each user: the onboarding answers that personalization uses. It holds no personal data; the email stays in auth-service.

**Read the shared guide first:** [AGENTS.md in Documentation](https://github.com/Hack-Challenges-recommendation-system/Documentation/blob/develop/AGENTS.md) covers the project, its architecture, the rules from the case (the case data and keys never go into git; this repository is public), the commit conventions and the conventions of the Java services. Locally it is `AGENTS.md` in your clone of the Documentation repository. This file adds only what is specific to profile-service.

## How profiles work

- A profile appears when auth-service publishes `users.to.create` and disappears on `users.to.delete`. A repeated create message is ignored.
- auth-service sends no type headers, so each Kafka listener names its payload type with `spring.json.value.default.type`.
- Both services declare `users.to.create` and `users.to.delete` with 3 partitions, and the consumer never creates topics itself. Otherwise a consumer that starts first gets one-partition topics and misses messages in partitions added later.
- Saving the onboarding answers marks onboarding as completed.
- Never send a profile to an external API together with the user id. recommendation-service passes only the business fields to the LLM.

## Profile fields

| Field | Values |
|---|---|
| `nickname` | 3–30 characters; required on save |
| `businessStage` | `IDEA`, `LAUNCH`, `GROWTH`, `SCALING`, or empty |
| `businessSphere` | free text, up to 64 characters |
| `experience` | `NONE`, `BEGINNER`, `EXPERIENCED`, or empty |
| `goal` | the current task in the user's words, up to 500 characters |
| `preferredFormats` | up to 10 format codes, e.g. `VIDEO`, `ARTICLE` |
| `onboardingCompleted` | becomes `true` on the first save |
| `updatedAt` | when the answers last changed; usable as a cache key |

The stage and experience values are a first guess. Align them with the case data once someone has looked at it.

## Endpoints (under `/api/v1`)

| Method and path | Caller | What it does |
|---|---|---|
| `GET /profiles/me` | signed in | The profile; 404 until the create event has arrived |
| `PUT /profiles/me` | signed in | Saves every field above except `onboardingCompleted` and `updatedAt` |
| `GET /health` | anyone | 200 |

## Configuration

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5436/postgres` |
| `DB_USERNAME`, `DB_PASSWORD` | `admin`, `admin` |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |

`docker compose up -d` starts Postgres on `127.0.0.1:5436` and the Kafka shared with auth-service on 9092.

## Tests

- `ProfileServiceTest`: create, repeated create, save, missing profile.
- `ProfileControllerTest`: the `X-User-Id` header, validation, unknown enum values.
- `ProfileIntegrationTest`: the profile follows the account events on real Postgres and Kafka.
