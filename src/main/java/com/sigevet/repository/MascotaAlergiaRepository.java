package com.sigevet.repository;

import com.sigevet.entity.MascotaAlergia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaAlergiaRepository extends JpaRepository<MascotaAlergia, Integer> {

    List<MascotaAlergia> findByMascotaId(Integer mascotaId);
}
