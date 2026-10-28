package com.sigevet.repository;

import com.sigevet.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByDni(String dni);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Integer id);

    @Query("SELECT c FROM Cliente c WHERE " +
           "(:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "c.dni LIKE CONCAT('%', :filtro, '%') OR " +
           "c.telefono LIKE CONCAT('%', :filtro, '%') OR " +
           "LOWER(c.correo) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    List<Cliente> buscarPorFiltro(@Param("filtro") String filtro);
}
