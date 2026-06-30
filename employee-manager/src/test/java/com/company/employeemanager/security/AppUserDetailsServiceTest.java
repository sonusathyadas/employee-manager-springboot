package com.company.employeemanager.security;

import com.company.employeemanager.entity.AppUser;
import com.company.employeemanager.entity.Role;
import com.company.employeemanager.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link AppUserDetailsService}.
 * Verifies that user loading by username correctly delegates to the repository
 * and throws the expected exception when a user is not found.
 */
@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    // -------------------------------------------------------------------------
    // loadUserByUsername tests
    // -------------------------------------------------------------------------

    @Test
    void loadUserByUsername_whenUserExists_returnsUserDetails() {
        // Arrange
        var appUser = AppUser.builder()
                .id(1L)
                .username("testuser")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        given(appUserRepository.findByUsername("testuser")).willReturn(Optional.of(appUser));

        // Act
        var result = appUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_whenUserExists_returnsCorrectAuthorities() {
        // Arrange
        var appUser = AppUser.builder()
                .id(1L)
                .username("adminuser")
                .password("encoded-password")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build();

        given(appUserRepository.findByUsername("adminuser")).willReturn(Optional.of(appUser));

        // Act
        var result = appUserDetailsService.loadUserByUsername("adminuser");

        // Assert
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority())
                .isEqualTo(Role.ROLE_ADMIN.name());
    }

    @Test
    void loadUserByUsername_whenUserDoesNotExist_throwsUsernameNotFoundException() {
        // Arrange
        given(appUserRepository.findByUsername("unknownuser")).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> appUserDetailsService.loadUserByUsername("unknownuser"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknownuser");
    }
}
