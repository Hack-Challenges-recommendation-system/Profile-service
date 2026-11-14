# profile-service

The business profile of each user: the onboarding answers that personalization uses. It holds no personal data; the email stays in auth-service.

## Project

Recommendation System is an entry for Hack&Change 2026, Alfa-Bank case «Альфа-Курс: знания в нужный момент», track 1: personal recommendations of educational materials for entrepreneurs. A vendor LLM ranks and explains materials; the services around it supply context, check its answers and record what users do.

| Repository | Role |
|---|---|
| `api-gateway` | Single entry point. Checks access tokens, routes `/api/v1/**`, sets `X-User-Id`. |
| `auth-service` | Registration, login, access and refresh JWT, logout denylist. |
| `profile-service` | Business profile of the user (onboarding answers). |
| `catalog-service` | Catalog of materials from the case data, with Russian full-text search. |
| `metric-service` | Raw user events and the metrics computed from them. |
| `recommendation-service` | Builds recommendations with the LLM. |
| `frontend` | Angular web app. |
| `Documentation` | Architecture docs. Only developers commit there; agents never do. |

All repositories live in the GitHub organization `Hack-Challenges-recommendation-system` and are **public**.

## Rules for every change

These come from the case. Breaking one can disqualify the submission.

1. **The case data never goes into git or a Docker image**: not the dump, not a copy, not an extract. Services read it from a path mounted at runtime; `/data/` is in `.gitignore`. Synthetic test data written by the team may be committed.
2. **No keys, tokens or passwords in git.** Read them from environment variables; `env.example` files list names only.
3. **External APIs, such as the LLM, get only the fragments they need.** No user ids, names, emails or other personal data, and no paid or closed materials.
4. **Paid materials:** show only a short benefit and the provided link, never their content.
5. **Everything must start with one `docker compose up` from the README.** A submission that doesn't start scores 0. Don't add infrastructure or a required external service without agreeing on it first.
6. **Simple UI, no intrusive notifications.**

## Commits

Every commit follows [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/), in the style the history already uses:

    <type>(<scope>): <what was changed>

- **Type:** `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `build`, `ci`, `chore`, `style` or `revert`.
- **Scope:** optional; the area that changed, e.g. `(jwt)`, `(kafka)`, `(migrations)`, `(controllers)`, `(CI/CD)`.
- **Description:** English, past tense, e.g. `fix(cookie): logout cookie name was fixed`.
- **Breaking change:** `!` after the type or scope, plus a `BREAKING CHANGE:` footer saying what callers must change.
- One logical change per commit. Commit to `develop`; `main` receives merges from `develop`.

## Code conventions

- Java 25, Spring Boot 4.1.2, Maven. Base package `team.nofantasy.recommendationsystem.<service>`, entry point `Main`.
- Three layers: `controller` → `service` → `repository`. Controllers never touch repositories or entities.
- Two DTO layers, both Java records:
  - `dto/controller/<feature>/*ControllerDto`: HTTP requests and responses, with Jakarta validation.
  - `dto/service/<feature>/*ServiceDto`: between controllers and services. Kafka messages live in `dto/service/kafka`, import rows in `dto/service/importer`.
- Mapping goes through MapStruct interfaces in `mapper/`. Unmapped source or target properties fail the build, so map every field or ignore it explicitly. Keep methods in pairs: controller-to-service first, then service-to-controller.
- Entities in `entity/` use Lombok: `@Getter`, `@Setter`, `@Builder` and a protected no-args constructor. Enums of an entity are nested in it.
- Domain exceptions live in `exception/`; `controller/advice/GlobalExceptionHandler` turns them into HTTP statuses.
- Schema changes go only into new Flyway files in `src/main/resources/database/migrations` (`V<n>__<description>.sql`). Never edit a migration that a shared database already ran. Hibernate runs with `ddl-auto: validate`.
- Endpoints sit under `/api/v1` (`spring.mvc.servlet.path`). Each service owns distinct top-level paths, so the gateway can route by prefix.
- The gateway puts the signed-in user's id into `X-User-Id`. Read it with `@RequestHeader("X-User-Id") UUID userId`, never from the body.
- Log with the SLF4J fluent API: `log.atInfo().setMessage(...).addKeyValue(...).log()`. Files go to `/logs/<service>.log` as ECS JSON.
- Configuration comes from environment variables, with local defaults in `application.yaml`.

## Build and test

- `mvn -B -ntp verify` runs all tests. Integration tests use Testcontainers, so Docker must be running; without Docker they are skipped.
- `mvn -B -ntp -DskipTests package` builds `target/app.jar`, which the `Dockerfile` copies.
- `docker compose up -d` starts the local infrastructure of this service (`docker-compose.yaml`).
- In MockMvc tests, request the full path and set the servlet path: `get("/api/v1/...").servletPath("/api/v1")`.
- CI: `.github/workflows/ci-cd.yml` tests every push and pull request, pushes the image to Docker Hub from `main`, and deploys on a manual run. Its header lists the secrets and variables it needs.

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
