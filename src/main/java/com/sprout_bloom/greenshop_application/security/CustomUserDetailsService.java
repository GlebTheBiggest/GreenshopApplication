package com.sprout_bloom.greenshop_application.security;

import com.sprout_bloom.greenshop_application.entity.User;
import com.sprout_bloom.greenshop_application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        log.info("Attempting to load user by identifier: {}", identifier);

        User user = userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> {
                            log.warn("User not found with identifier: {}", identifier);
                            return new UsernameNotFoundException("User not found with identifier: " + identifier);
                        }));

        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("User account is disabled: " + identifier);
        }

        log.info("User {} loaded successfully", user.getEmail());
        return new CustomUserDetails(user);
    }

    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        log.info("Attempting to load user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new UsernameNotFoundException("User not found with ID: " + id);
                });

        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("User account is disabled: " + id);
        }

        log.info("User {} loaded successfully", user.getEmail());
        return new CustomUserDetails(user);
    }
}