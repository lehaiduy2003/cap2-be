package com.c1se_01.roomiego.repository;

import com.c1se_01.roomiego.model.Contract;
import com.c1se_01.roomiego.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByOwnerOrTenant(User owner, User tenant);
}
