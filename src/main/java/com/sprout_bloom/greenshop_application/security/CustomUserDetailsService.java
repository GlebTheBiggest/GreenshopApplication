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
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        log.info("⏳ Attempting to load user by identifier: {}", identifier);

        User user = userRepository.findByEmailOrUsername(identifier)
                .orElseThrow(() -> {
                    log.warn("🚨 User not found with identifier: {}", identifier);
                    return new UsernameNotFoundException("Користувача не знайдено: " + identifier);
                });

        log.info("✅ User loaded successfully: {} (ID: {})", user.getUsername(), user.getId());

        return new CustomUserDetails(user);
    }
}