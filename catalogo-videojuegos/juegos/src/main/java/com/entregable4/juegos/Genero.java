package com.entregable4.juegos;

/**
 * Generos disponibles para clasificar los videojuegos.
 */
public enum Genero {
    ACCION("Acción"),
    AVENTURA("Aventura"),
    RPG("RPG"),
    SHOOTER("Shooter"),
    ESTRATEGIA("Estrategia"),
    DEPORTES("Deportes"),
    CARRERAS("Carreras"),
    PLATAFORMAS("Plataformas"),
    PUZZLE("Puzzle"),
    OTRO("Otro");

    private final String nombreVisible;

    Genero(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
