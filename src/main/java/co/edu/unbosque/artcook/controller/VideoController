package co.edu.unbosque.artcook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.artcook.services.AuditoriaService;
import co.edu.unbosque.artcook.services.VideoService;

/**
 * Controlador que gestiona la generacion de videos de recetas.
 * Usa fal.ai para imagenes, ElevenLabs para audio y FFmpeg para combinarlos.
 */
@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private AuditoriaService auditoriaService;

    /**
     * Genera un video a partir del JSON de una receta enviado directamente.
     * No requiere que la receta este guardada en la base de datos.
     *
     * @param jsonReceta   JSON de la receta generado por la IA
     * @param nombreReceta nombre de la receta para el archivo de video
     * @param usuarioId    ID del usuario que solicita el video
     * @return ruta del video generado
     */
    @PostMapping("/generar")
    public ResponseEntity<?> generarVideo(
            @RequestParam String jsonReceta,
            @RequestParam String nombreReceta,
            @RequestParam long usuarioId) {

        try {
            String rutaVideo = videoService.generarVideo(jsonReceta, nombreReceta);
            auditoriaService.registrarAccion(usuarioId, "GENERAR_VIDEO", "Video", null,
                    "Video generado para receta: " + nombreReceta);
            return new ResponseEntity<>(rutaVideo, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al generar el video: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
