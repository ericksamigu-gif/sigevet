package com.sigevet.repository;

import com.sigevet.entity.Vacuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacunaRepository extends JpaRepository<Vacuna, Integer> {

    List<Vacuna> findByEspecieDestinoIgnoreCase(String especieDestino);
}
