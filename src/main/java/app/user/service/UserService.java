package app.user.service;

import app.account.service.AccountService;
import app.plan.model.Plan;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.web.dto.RegisterOwnerRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AccountService accountService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountService = accountService;
    }

    @Transactional
    public void registerAccountOwner(RegisterOwnerRequest registerOwnerRequest, Plan plan) {

        User owner = userToOwner(registerOwnerRequest);
        userRepository.save(owner);
        log.info("Registered new account owner with id [%s]".formatted(owner.getId()));

        accountService.createNew(registerOwnerRequest, owner, plan);

    }

    private User userToOwner(RegisterOwnerRequest registerOwnerRequest) {
        User user = initiateNewUser(registerOwnerRequest);
        user.setUserRole(UserRole.OWNER);

        return user;
    }


    private User initiateNewUser(RegisterOwnerRequest registerOwnerRequest) {

        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .firstName(registerOwnerRequest.getFirstName())
                .lastName(registerOwnerRequest.getLastName())
                .email(registerOwnerRequest.getEmail())
                .password(passwordEncoder.encode(registerOwnerRequest.getPassword()))
                .userRole(UserRole.OWNER)
                .isActive(true)
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

        return new AuthenticationMetadata(user.getId(), user.getEmail(), user.getPassword(), user.getUserRole(), user.isActive());
    }
}
