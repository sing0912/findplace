package com.petpro.domain.pet.repository;

import com.petpro.domain.pet.entity.PetChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PetChecklistRepository extends JpaRepository<PetChecklist, Long> {

    Optional<PetChecklist> findByPetId(Long petId);

    boolean existsByPetId(Long petId);

    void deleteByPetId(Long petId);
}
