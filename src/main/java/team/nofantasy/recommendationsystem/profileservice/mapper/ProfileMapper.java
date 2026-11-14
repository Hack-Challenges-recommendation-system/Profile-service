package team.nofantasy.recommendationsystem.profileservice.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import team.nofantasy.recommendationsystem.profileservice.dto.controller.profile.ProfileControllerDto;
import team.nofantasy.recommendationsystem.profileservice.dto.controller.profile.ProfileToUpdateControllerDto;
import team.nofantasy.recommendationsystem.profileservice.dto.service.profile.ProfileServiceDto;
import team.nofantasy.recommendationsystem.profileservice.dto.service.profile.ProfileToUpdateServiceDto;
import team.nofantasy.recommendationsystem.profileservice.entity.Profile;

import java.util.UUID;

@Mapper
public interface ProfileMapper {
    ProfileToUpdateServiceDto profileToUpdateControllerToServiceDto(
            UUID userId,
            ProfileToUpdateControllerDto profileToUpdateControllerDto
    );

    ProfileControllerDto profileServiceToControllerDto(ProfileServiceDto profileServiceDto);

    @BeanMapping(ignoreUnmappedSourceProperties = {"createdAt"})
    ProfileServiceDto profileEntityToServiceDto(Profile profile);
}
