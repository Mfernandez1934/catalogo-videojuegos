package com.entregable4.juegos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Videojuego registrado en el catalogo.
 */
@Entity
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genero genero;

    private int anio;

    private String plataforma;

    @Column(length = 1000)
    private String imagenUrl;

    protected Juego() {
        // requerido por JPA
    }

    public Juego(String nombre, Genero genero, int anio, String plataforma, String imagenUrl) {
        this.nombre = nombre;
        this.genero = genero;
        this.anio = anio;
        this.plataforma = plataforma;
        this.imagenUrl = imagenUrl;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Genero getGenero() {
        return genero;
    }

    public int getAnio() {
        return anio;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
