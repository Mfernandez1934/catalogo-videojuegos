package com.entregable4.catalogo;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.entregable4.juegos.Genero;
import com.entregable4.juegos.Juego;
import com.entregable4.juegos.JuegoService;

/**
 * Arma la vista del catalogo: clasificacion por genero y busqueda.
 * Depende del modulo juegos solo a traves de JuegoService.
 */
@Service
public class CatalogoService {

    private final JuegoService juegoService;

    public CatalogoService(JuegoService juegoService) {
        this.juegoService = juegoService;
    }

    /**
     * Filtra el catalogo por genero (opcional) y por texto en nombre o plataforma (opcional).
     * El resultado queda ordenado por nombre.
     */
    public List<Juego> buscar(Genero genero, String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return juegoService.listarTodos().stream()
                .filter(j -> genero == null || j.getGenero() == genero)
                .filter(j -> filtro.isEmpty() || contiene(j.getNombre(), filtro) || contiene(j.getPlataforma(), filtro))
                .sorted(Comparator.comparing(Juego::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Agrupa todos los juegos por genero, en el orden en que estan declarados los generos.
     * Solo incluye generos que tengan al menos un juego.
     */
    public Map<Genero, List<Juego>> agruparPorGenero() {
        Map<Genero, List<Juego>> resultado = new EnumMap<>(Genero.class);
        for (Juego juego : buscar(null, null)) {
            resultado.computeIfAbsent(juego.getGenero(), g -> new java.util.ArrayList<>()).add(juego);
        }
        return resultado;
    }

    /**
     * Cantidad de juegos por genero (solo generos con juegos).
     */
    public Map<Genero, Long> contarPorGenero() {
        Map<Genero, Long> conteo = new EnumMap<>(Genero.class);
        agruparPorGenero().forEach((genero, juegos) -> conteo.put(genero, (long) juegos.size()));
        return conteo;
    }

    private static boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }
}
