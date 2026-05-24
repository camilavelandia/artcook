package co.edu.unbosque.artcook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.artcook.services.AuditoriaService;
import co.edu.unbosque.artcook.services.VideoService;

/**
 * Controlador que gestiona la generación de videos a partir del contenido de una receta.
 * Recibe el JSON de la receta y produce un video que se almacena en el servidor.
 */
@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private AuditoriaService auditoriaService;

    /**
     * Genera un video a partir del contenido JSON de una receta.
     * Registra la acción en auditoría una vez el video es creado exitosamente.
     *
     * @param jsonReceta   contenido de la receta en formato JSON
     * @param nombreReceta nombre de la receta para identificar el video generado
     * @param usuarioId    identificador del usuario que solicita la generación
     * @return ruta del video generado o mensaje de error según el caso
     */
    @PostMapping("/generar")
    public ResponseEntity<?> generarVideo(@RequestBody String jsonReceta, @RequestParam String nombreReceta,
            @RequestParam long usuarioId) {

        try {
            String rutaVideo = videoService.generarVideo(jsonReceta, nombreReceta);
            System.out.println("RUTA VIDEO RETORNADA: '" + rutaVideo + "'");
            auditoriaService.registrarAccion(usuarioId, "GENERAR_VIDEO", "Video", null,
                    "Video generado para receta: " + nombreReceta);
            return new ResponseEntity<>(rutaVideo, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al generar el video: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}