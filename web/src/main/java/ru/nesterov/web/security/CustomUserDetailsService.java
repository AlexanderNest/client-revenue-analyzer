package ru.nesterov.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.user.UserService;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDto userDto = userService.getUserByUsername(username);

        if (userDto == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return new User(
                userDto.getUsername(),
                "",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + userDto.getRole().name())));
    }
}
