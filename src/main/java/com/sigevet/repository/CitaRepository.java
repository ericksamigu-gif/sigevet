package com.sigevet.repository;

import com.sigevet.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

    long countByMascotaId(Integer mascotaId);

    long countByEstadoIgnoreCase(String estado);

    long countByFecha(LocalDate fecha);

    List<Cita> findAllByOrderByFechaDescHoraDesc();

    List<Cita> findByEstadoIgnoreCaseOrderByFechaDescHoraDesc(String estado);

    List<Cita> findByFechaOrderByHoraAsc(LocalDate fecha);

    @Query("SELECT c FROM Cita c LEFT JOIN c.mascota m LEFT JOIN c.veterinario v LEFT JOIN m.cliente cl WHERE " +
           "(:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(m.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(c.motivo) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(c.estado) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(v.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(v.apellido) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(cl.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(cl.apellido) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    List<Cita> buscarPorFiltro(@Param("filtro") String filtro);
}
