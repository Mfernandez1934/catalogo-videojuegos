package com.entregable4.juegos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JuegoRepository extends JpaRepository<Juego, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<Juego> findAllByOrderByNombreAsc();
}
