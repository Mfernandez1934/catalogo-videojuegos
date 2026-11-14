package com.entregable4.juegos;

/**
 * Se lanza cuando los datos de un juego no cumplen las reglas de registro.
 */
public class JuegoInvalidoException extends RuntimeException {

    public JuegoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
