package com.sae.smartdesk.auth.service;

import com.sae.smartdesk.auth.entity.Role;
import com.sae.smartdesk.auth.entity.User;
import com.sae.smartdesk.auth.repository.RoleRepository;
import com.sae.smartdesk.auth.repository.UserRepository;
import com.sae.smartdesk.common.exception.NotFoundException;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
            .orElseThrow(() -> new NotFoundException("Role not found: " + name));
    }

    public User createUserIfNotExists(String username, String email, String fullName, String password, Set<Role> roles) {
        return userRepository.findByUsername(username)
            .orElseGet(() -> {
                User user = new User();
                user.setId(UUID.randomUUID());
                user.setUsername(username);
                user.setEmail(email);
                user.setFullName(fullName);
                user.setPasswordHash(passwordEncoder.encode(password));
                user.setRoles(roles);
                return userRepository.save(user);
            });
    }
}
