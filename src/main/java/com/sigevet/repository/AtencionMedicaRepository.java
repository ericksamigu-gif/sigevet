package com.sigevet.repository;

import com.sigevet.entity.AtencionMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AtencionMedicaRepository extends JpaRepository<AtencionMedica, Integer> {

    List<AtencionMedica> findAllByOrderByFechaDescIdDesc();

    List<AtencionMedica> findByMascotaIdOrderByFechaDesc(Integer mascotaId);

    Optional<AtencionMedica> findByCitaId(Integer citaId);

    boolean existsByCitaId(Integer citaId);

    long countByFecha(LocalDate fecha);

    long countByMascotaId(Integer mascotaId);

    @Query("SELECT a FROM AtencionMedica a " +
           "LEFT JOIN a.mascota m " +
           "LEFT JOIN a.cita c " +
           "LEFT JOIN a.veterinario v " +
           "LEFT JOIN m.cliente cl " +
           "WHERE (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(m.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(m.especie) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(a.diagnostico) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(a.tratamiento) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(v.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(v.apellido) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(cl.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(cl.apellido) LIKE LOWER(CONCAT('%', :filtro, '%'))) " +
           "ORDER BY a.fecha DESC, a.id DESC")
    List<AtencionMedica> buscarPorFiltro(@Param("filtro") String filtro);
}
