package com.findplace.domain.pet.repository;

import com.findplace.domain.pet.entity.Pet;
import com.findplace.domain.pet.entity.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    @Query("SELECT p FROM Pet p WHERE p.user.id = :userId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Pet> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Pet p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Pet> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT COUNT(p) FROM Pet p WHERE p.user.id = :userId AND p.deletedAt IS NULL")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(p) FROM Pet p WHERE p.user.id = :userId AND p.isDeceased = false AND p.deletedAt IS NULL")
    long countAliveByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(p) FROM Pet p WHERE p.user.id = :userId AND p.isDeceased = true AND p.deletedAt IS NULL")
    long countDeceasedByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Pet p WHERE p.user.id = :userId AND p.species = :species AND p.deletedAt IS NULL")
    List<Pet> findByUserIdAndSpecies(@Param("userId") Long userId, @Param("species") Species species);

    @Query("SELECT p FROM Pet p WHERE p.user.id = :userId AND p.isDeceased = :isDeceased AND p.deletedAt IS NULL")
    List<Pet> findByUserIdAndIsDeceased(@Param("userId") Long userId, @Param("isDeceased") boolean isDeceased);

    boolean existsByIdAndUserId(Long id, Long userId);
}
