package com.rama.cloudstorage.service.user;

import com.rama.cloudstorage.repository.user.UserRepository;
import com.rama.cloudstorage.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameIgnoreCase(username)
                .map(user -> new UserPrincipal(
                        user.getId(),
                        user.getUsername(),
                        user.getPassword()
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
}
