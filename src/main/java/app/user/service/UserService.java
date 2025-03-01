package app.user.service;

import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.web.dto.RegisterOwnerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerAccountOwner(RegisterOwnerRequest registerOwnerRequest) {

        User owner = createNew(registerOwnerRequest);
        owner.setUserRole(UserRole.OWNER);

        return userRepository.save(owner);
    }

    private User createNew(RegisterOwnerRequest registerOwnerRequest) {

        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .firstName(registerOwnerRequest.getFirstName())
                .lastName(registerOwnerRequest.getLastName())
                .email(registerOwnerRequest.getEmail())
                .password(passwordEncoder.encode(registerOwnerRequest.getPassword()))
                .userRole(UserRole.USER)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    public User getById(UUID userId) {

        return userRepository.findById(userId).orElseThrow(() -> new NullPointerException("User with id [%s] not found".formatted(userId)));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User with email [%s] does not exist.".formatted(email)));

        return new AuthenticationMetadata(user.getId(), user.getEmail(), user.getPassword(), user.getUserRole(), user.isActive);
    }
}
