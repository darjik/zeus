package org.zeus.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.ims.entity.User;
import org.zeus.ims.entity.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByActiveTrue();

    List<User> findByRole(UserRole role);

    List<User> findByActiveAndRole(Boolean active, UserRole role);

    // Dashboard statistics methods
    long countByActiveTrue();

    long countByActiveTrueAndRole(UserRole role);

    @Query("SELECT u FROM User u WHERE u.active = true AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> findActiveUsersBySearch(@Param("search") String search);

    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> findUsersBySearch(@Param("search") String search);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.active = true")
    long countActiveUsersByRole(@Param("role") UserRole role);
}
