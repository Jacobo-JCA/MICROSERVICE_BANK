package com.sofka.accounts.repository;

import com.sofka.accounts.domain.Account;
import com.sofka.accounts.domain.Transaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @EntityGraph(attributePaths = {"account"})
    Optional<Transaction> findTopByAccountOrderByDateDesc(Account account);
    @EntityGraph(attributePaths = {"account"})
    List<Transaction> findByAccountClientIdAndDateBetween(Long clientId, LocalDateTime startDate, LocalDateTime endDate);
    @EntityGraph(attributePaths = {"account"})
    List<Transaction> findByAccountAndDateBetween(Account account, LocalDateTime startDate, LocalDateTime endDate);
}
