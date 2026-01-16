package com.ansh.portfilio_tracker.Classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Join table entity representing the relationship between users and portfolios.
 * Supports many-to-many relationship: one user can have multiple portfolios,
 * and one portfolio can belong to multiple users (shared portfolios feature).
 *
 * TODO: When User entity is created, replace this with proper @ManyToMany relationship
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_portfolios",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "portfolio_id"}))
public class UserPortfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;
}
