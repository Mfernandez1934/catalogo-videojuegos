package com.entregable4.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.entregable4.juegos.Genero;
import com.entregable4.juegos.Juego;
import com.entregable4.juegos.JuegoRepository;

/**
 * Pruebas de integracion: levantan la aplicacion completa (los tres modulos + H2 en memoria)
 * y la ejercitan por HTTP con MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogoWebIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JuegoRepository repository;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    @Test
    void laAplicacionLevantaYHealthDaUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void registrarJuegoPorFormularioLoGuardaYLoMuestraEnElCatalogo() throws Exception {
        mvc.perform(post("/juegos")
                        .param("nombre", "Hades")
                        .param("genero", "ACCION")
                        .param("anio", "2020")
                        .param("plataforma", "PC")
                        .param("imagenUrl", "https://ejemplo.com/hades.png"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(repository.existsByNombreIgnoreCase("Hades")).isTrue();

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hades")));
    }

    @Test
    void registrarJuegoInvalidoMuestraErrorYNoLoGuarda() throws Exception {
        mvc.perform(post("/juegos")
                        .param("nombre", "Juego viejo")
                        .param("genero", "OTRO")
                        .param("anio", "1800"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("debe estar entre")));

        assertThat(repository.count()).isZero();
    }

    @Test
    void filtrarPorGeneroMuestraSoloEseGenero() throws Exception {
        repository.save(new Juego("Elden Ring", Genero.RPG, 2022, "PC", null));
        repository.save(new Juego("Rocket League", Genero.DEPORTES, 2015, "PC", null));

        mvc.perform(get("/").param("genero", "RPG"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Elden Ring")))
                .andExpect(content().string(not(containsString("Rocket League"))));
    }

    @Test
    void buscarPorTextoFiltraElCatalogo() throws Exception {
        repository.save(new Juego("Celeste", Genero.PLATAFORMAS, 2018, "Switch", null));
        repository.save(new Juego("Doom", Genero.SHOOTER, 1993, "PC", null));

        mvc.perform(get("/").param("q", "celes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Celeste")))
                .andExpect(content().string(not(containsString("Doom"))));
    }

    @Test
    void formularioDeAltaResponde() throws Exception {
        mvc.perform(get("/juegos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registrar juego")));
    }
}
