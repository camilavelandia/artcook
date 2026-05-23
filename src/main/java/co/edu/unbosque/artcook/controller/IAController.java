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
     * @param prompt     descripción de la receta o manualidad
     * @param tipo       tipo de contenido: {@code COCINA} o {@code MANUALIDAD}
     * @param usuarioId  ID del usuario que realiza la petición
     * @param porciones  número de porciones deseado; por defecto 1
     * @return contenido generado por GPT, o mensaje de error según el caso
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
     * @param prompt     descripción de la receta o manualidad
     * @param tipo       tipo de contenido: {@code COCINA} o {@code MANUALIDAD}
     * @param usuarioId  ID del usuario que realiza la petición
     * @param porciones  número de porciones deseado; por defecto 1
     * @return contenido generado por Gemini, o mensaje de error según el caso
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
     * @param prompt     descripción de la receta o manualidad
     * @param tipo       tipo de contenido: {@code COCINA} o {@code MANUALIDAD}
     * @param usuarioId  ID del usuario que realiza la petición
     * @param porciones  número de porciones deseado; por defecto 1
     * @return contenido generado por Claude, o mensaje de error según el caso
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
     * Genera contenido con la IA seleccionada por el usuario, persiste la receta
     * en base de datos con {@code jsonRecetaSeleccionada} e {@code iaSeleccionada}
     * ya establecidos, y retorna el contenido junto con el ID de la receta guardada
     * para permitir la descarga inmediata del PDF.
     *
     * @param prompt          descripción de la receta o manualidad
     * @param tipo            tipo de contenido: {@code COCINA} o {@code MANUALIDAD}
     * @param porciones       número de porciones; por defecto 1 si no se envía
     * @param usuarioId       ID del usuario que realiza la petición
     * @param ia              IA a usar: {@code gpt}, {@code gemini} o {@code claude}
     * @param titulo          título de la receta; si no se envía se usa el prompt
     * @return objeto con {@code recetaId}, {@code contenido} e {@code ia} usada,
     *         o mensaje de error según el caso
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
     * Genera contenido con las tres IAs en paralelo, persiste la receta en base
     * de datos y retorna los tres contenidos junto con el ID de la receta guardada.
     *
     * <p>Si alguna IA no responde dentro del timeout de 30 segundos, su resultado
     * se marca como no disponible y el resto se guarda normalmente. La primera IA
     * disponible (en orden GPT → Gemini → Claude) se establece como seleccionada
     * por defecto, de modo que la descarga del PDF siempre tenga contenido válido.</p>
     *
     * @param titulo     título de la receta o manualidad
     * @param tipo       tipo de contenido: {@code COCINA} o {@code MANUALIDAD}
     * @param prompt     descripción de la receta o manualidad
     * @param porciones  número de porciones; requerido y mayor a 0 para COCINA
     * @param usuarioId  ID del usuario que realiza la petición
     * @return objeto con {@code recetaId} y los contenidos de las tres IAs,
     *         junto con flags de éxito por IA, o mensaje de error según el caso
     */
    @PostMapping("/generar-todas")
    public ResponseEntity<?> generarConTodasLasIAs(
            @RequestParam String titulo,
            @RequestParam String tipo,
            @RequestParam String prompt,
            @RequestParam(required = false) Integer porciones,
            @RequestParam long usuarioId) {

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
                return null;
            }
        });

        CompletableFuture<String> futuroGemini = CompletableFuture.supplyAsync(() -> {
            try {
                return iaService.generarRecetaConGemini(prompt, tipo, porcionesFinales);
            } catch (Exception e) {
                return null;
            }
        });

        CompletableFuture<String> futuroClaude = CompletableFuture.supplyAsync(() -> {
            try {
                return iaService.generarRecetaConClaude(prompt, tipo, porcionesFinales);
            } catch (Exception e) {
                return null;
            }
        });

        try {
            CompletableFuture.allOf(futuroGPT, futuroGemini, futuroClaude).get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // se toma lo que llegó dentro del tiempo
        } catch (Exception e) {
            // se continúa con los resultados parciales
        }

        String resultadoGPT    = futuroGPT.getNow("OpenAI no disponible.");
        String resultadoGemini = futuroGemini.getNow("Gemini no disponible.");
        String resultadoClaude = futuroClaude.getNow("Claude no disponible.");

        TipoRecetaDTO tipoDTO = TipoRecetaDTO.valueOf(tipo.toUpperCase());
        RecetaDTO recetaDTO = new RecetaDTO(titulo, prompt, tipoDTO, porcionesFinales, usuarioId);
        recetaDTO.setJsonRecetaGPT(resultadoGPT);
        recetaDTO.setJsonRecetaGemini(resultadoGemini);
        recetaDTO.setJsonRecetaClaude(resultadoClaude);

        if (resultadoGPT != null && !resultadoGPT.equals("OpenAI no disponible.")) {
            recetaDTO.setJsonRecetaSeleccionada(resultadoGPT);
            recetaDTO.setIaSeleccionada("gpt");
        } else if (resultadoGemini != null && !resultadoGemini.equals("Gemini no disponible.")) {
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

        auditoriaService.registrarAccion(usuarioId, "GENERAR_RECETA_TODAS_IAS", "Receta", recetaDTO.getId(),
                "Receta generada con las 3 IAs. Prompt: " + prompt);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("recetaId",      recetaDTO.getId());
        respuesta.put("prompt",        prompt);
        respuesta.put("recetaGPT",     resultadoGPT);
        respuesta.put("recetaGemini",  resultadoGemini);
        respuesta.put("recetaClaude",  resultadoClaude);
        respuesta.put("exitosaGPT",    resultadoGPT    != null && !resultadoGPT.equals("OpenAI no disponible."));
        respuesta.put("exitosaGemini", resultadoGemini != null && !resultadoGemini.equals("Gemini no disponible."));
        respuesta.put("exitosaClaude", resultadoClaude != null && !resultadoClaude.equals("Claude no disponible."));

        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    /**
     * Genera una narración en texto para una receta existente usando GPT.
     *
     * @param textoReceta contenido de la receta sobre la que se genera la narración
     * @param usuarioId   ID del usuario que realiza la petición
     * @return narración generada, o mensaje de error según el caso
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