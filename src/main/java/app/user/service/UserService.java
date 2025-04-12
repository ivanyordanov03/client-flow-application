package app.user.service;

import app.account.model.Account;
import app.exception.EmailAlreadyInUseException;
import app.notification.service.NotificationService;
import app.plan.properties.PlanProperties;
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
    private static final String ARCHIVED_FILTER = "archived";
    private static final String ACCESS_DENIED = "Access denied.";
    private static final String INVALID_FILTER_VALUE = "Invalid filter value: %s.";
    private static final String USER_ID_NOT_FOUND = "User with id [%s] not found";
    private static final String EMAIL_ALREADY_IN_USE = "Email address [%s] is already in use.";
    private static final String ACCOUNT_WITH_ID_CREATED = "New account with id [%s] has been created.";
    private static final String USER_WITH_EMAIL_DOES_NOT_EXIST = "User with email [%s] does not exist.";
    private static final String REGISTERED_NEW_USER_WITH_ID = "New user with id [%s] has been registered.";
    private static final String CREATED_NEW_ACCOUNT_FOR_USER_ID = "A new account with id [%s] has been created for user with id [%s].";
    private static final String USER_ID_MODIFIED_BY_USER_ID = "User with id [%s] has been modified by user with id [%s].";
    private static final String ACCOUNT_REACHED_MAX_USERS = "You have reached the maximum number of users for your current plan. Upgrade your account to proceed";
    private static final String USER_ID_SET_STATUS_BY_USER_ID = "User with id [%s] was set to %s by user with id [%s].";
    private static final String USER_ID_WAS_SET_ARCHIVE_STATUS_BY_USER_ID = "User with id [%s] was %s by user with id [%s].";
    private static final String USER_ID_DELETED_BY_USER_ID = "User with id [%s] was permanently DELETED by user with id [%s].";
    private static final String NEW_USER_EMAIL_SUBJECT = "Welcome to ClientFlow!";
    private static final String NEW_USER_EMAIL_BODY = "%s,\nYou have been invited to join your team at https://clientflow.com.\nYour password is %s";


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;
    private final PlanProperties planProperties;
    private final NotificationService notificationService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AccountService accountService,
                       PlanProperties planProperties,
                       NotificationService notificationService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountService = accountService;
        this.planProperties = planProperties;
        this.notificationService = notificationService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = getByEmail(email);

        return new AuthenticationMetadata(user.getId(), user.getEmail(), user.getPassword(), user.getUserRole(), user.getAccountId(), user.isActive());
    }

    @Transactional
    public void registerAccountOwner(UserRequest userRequest) {

        Account account = accountService.createNew(userRequest);

        User user = register(userRequest, account.getId());
        user.setUserRole(UserRole.PRIMARY_ADMIN);
        userRepository.save(user);

        accountService.insertOwnerId(account.getId(), user.getId());

        log.info(ACCOUNT_WITH_ID_CREATED.formatted(account.getId()));
        log.info(CREATED_NEW_ACCOUNT_FOR_USER_ID.formatted(account.getId(), account.getOwnerId()));
    }

    public User register(UserRequest userRequest, UUID accountId) {

        validateUserLimit(accountId, userRequest.getPlanName());
        validateEmail(userRequest.getEmail());

        User user = initiateNewUser(userRequest, accountId);
        if (userRequest.getPassword() != null) {
            user.setUserRole(UserRole.valueOf(userRequest.getUserRoleString()));
        }
        userRepository.save(user);
        notificationService.sendNotification(user.getId(),
                user.getEmail(),
                NEW_USER_EMAIL_SUBJECT,
                NEW_USER_EMAIL_BODY.formatted(userRequest.getFirstName(), userRequest.getPassword()));

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
            throw new IllegalStateException(ACCESS_DENIED);
        }

        user.setFirstName(editUserRequest.getFirstName());
        user.setLastName(editUserRequest.getLastName());

        if (!user.getEmail().equals(editUserRequest.getEmail())) {
            validateEmail(editUserRequest.getEmail());
            user.setEmail(editUserRequest.getEmail());
        }

        String newPassword = editUserRequest.getPassword();
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(editUserRequest.getPassword()));
        }

        if (!user.getUserRole().equals(UserRole.PRIMARY_ADMIN)) {
            user.setUserRole(UserRole.valueOf(editUserRequest.getUserRoleString()));
        }
        user.setDateUpdated(LocalDateTime.now());
        userRepository.save(user);

        log.info(USER_ID_MODIFIED_BY_USER_ID.formatted(id, editorId));
    }

    public User getById(UUID userId) {

        return userRepository.findById(userId).orElseThrow(() -> new NullPointerException(USER_ID_NOT_FOUND.formatted(userId)));
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
        } else if (filter.equals(ARCHIVED_FILTER)){
            return userRepository.findAllByAccountIdAndArchivedIsTrueOrderByUserRoleAscFirstNameAscLastNameAsc(accountId);
        } else {
            throw new IllegalArgumentException(INVALID_FILTER_VALUE.formatted(filter));
        }
    }

    public void validateUserLimit(UUID accountId, String planName) {
        int currentUsers = getAllByAccountIdNotArchivedOrdered(accountId).size();
        int maxUsers = getPlanMaxUsersFromPlanNameString(planName);

        if (currentUsers >= maxUsers) {
            throw new IllegalStateException(ACCOUNT_REACHED_MAX_USERS);
        }
    }

    public int getPlanMaxUsersFromPlanNameString(String planName) {

        return switch (planName) {
            case "PLUS" -> planProperties.getMaxUsersPlus();
            case "ESSENTIALS" -> planProperties.getMaxUsersEssentials();
            case "SIMPLE_START" -> planProperties.getMaxUsersSimpleStart();
            default -> throw new IllegalArgumentException("Unexpected plan value: " + planName);
        };
    }

    public void switchStatus(UUID id, UUID loggedUserId) {

        User user = getById(id);
        if (user.getUserRole().equals(UserRole.PRIMARY_ADMIN) || user.isArchived()) {
            return;
        }
        user.setActive(!user.isActive());
        user.setDateUpdated(LocalDateTime.now());
        userRepository.save(user);
        String status = (user.isActive()) ? "ACTIVE" : "INACTIVE";
        log.info(USER_ID_SET_STATUS_BY_USER_ID.formatted(id, status, loggedUserId));
    }

    public void switchArchveStatus(UUID id, UUID loggedUserId) {

        User user = getById(id);
        if (user.getUserRole().equals(UserRole.PRIMARY_ADMIN)) {
            return;
        }
        if (user.isActive()) {
            user.setActive(false);
        }

        user.setArchived(!user.isArchived());
        user.setDateUpdated(LocalDateTime.now());
        userRepository.save(user);
        String status = (user.isActive()) ? "ARCHIVED" : "RESTORED";
        log.info(USER_ID_WAS_SET_ARCHIVE_STATUS_BY_USER_ID.formatted(id, status, loggedUserId));
    }

    public void delete(UUID id, UUID loggedUserId) {

        User user = getById(id);
        if (user.getUserRole().equals(UserRole.PRIMARY_ADMIN)) {
            throw new IllegalStateException(ACCESS_DENIED);
        }
        userRepository.delete(getById(id));
        log.info(USER_ID_DELETED_BY_USER_ID.formatted(id, loggedUserId));
    }

    private void validateEmail(String email) {

        Optional<User> optionUser = userRepository.findByEmail(email);
        if (optionUser.isPresent()) {
            throw new EmailAlreadyInUseException(EMAIL_ALREADY_IN_USE.formatted(email));
        }
    }

    public User getByEmail(String email) {
        return userRepository.getUserByEmail(email).orElseThrow(() -> new NullPointerException(USER_WITH_EMAIL_DOES_NOT_EXIST.formatted(email)));
    }
}