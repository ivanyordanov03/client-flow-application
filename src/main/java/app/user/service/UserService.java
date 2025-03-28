package app.user.service;

import app.account.model.Account;
import app.account.service.AccountService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.web.dto.EditUserRequest;
import app.web.dto.UserRequest;
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

    private static final String DEFAULT_FILTER = "current";
    private static final String USER_WITH_ID_NOT_FOUND = "User with id [%s] not found";
    private static final String EMAIL_ALREADY_IN_USE = "Email address [%s] is already in use.";
    private static final String USER_WITH_EMAIL_DOES_NOT_EXIST = "User with email [%s] does not exist.";
    private static final String REGISTERED_NEW_USER_WITH_ID = "New user with id [%s] has been registered.";
    private static final String CREATED_NEW_ACCOUNT_FOR_USER_ID = "A new account with id [%s] has been created for user with id [%s].";
    private static final String USER_WITH_ID_WAS_MODIFIED_BY_USER_WITH_ID = "User with id [%s] has been modified by user with id [%s].";
//    private static final String ACCOUNT_MUST_HAVE_AT_LEAST_ONE_PRIMARY_ADMIN = "There must be at least one primary admin in your account.";
    private static final String ONLY_PRIMARY_ADMIN_CAN_EDIT_PRIMARY_ADMIN = "Only users with user role Primary Admin can modify other users with Primary admin role.";


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

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(USER_WITH_EMAIL_DOES_NOT_EXIST.formatted(email)));

        return new AuthenticationMetadata(user.getId(), user.getEmail(), user.getPassword(), user.getUserRole(), user.isActive());
    }

    @Transactional
    public void registerAccountOwner(UserRequest registerUserRequest) {

        Account account = accountService.createNew(registerUserRequest);

        User user = register(registerUserRequest, account.getId());
        user.setUserRole(UserRole.PRIMARY_ADMIN);
        userRepository.save(user);

        accountService.insertOwnerId(account.getId(), user.getId());

        log.info(CREATED_NEW_ACCOUNT_FOR_USER_ID.formatted(account.getId(), account.getOwnerId()));
    }

    public User register(UserRequest registerUserRequest, UUID accountId) {

        Optional<User> optionUser = userRepository.findByEmail(registerUserRequest.getEmail());
        if (optionUser.isPresent()) {
            throw new IllegalArgumentException(EMAIL_ALREADY_IN_USE.formatted(registerUserRequest.getEmail()));
        }

        User user = initiateNewUser(registerUserRequest, accountId);
        if (registerUserRequest.getPassword() != null) {
            user.setUserRole(UserRole.valueOf(registerUserRequest.getUserRoleString()));
        }
        userRepository.save(user);

        log.info(REGISTERED_NEW_USER_WITH_ID.formatted(user.getId()));

        return user;
    }

    private User initiateNewUser(UserRequest registerUserRequest, UUID accountId) {

        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .firstName(registerUserRequest.getFirstName())
                .lastName(registerUserRequest.getLastName())
                .email(registerUserRequest.getEmail())
                .password(passwordEncoder.encode(registerUserRequest.getPassword()))
                .userRole(UserRole.USER)
                .active(true)
                .accountId(accountId)
                .dateCreated(now)
                .dateUpdated(now)
                .build();
    }

    public void edit(UUID id, EditUserRequest editUserRequest, UUID editorId) {

        User user = getById(id);
        UserRole userRole = user.getUserRole();
        User editor = getById(editorId);
        UserRole editorUserRole = editor.getUserRole();

        if (userRole.equals(UserRole.PRIMARY_ADMIN) && !editorUserRole.equals(UserRole.PRIMARY_ADMIN)) {
            throw new IllegalStateException(ONLY_PRIMARY_ADMIN_CAN_EDIT_PRIMARY_ADMIN);
        }

        user.setFirstName(editUserRequest.getFirstName());
        user.setLastName(editUserRequest.getLastName());
        user.setEmail(editUserRequest.getEmail());
        user.setUserRole(UserRole.valueOf(editUserRequest.getUserRoleString()));

        String newPassword = editUserRequest.getPassword();
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(editUserRequest.getPassword()));
        }

        userRepository.save(user);

        log.info(USER_WITH_ID_WAS_MODIFIED_BY_USER_WITH_ID.formatted(id, editorId));
    }

    public User getById(UUID userId) {

        return userRepository.findById(userId).orElseThrow(() -> new NullPointerException(USER_WITH_ID_NOT_FOUND.formatted(userId)));
    }

    public List<User> getAllByAccountId(UUID accountId) {

        return userRepository.findAllByAccountId(accountId);
    }

    public List<User> getAllByAccountIdNotArchivedOrdered(UUID accountId) {

        return userRepository.findAllByAccountIdAndArchivedIsFalseOrderByUserRoleAscFirstNameAscLastNameAsc(accountId);
    }

    public List<User> getAllByAccountIdAndFilterOrdered(UUID accountId, String filter, String userRole) {

        if (userRole.equals(UserRole.USER.toString()) || filter.equals(DEFAULT_FILTER)) {
            return userRepository.findAllByAccountIdAndArchivedIsFalseOrderByUserRoleAscFirstNameAscLastNameAsc(accountId);
        } else {
            return userRepository.findAllByAccountIdAndArchivedIsTrueOrderByUserRoleAscFirstNameAscLastNameAsc(accountId);
        }
    }
}