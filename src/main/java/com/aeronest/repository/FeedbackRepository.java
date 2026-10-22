package com.aeronest.repository;

import com.aeronest.domain.Feedback;
import com.aeronest.domain.DomainEnums.FeedbackStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @EntityGraph(attributePaths = {"account", "booking"})
    List<Feedback> findAllByOrderByCreatedAtDesc();
    List<Feedback> findByAccountIdOrderByCreatedAtDesc(Long accountId);
    long countByStatus(FeedbackStatus status);
}
