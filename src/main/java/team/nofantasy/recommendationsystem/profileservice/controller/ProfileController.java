package team.nofantasy.recommendationsystem.profileservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import team.nofantasy.recommendationsystem.profileservice.dto.controller.profile.ProfileControllerDto;
import team.nofantasy.recommendationsystem.profileservice.dto.controller.profile.ProfileToUpdateControllerDto;
import team.nofantasy.recommendationsystem.profileservice.mapper.ProfileMapper;
import team.nofantasy.recommendationsystem.profileservice.service.ProfileService;

import java.util.UUID;

/**
 * Profile of the signed-in user. The API gateway puts the user id from the access token into {@code X-User-Id}.
 */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/me")
    public ProfileControllerDto getMyProfile(@RequestHeader("X-User-Id") UUID userId) {
        log.atInfo()
                .setMessage("API endpoint is called")
                .addKeyValue("API endpoint", "GET /profile/me")
                .addKeyValue("userId", userId)
                .log();

        return profileMapper.profileServiceToControllerDto(profileService.getProfile(userId));
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/me")
    public ProfileControllerDto updateMyProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid ProfileToUpdateControllerDto profileToUpdateControllerDto
    ) {
        log.atInfo()
                .setMessage("API endpoint is called")
                .addKeyValue("API endpoint", "PUT /profile/me")
                .addKeyValue("userId", userId)
                .log();

        return profileMapper.profileServiceToControllerDto(
                profileService.updateProfile(
                        profileMapper.profileToUpdateControllerToServiceDto(userId, profileToUpdateControllerDto)
                )
        );
    }
}
