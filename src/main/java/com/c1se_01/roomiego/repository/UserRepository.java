package com.c1se_01.roomiego.repository;

import com.c1se_01.roomiego.enums.Role;
import com.c1se_01.roomiego.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByRole(Role role);

    User findByFullName(String fullName);

    List<User> findByEmailIn(List<String> receiverEmails);
}
