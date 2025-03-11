package app.user.service;

import app.account.model.Account;
import app.account.service.AccountService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.web.dto.RegisterUserRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private static final String REGISTERED_NEW_USER_WITH_ID = "New user with id [%s] has been registered.";
    private static final String CREATED_NEW_ACCOUNT_FOR_USER_ID = "A new account with id [%s] has been created for user with id [%s].";
    private static final String EMAIL_ALREADY_IN_USE = "Email address [%s] is already in use.";
    private static final String USER_WITH_ID_NOT_FOUND = "User with id [%s] not found";
    private static final String USER_WITH_EMAIL_DOES_NOT_EXIST = "User with email [%s] does not exist.";

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
    public void registerAccountOwner(RegisterUserRequest registerUserRequest) {

        Account account = accountService.createNew(registerUserRequest);

        User user = registerUser(registerUserRequest, account.getId());
        user.setUserRole(UserRole.OWNER);
        account.setOwnerId(user.getId());

        log.info(CREATED_NEW_ACCOUNT_FOR_USER_ID.formatted(account.getId(), account.getOwnerId()));
    }

    public User registerUser(RegisterUserRequest registerUserRequest, UUID accountId) {

        Optional<User> optionUser = userRepository.findByEmail(registerUserRequest.getEmail());
        if (optionUser.isPresent()) {
            throw new RuntimeException(EMAIL_ALREADY_IN_USE.formatted(registerUserRequest.getEmail()));
        }

        User user = initiateNewUser(registerUserRequest, accountId);
        userRepository.save(user);

        log.info(REGISTERED_NEW_USER_WITH_ID.formatted(user.getId()));

        return user;
    }

    private User initiateNewUser(RegisterUserRequest registerUserRequest, UUID accountId) {

        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .firstName(registerUserRequest.getFirstName())
                .lastName(registerUserRequest.getLastName())
                .email(registerUserRequest.getEmail())
                .password(passwordEncoder.encode(registerUserRequest.getPassword()))
                .userRole(UserRole.USER)
                .isActive(true)
                .accountId(accountId)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    public User getById(UUID userId) {

        return userRepository.findById(userId).orElseThrow(() -> new NullPointerException(USER_WITH_ID_NOT_FOUND.formatted(userId)));
    }

    public List<User> getAllByAccountId(UUID accountId) {

        return userRepository.findAllByAccountId(accountId);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(USER_WITH_EMAIL_DOES_NOT_EXIST.formatted(email)));

        return new AuthenticationMetadata(user.getId(), user.getEmail(), user.getPassword(), user.getUserRole(), user.isActive());
    }
}