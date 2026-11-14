package com.entregable4.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.entregable4.juegos.Genero;
import com.entregable4.juegos.Juego;
import com.entregable4.juegos.JuegoService;

/**
 * Pruebas unitarias del modulo catalogo. JuegoService se simula con Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CatalogoServiceTest {

    @Mock
    private JuegoService juegoService;

    @InjectMocks
    private CatalogoService catalogo;

    @BeforeEach
    void datos() {
        when(juegoService.listarTodos()).thenReturn(List.of(
                new Juego("Zelda: Breath of the Wild", Genero.AVENTURA, 2017, "Switch", null),
                new Juego("Elden Ring", Genero.RPG, 2022, "PC", null),
                new Juego("FIFA 23", Genero.DEPORTES, 2022, "PS5", null),
                new Juego("Baldur's Gate 3", Genero.RPG, 2023, "PC", null)));
    }

    @Test
    void buscarSinFiltrosDevuelveTodoOrdenadoPorNombre() {
        List<Juego> resultado = catalogo.buscar(null, null);

        assertThat(resultado).extracting(Juego::getNombre)
                .containsExactly("Baldur's Gate 3", "Elden Ring", "FIFA 23", "Zelda: Breath of the Wild");
    }

    @Test
    void buscarPorGeneroFiltra() {
        List<Juego> resultado = catalogo.buscar(Genero.RPG, null);

        assertThat(resultado).extracting(Juego::getNombre)
                .containsExactly("Baldur's Gate 3", "Elden Ring");
    }

    @Test
    void buscarPorTextoIgnoraMayusculasYBuscaEnPlataforma() {
        assertThat(catalogo.buscar(null, "zelda")).hasSize(1);
        assertThat(catalogo.buscar(null, "pc")).hasSize(2);
    }

    @Test
    void buscarCombinaGeneroYTexto() {
        assertThat(catalogo.buscar(Genero.RPG, "elden"))
                .extracting(Juego::getNombre).containsExactly("Elden Ring");
    }

    @Test
    void buscarSinCoincidenciasDevuelveListaVacia() {
        assertThat(catalogo.buscar(Genero.SHOOTER, null)).isEmpty();
    }

    @Test
    void agruparPorGeneroSoloIncluyeGenerosConJuegos() {
        Map<Genero, List<Juego>> grupos = catalogo.agruparPorGenero();

        assertThat(grupos).containsOnlyKeys(Genero.AVENTURA, Genero.RPG, Genero.DEPORTES);
        assertThat(grupos.get(Genero.RPG)).hasSize(2);
    }

    @Test
    void contarPorGenero() {
        Map<Genero, Long> conteo = catalogo.contarPorGenero();

        assertThat(conteo.get(Genero.RPG)).isEqualTo(2L);
        assertThat(conteo.get(Genero.DEPORTES)).isEqualTo(1L);
    }
}
