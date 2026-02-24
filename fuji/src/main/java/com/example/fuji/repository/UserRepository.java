package com.example.fuji.repository;
//để repository có thể tương tác với database
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.User;
import com.example.fuji.enums.Role;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    List<User> findByRole(Role role);
}
