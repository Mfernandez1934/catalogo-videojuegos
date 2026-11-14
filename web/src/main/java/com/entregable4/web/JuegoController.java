package com.entregable4.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.entregable4.catalogo.CatalogoService;
import com.entregable4.juegos.Genero;
import com.entregable4.juegos.Juego;
import com.entregable4.juegos.JuegoInvalidoException;
import com.entregable4.juegos.JuegoService;

@Controller
public class JuegoController {

    private final JuegoService juegoService;
    private final CatalogoService catalogoService;

    public JuegoController(JuegoService juegoService, CatalogoService catalogoService) {
        this.juegoService = juegoService;
        this.catalogoService = catalogoService;
    }

    /** Catalogo con filtros opcionales por genero y texto. */
    @GetMapping("/")
    public String catalogo(@RequestParam(required = false) Genero genero,
                           @RequestParam(required = false) String q,
                           Model model) {
        model.addAttribute("juegos", catalogoService.buscar(genero, q));
        model.addAttribute("conteo", catalogoService.contarPorGenero());
        model.addAttribute("generos", Genero.values());
        model.addAttribute("generoSeleccionado", genero);
        model.addAttribute("q", q == null ? "" : q);
        return "catalogo";
    }

    /** Formulario de alta. */
    @GetMapping("/juegos/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("generos", Genero.values());
        return "nuevo";
    }

    /** Registro de un juego nuevo. */
    @PostMapping("/juegos")
    public String registrar(@RequestParam String nombre,
                            @RequestParam(required = false) Genero genero,
                            @RequestParam(defaultValue = "0") int anio,
                            @RequestParam(required = false) String plataforma,
                            @RequestParam(required = false) String imagenUrl,
                            Model model,
                            RedirectAttributes redirect) {
        try {
            Juego juego = juegoService.registrar(new Juego(nombre, genero, anio, plataforma, imagenUrl));
            redirect.addFlashAttribute("mensaje", "Juego '" + juego.getNombre() + "' registrado");
            return "redirect:/";
        } catch (JuegoInvalidoException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("generos", Genero.values());
            model.addAttribute("nombre", nombre);
            model.addAttribute("generoSeleccionado", genero);
            model.addAttribute("anio", anio);
            model.addAttribute("plataforma", plataforma);
            model.addAttribute("imagenUrl", imagenUrl);
            return "nuevo";
        }
    }
}
