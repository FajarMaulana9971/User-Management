package com.unictive.usermanagement.security;

import com.unictive.usermanagement.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userSecurityService")
@RequiredArgsConstructor
public class UserSecurityService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean isOwner(Long userId, String username) {
        return userRepository.findByIdWithRole(userId)
                .map(user -> user.getUsername().equals(username))
                .orElse(false);
    }
}
