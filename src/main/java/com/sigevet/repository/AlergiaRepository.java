package com.sigevet.repository;

import com.sigevet.entity.Alergia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlergiaRepository extends JpaRepository<Alergia, Integer> {

    List<Alergia> findByCategoriaIgnoreCase(String categoria);
}
