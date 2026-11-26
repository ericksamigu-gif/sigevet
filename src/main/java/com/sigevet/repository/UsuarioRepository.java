package com.sigevet.repository;

import com.sigevet.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Usuario findByUsername(String username);

    @Query(value = "SELECT * FROM usuario WHERE username = :username", nativeQuery = true)
    Usuario findByUsernameAnyStatus(@Param("username") String username);

    List<Usuario> findByRolIgnoreCase(String rol);
}
