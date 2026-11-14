package com.entregable4.juegos;

import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Registro y consulta de videojuegos. Contiene las reglas de validacion.
 */
@Service
public class JuegoService {

    public static final int ANIO_MINIMO = 1800;

    private final JuegoRepository repository;

    public JuegoService(JuegoRepository repository) {
        this.repository = repository;
    }

    public Juego registrar(Juego juego) {
        validar(juego);
        juego.setNombre(juego.getNombre().trim());
        if (repository.existsByNombreIgnoreCase(juego.getNombre())) {
            throw new JuegoInvalidoException("Ya existe un juego llamado '" + juego.getNombre() + "'");
        }
        return repository.save(juego);
    }

    public List<Juego> listarTodos() {
        return repository.findAllByOrderByNombreAsc();
    }

    void validar(Juego juego) {
        if (juego == null) {
            throw new JuegoInvalidoException("El juego no puede ser nulo");
        }
        if (juego.getNombre() == null || juego.getNombre().isBlank()) {
            throw new JuegoInvalidoException("El nombre es obligatorio");
        }
        if (juego.getGenero() == null) {
            throw new JuegoInvalidoException("El género es obligatorio");
        }
        int anioMaximo = Year.now().getValue() + 1;
        if (juego.getAnio() < ANIO_MINIMO || juego.getAnio() > anioMaximo) {
            throw new JuegoInvalidoException(
                    "El año debe estar entre " + ANIO_MINIMO + " y " + anioMaximo);
        }
        String url = juego.getImagenUrl();
        if (url != null && !url.isBlank()
                && !(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new JuegoInvalidoException("La URL de la imagen debe empezar con http:// o https://");
        }
    }
}
