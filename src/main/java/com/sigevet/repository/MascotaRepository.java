package com.sigevet.repository;

import com.sigevet.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Integer> {

    List<Mascota> findByClienteId(Integer clienteId);

    long countByClienteId(Integer clienteId);

    long countByEspecieIgnoreCase(String especie);

    @Query("SELECT m FROM Mascota m LEFT JOIN m.cliente c WHERE " +
           "(:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(m.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(m.especie) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(m.raza) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "c.dni LIKE CONCAT('%', :filtro, '%'))")
    List<Mascota> buscarPorFiltro(@Param("filtro") String filtro);
}
