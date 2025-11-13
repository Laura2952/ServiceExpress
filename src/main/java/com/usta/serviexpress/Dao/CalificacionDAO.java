package com.usta.serviexpress.Dao;

import com.usta.serviexpress.Entity.CalificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CalificacionDAO
 *
 * Purpose:
 * - Data Access Object (DAO) for performing CRUD operations on CalificacionEntity.
 * - Extends Spring Data JPA's JpaRepository to inherit common persistence methods such as save, findById, findAll, delete, etc.
 *
 * Type parameters:
 * - CalificacionEntity: The entity type managed by this repository.
 * - Long: Type of the entity's primary key.
 *
 * Important considerations:
 * - No custom queries are defined here; JpaRepository provides default implementations.
 * - Additional query methods can be added following Spring Data JPA naming conventions.
 */
public interface CalificacionDAO extends JpaRepository<CalificacionEntity, Long> {
}

/*
Summary (Technical Note):
CalificacionDAO is a Spring Data JPA repository interface for managing CalificacionEntity instances.
It provides standard CRUD and pagination operations out of the box. Custom queries can be added
as needed by defining method signatures according to Spring Data JPA conventions.
*/
