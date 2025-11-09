package com.c1se_01.roomiego.repository;

import com.c1se_01.roomiego.model.Roommate;
import com.c1se_01.roomiego.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoommateRepository extends JpaRepository<Roommate, Long> {
    List<Roommate> findAllByGender(String gender);
    Optional<Roommate> findByUser(User user);
}
