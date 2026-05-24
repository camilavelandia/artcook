package co.edu.unbosque.artcook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.artcook.dto.RecetaDTO;
import co.edu.unbosque.artcook.dto.TipoRecetaDTO;
import co.edu.unbosque.artcook.exception.CampoVacioException;
import co.edu.unbosque.artcook.exception.PromptVacioException;
import co.edu.unbosque.artcook.services.AuditoriaService;
import co.edu.unbosque.artcook.services.IAService;
import co.edu.unbosque.artcook.services.RecetaService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST que gestiona la generación de recetas y manualidades
 * mediante las IAs disponibles: GPT, Gemini y Claude.
 *
 * <p>Expone endpoints individuales por IA ({@code /generar-gpt},
 * {@code /generar-gemini}, {@code /generar-claude}), un endpoint
 * unificado con selección de IA ({@code /generar}) y un endpoint
 * que lanza las tres en paralelo ({@code /generar-todas}).</p>
 */
@RestController
@RequestMapping("/ia")
public class IAController {

    @Autowired
    private IAService iaService;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private RecetaService recetaSer;

    /**
     * Genera contenido usando OpenAI GPT con el prompt, tipo y porciones indicados.
     *
     * @param prompt     descripción de la receta o manualidad a generar
     * @param tipo       tipo de contenido a generar, por defecto COCINA
     * @param usuarioId  identificador del usuario que hace la solicitud
     * @param porciones  número de porciones para la receta, por defecto 1
     * @return el contenido generado por GPT o un mensaje de error según el caso
     */
    @PostMapping("/generar-gpt")
    public ResponseEntity<?> generarConGPT(
            @RequestParam String prompt,
            @RequestParam(required = false, defaultValue = "COCINA") String tipo,
            @RequestParam long usuarioId,
            @RequestParam(required = false, defaultValue = "1") Integer porciones) {
        try {
            String resultado = iaService.generarRecetaConGPT(prompt, tipo, porciones);
            auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_GPT", "IA", null,
                    "Receta generada con GPT. Prompt: " + prompt);
            return new ResponseEntity<>(resultado, HttpStatus.OK);
        } catch (PromptVacioException e) {
            return new ResponseEntity<>("El prompt no puede estar vacío o es muy corto.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inesperado al consultar GPT.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Genera contenido usando Google Gemini con el prompt, tipo y porciones indicados.
     *
     * @param prompt     descripción de la receta o manualidad a generar
     * @param tipo       tipo de contenido a generar, por defecto COCINA
     * @param usuarioId  identificador del usuario que hace la solicitud
     * @param porciones  número de porciones para la receta, por defecto 1
     * @return el contenido generado por Gemini o un mensaje de error según el caso
     */
    @PostMapping("/generar-gemini")
    public ResponseEntity<?> generarConGemini(
            @RequestParam String prompt,
            @RequestParam(required = false, defaultValue = "COCINA") String tipo,
            @RequestParam long usuarioId,
            @RequestParam(required = false, defaultValue = "1") Integer porciones) {
        try {
            String resultado = iaService.generarRecetaConGemini(prompt, tipo, porciones);
            auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_GEMINI", "IA", null,
                    "Receta generada con Gemini. Prompt: " + prompt);
            return new ResponseEntity<>(resultado, HttpStatus.OK);
        } catch (PromptVacioException e) {
            return new ResponseEntity<>("El prompt no puede estar vacío o es muy corto.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inesperado al consultar Gemini.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Genera contenido usando Anthropic Claude con el prompt, tipo y porciones indicados.
     *
     * @param prompt     descripción de la receta o manualidad a generar
     * @param tipo       tipo de contenido a generar, por defecto COCINA
     * @param usuarioId  identificador del usuario que hace la solicitud
     * @param porciones  número de porciones para la receta, por defecto 1
     * @return el contenido generado por Claude o un mensaje de error según el caso
     */
    @PostMapping("/generar-claude")
    public ResponseEntity<?> generarConClaude(
            @RequestParam String prompt,
            @RequestParam(required = false, defaultValue = "COCINA") String tipo,
            @RequestParam long usuarioId,
            @RequestParam(required = false, defaultValue = "1") Integer porciones) {
        try {
            String resultado = iaService.generarRecetaConClaude(prompt, tipo, porciones);
            auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_CLAUDE", "IA", null,
                    "Receta generada con Claude. Prompt: " + prompt);
            return new ResponseEntity<>(resultado, HttpStatus.OK);
        } catch (PromptVacioException e) {
            return new ResponseEntity<>("El prompt no puede estar vacío o es muy corto.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inesperado al consultar Claude.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Genera contenido con la IA seleccionada por el usuario y guarda la receta resultante.
     *
     * @param prompt     descripción de la receta o manualidad a generar
     * @param tipo       tipo de contenido, puede ser COCINA o MANUALIDAD
     * @param porciones  número de porciones, opcional, por defecto 1
     * @param usuarioId  identificador del usuario que hace la solicitud
     * @param ia         nombre de la IA a usar: gpt, gemini o claude
     * @param titulo     título de la receta, si no se envía se usa el prompt como título
     * @return mapa con el id de la receta, el contenido generado y la IA usada, o mensaje de error
     */
    @PostMapping("/generar")
    public ResponseEntity<?> generarConIASeleccionada(
            @RequestParam String prompt,
            @RequestParam String tipo,
            @RequestParam(required = false) Integer porciones,
            @RequestParam long usuarioId,
            @RequestParam String ia,
            @RequestParam(required = false) String titulo) {

        Integer porcionesFinales = porciones != null ? porciones : 1;
        String tituloFinal = (titulo != null && !titulo.isBlank()) ? titulo : prompt;

        try {
            String resultado = switch (ia.toLowerCase()) {
                case "gpt"    -> iaService.generarRecetaConGPT(prompt, tipo, porcionesFinales);
                case "gemini" -> iaService.generarRecetaConGemini(prompt, tipo, porcionesFinales);
                case "claude" -> iaService.generarRecetaConClaude(prompt, tipo, porcionesFinales);
                default -> throw new IllegalArgumentException("IA no valida. Usa: gpt, gemini o claude");
            };

            TipoRecetaDTO tipoDTO = TipoRecetaDTO.valueOf(tipo.toUpperCase());
            RecetaDTO recetaDTO = new RecetaDTO(tituloFinal, prompt, tipoDTO, porcionesFinales, usuarioId);

            switch (ia.toLowerCase()) {
                case "gpt"    -> recetaDTO.setJsonRecetaGPT(resultado);
                case "gemini" -> recetaDTO.setJsonRecetaGemini(resultado);
                case "claude" -> recetaDTO.setJsonRecetaClaude(resultado);
            }
            recetaDTO.setJsonRecetaSeleccionada(resultado);
            recetaDTO.setIaSeleccionada(ia);

            int guardado = recetaSer.create(recetaDTO);
            if (guardado != 0) {
                return new ResponseEntity<>("Error al guardar la receta.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            auditoriaService.registrarAccion(usuarioId, "GENERAR_" + ia.toUpperCase(),
                    "IA", null, "Generado con " + ia + ". Prompt: " + prompt);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("recetaId", recetaDTO.getId());
            respuesta.put("contenido", resultado);
            respuesta.put("ia", ia);

            return new ResponseEntity<>(respuesta, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (PromptVacioException e) {
            return new ResponseEntity<>("El prompt no puede estar vacio.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inesperado al consultar la IA.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Genera contenido con las tres IAs en paralelo y guarda la receta con los resultados obtenidos.
     * Si alguna IA falla, se guarda con las respuestas que sí llegaron.
     * Si las tres fallan, retorna un error 503.
     *
     * @param titulo     título de la receta a crear
     * @param tipo       tipo de contenido, puede ser COCINA o MANUALIDAD
     * @param prompt     descripción de la receta o manualidad a generar
     * @param porciones  número de porciones, opcional, por defecto 1
     * @param usuarioId  identificador del usuario que hace la solicitud
     * @return mapa con los resultados de cada IA y el id de la receta guardada, o mensaje de error
     */
    @PostMapping("/generar-todas")
    public ResponseEntity<?> generarConTodasLasIAs(
            @RequestParam String titulo,
            @RequestParam String tipo,
            @RequestParam String prompt,
            @RequestParam(required = false) Integer porciones,
            @RequestParam long usuarioId) {

        if (prompt == null || prompt.trim().length() < 10) {
            return new ResponseEntity<>("El prompt debe tener al menos 10 caracteres.", HttpStatus.BAD_REQUEST);
        }

        if (!tipo.equalsIgnoreCase("COCINA") && !tipo.equalsIgnoreCase("MANUALIDAD")) {
            return new ResponseEntity<>("El tipo debe ser COCINA o MANUALIDAD.", HttpStatus.BAD_REQUEST);
        }

        if (tipo.equalsIgnoreCase("COCINA") && (porciones == null || porciones <= 0)) {
            return new ResponseEntity<>("Las porciones deben ser mayor a 0.", HttpStatus.BAD_REQUEST);
        }

        final Integer porcionesFinales = porciones != null ? porciones : 1;

        CompletableFuture<String> futuroGPT = CompletableFuture.supplyAsync(() -> {
            try {
                return iaService.generarRecetaConGPT(prompt, tipo, porcionesFinales);
            } catch (Exception e) {
                System.out.println("ERROR GPT: " + e.getMessage());
                return null;
            }
        });

        CompletableFuture<String> futuroGemini = CompletableFuture.supplyAsync(() -> {
            try {
                return iaService.generarRecetaConGemini(prompt, tipo, porcionesFinales);
            } catch (Exception e) {
                System.out.println("ERROR GEMINI: " + e.getMessage());
                return null;
            }
        });

        CompletableFuture<String> futuroClaude = CompletableFuture.supplyAsync(() -> {
            try {
                return iaService.generarRecetaConClaude(prompt, tipo, porcionesFinales);
            } catch (Exception e) {
                System.out.println("ERROR CLAUDE: " + e.getMessage());
                return null;
            }
        });

        try {
            CompletableFuture.allOf(futuroGPT, futuroGemini, futuroClaude).get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            System.out.println("Timeout - tomando lo que llegó");
        } catch (Exception e) {
            System.out.println("Error en allOf: " + e.getMessage());
        }

        String resultadoGPT    = futuroGPT.getNow(null);
        String resultadoGemini = futuroGemini.getNow(null);
        String resultadoClaude = futuroClaude.getNow(null);

        if (resultadoGPT == null && resultadoGemini == null && resultadoClaude == null) {
            return new ResponseEntity<>(
                "Ninguna IA pudo generar la receta. Intenta con un prompt más descriptivo.",
                HttpStatus.SERVICE_UNAVAILABLE
            );
        }

        TipoRecetaDTO tipoDTO = TipoRecetaDTO.valueOf(tipo.toUpperCase());
        RecetaDTO recetaDTO = new RecetaDTO(titulo, prompt, tipoDTO, porcionesFinales, usuarioId);
        recetaDTO.setJsonRecetaGPT(resultadoGPT);
        recetaDTO.setJsonRecetaGemini(resultadoGemini);
        recetaDTO.setJsonRecetaClaude(resultadoClaude);

        if (resultadoGPT != null) {
            recetaDTO.setJsonRecetaSeleccionada(resultadoGPT);
            recetaDTO.setIaSeleccionada("gpt");
        } else if (resultadoGemini != null) {
            recetaDTO.setJsonRecetaSeleccionada(resultadoGemini);
            recetaDTO.setIaSeleccionada("gemini");
        } else {
            recetaDTO.setJsonRecetaSeleccionada(resultadoClaude);
            recetaDTO.setIaSeleccionada("claude");
        }

        int resultadoGuardado = recetaSer.create(recetaDTO);
        if (resultadoGuardado != 0) {
            return new ResponseEntity<>("Error al guardar la receta.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_TODAS_IAS", "Receta",
                recetaDTO.getId(), "Receta generada con las 3 IAs. Prompt: " + prompt);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("recetaId",      recetaDTO.getId());
        respuesta.put("prompt",        prompt);
        respuesta.put("recetaGPT",     resultadoGPT);
        respuesta.put("recetaGemini",  resultadoGemini);
        respuesta.put("recetaClaude",  resultadoClaude);
        respuesta.put("exitosaGPT",    resultadoGPT != null);
        respuesta.put("exitosaGemini", resultadoGemini != null);
        respuesta.put("exitosaClaude", resultadoClaude != null);

        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    /**
     * Genera una narración en texto para una receta existente usando GPT.
     *
     * @param textoReceta texto de la receta sobre la que se va a generar la narración
     * @param usuarioId   identificador del usuario que hace la solicitud
     * @return la narración generada o un mensaje de error según el caso
     */
    @PostMapping("/narracion")
    public ResponseEntity<?> generarNarracion(@RequestParam String textoReceta, @RequestParam long usuarioId) {
        try {
            String narracion = iaService.generarNarracionConIA(textoReceta);
            auditoriaService.registrarAccion(usuarioId, "GENERAR_NARRACION", "IA", null,
                    "Narración generada para receta.");
            return new ResponseEntity<>(narracion, HttpStatus.OK);
        } catch (CampoVacioException e) {
            return new ResponseEntity<>("El texto de la receta no puede estar vacío.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inesperado al generar la narración.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}