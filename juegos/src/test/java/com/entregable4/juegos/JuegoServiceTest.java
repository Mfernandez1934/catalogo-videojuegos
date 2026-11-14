package com.entregable4.juegos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias del modulo juegos. El repositorio se simula con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class JuegoServiceTest {

    @Mock
    private JuegoRepository repository;

    @InjectMocks
    private JuegoService service;

    private Juego juegoValido() {
        return new Juego("Hollow Knight", Genero.PLATAFORMAS, 2017, "PC", "https://ejemplo.com/hk.png");
    }

    @Test
    void registrarJuegoValidoLoGuarda() {
        Juego juego = juegoValido();
        when(repository.existsByNombreIgnoreCase("Hollow Knight")).thenReturn(false);
        when(repository.save(juego)).thenReturn(juego);

        Juego guardado = service.registrar(juego);

        assertThat(guardado.getNombre()).isEqualTo("Hollow Knight");
        verify(repository).save(juego);
    }

    @Test
    void registrarQuitaEspaciosDelNombre() {
        Juego juego = new Juego("  Celeste  ", Genero.PLATAFORMAS, 2018, "PC", null);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Juego guardado = service.registrar(juego);

        assertThat(guardado.getNombre()).isEqualTo("Celeste");
    }

    @Test
    void registrarSinNombreFalla() {
        Juego juego = new Juego("  ", Genero.RPG, 2020, "PC", null);

        assertThatThrownBy(() -> service.registrar(juego))
                .isInstanceOf(JuegoInvalidoException.class)
                .hasMessageContaining("nombre");
        verify(repository, never()).save(any());
    }

    @Test
    void registrarSinGeneroFalla() {
        Juego juego = new Juego("Tetris", null, 1984, "Varios", null);

        assertThatThrownBy(() -> service.registrar(juego))
                .isInstanceOf(JuegoInvalidoException.class)
                .hasMessageContaining("género");
    }

    @Test
    void registrarConAnioAnteriorAlMinimoFalla() {
        Juego juego = new Juego("Juego antiguo", Genero.OTRO, 1900, "PC", null);

        assertThatThrownBy(() -> service.registrar(juego))
                .isInstanceOf(JuegoInvalidoException.class)
                .hasMessageContaining("año");
    }

    @Test
    void registrarConAnioMuyFuturoFalla() {
        int anioFuturo = Year.now().getValue() + 5;
        Juego juego = new Juego("Juego del futuro", Genero.OTRO, anioFuturo, "PC", null);

        assertThatThrownBy(() -> service.registrar(juego))
                .isInstanceOf(JuegoInvalidoException.class);
    }

    @Test
    void registrarConUrlInvalidaFalla() {
        Juego juego = new Juego("Doom", Genero.SHOOTER, 1993, "PC", "imagen.png");

        assertThatThrownBy(() -> service.registrar(juego))
                .isInstanceOf(JuegoInvalidoException.class)
                .hasMessageContaining("URL");
    }

    @Test
    void registrarNombreDuplicadoFalla() {
        Juego juego = juegoValido();
        when(repository.existsByNombreIgnoreCase("Hollow Knight")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(juego))
                .isInstanceOf(JuegoInvalidoException.class)
                .hasMessageContaining("Ya existe");
        verify(repository, never()).save(any());
    }

    @Test
    void listarTodosDevuelveLoDelRepositorio() {
        List<Juego> juegos = List.of(juegoValido());
        when(repository.findAllByOrderByNombreAsc()).thenReturn(juegos);

        assertThat(service.listarTodos()).hasSize(1);
    }
}
