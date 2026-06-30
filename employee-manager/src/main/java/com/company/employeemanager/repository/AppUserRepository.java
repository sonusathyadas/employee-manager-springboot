package com.company.employeemanager.repository;

import com.company.employeemanager.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link AppUser} entities.
 * Provides CRUD operations and a username-based lookup used during authentication.
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Finds a user by their unique username.
     *
     * @param username the login username to search for.
     * @return an {@link Optional} containing the user if found, or empty otherwise.
     */
    Optional<AppUser> findByUsername(String username);

    /**
     * Checks whether a user with the given username already exists.
     *
     * @param username the username to check.
     * @return {@code true} if the username is already taken.
     */
    boolean existsByUsername(String username);
}
