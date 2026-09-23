package com.gergert.authservice.dto;

import com.gergert.authservice.dto.user.UserResponseDto;
import com.gergert.authservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING
)

public interface UserMapper {

    @Mapping(source = "id", target = "userId")
    UserResponseDto toUserDto(User user);
    List<UserResponseDto> toUserDto(List<User> users);
}
