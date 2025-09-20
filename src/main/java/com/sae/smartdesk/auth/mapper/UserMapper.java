package com.sae.smartdesk.auth.mapper;

import com.sae.smartdesk.auth.dto.ProfileResponse;
import com.sae.smartdesk.auth.entity.Role;
import com.sae.smartdesk.auth.entity.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "mfaEnabled", expression = "java(user.getMfaTotpSecret() != null)")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    ProfileResponse toProfile(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
