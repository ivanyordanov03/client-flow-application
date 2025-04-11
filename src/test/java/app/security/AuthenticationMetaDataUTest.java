package app.security;

import app.user.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationMetaDataUTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final String email = "test@example.com";
    private final String password = "securePassword123";
    private final UserRole userRole = UserRole.ADMINISTRATOR;

    @Test
    void shouldCorrectlyImplementUserDetails() {

        AuthenticationMetadata data = new AuthenticationMetadata(
                userId, email, password, userRole, accountId, true);

        assertThat(data.getUsername()).isEqualTo(email);
        assertThat(data.getPassword()).isEqualTo(password);
        assertThat(data.getUserId()).isEqualTo(userId);
        assertThat(data.getAccountId()).isEqualTo(accountId);

        Collection<? extends GrantedAuthority> authorities = data.getAuthorities();
        assertThat(authorities)
                .hasSize(1)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMINISTRATOR");

        assertThat(data.isAccountNonExpired()).isTrue();
        assertThat(data.isAccountNonLocked()).isTrue();
        assertThat(data.isCredentialsNonExpired()).isTrue();
        assertThat(data.isEnabled()).isTrue();
    }

    @Test
    void shouldReflectInactiveStatus() {

        AuthenticationMetadata auth = new AuthenticationMetadata(
                userId, email, password, userRole, accountId, false);

        assertThat(auth.isAccountNonExpired()).isFalse();
        assertThat(auth.isAccountNonLocked()).isFalse();
        assertThat(auth.isCredentialsNonExpired()).isFalse();
        assertThat(auth.isEnabled()).isFalse();
    }

    @Test
    void shouldHandleDifferentRoles() {

        AuthenticationMetadata userAuth = new AuthenticationMetadata(
                userId, email, password, UserRole.USER, accountId, true);

        assertThat(userAuth.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }
}
