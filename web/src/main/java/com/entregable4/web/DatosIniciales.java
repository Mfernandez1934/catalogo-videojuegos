package com.entregable4.web;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.entregable4.juegos.Genero;
import com.entregable4.juegos.Juego;
import com.entregable4.juegos.JuegoRepository;

/**
 * Carga algunos juegos de ejemplo la primera vez que arranca la aplicacion
 * (si la base esta vacia). No se ejecuta en el perfil de pruebas.
 */
@Component
@Profile("!test")
public class DatosIniciales implements CommandLineRunner {

    private final JuegoRepository repository;

    public DatosIniciales(JuegoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.save(new Juego("The Legend of Zelda: Breath of the Wild", Genero.AVENTURA, 2017, "Nintendo Switch",
                "https://placehold.co/300x400/2e7d32/white?text=Zelda"));
        repository.save(new Juego("Elden Ring", Genero.RPG, 2022, "PC / PS5 / Xbox",
                "https://placehold.co/300x400/6d4c41/white?text=Elden+Ring"));
        repository.save(new Juego("Celeste", Genero.PLATAFORMAS, 2018, "PC / Switch",
                "https://placehold.co/300x400/ad1457/white?text=Celeste"));
        repository.save(new Juego("Age of Empires II", Genero.ESTRATEGIA, 1999, "PC",
                "https://placehold.co/300x400/1565c0/white?text=AoE+II"));
        repository.save(new Juego("Rocket League", Genero.DEPORTES, 2015, "PC / PS5 / Xbox / Switch",
                "https://placehold.co/300x400/ef6c00/white?text=Rocket+League"));
    }
}
