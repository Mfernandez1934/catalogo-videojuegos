package com.entregable4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada. Esta en el paquete raiz com.entregable4 para que Spring
 * escanee los tres modulos (juegos, catalogo y web): entidades, repositorios y servicios.
 */
@SpringBootApplication
public class CatalogoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogoApplication.class, args);
    }
}
