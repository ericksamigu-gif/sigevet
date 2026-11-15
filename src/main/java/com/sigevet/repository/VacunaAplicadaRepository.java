package com.sigevet.repository;

import com.sigevet.entity.VacunaAplicada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacunaAplicadaRepository extends JpaRepository<VacunaAplicada, Integer> {

    @Query("SELECT va FROM VacunaAplicada va " +
           "LEFT JOIN va.atencionMedica am " +
           "LEFT JOIN va.vacuna v " +
           "WHERE am.mascota.id = :mascotaId " +
           "ORDER BY va.fechaAplicacion DESC")
    List<VacunaAplicada> findByMascotaId(@Param("mascotaId") Integer mascotaId);
}
