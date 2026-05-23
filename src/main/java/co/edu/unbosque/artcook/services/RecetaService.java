package co.edu.unbosque.artcook.services;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import co.edu.unbosque.artcook.dto.RecetaDTO;
import co.edu.unbosque.artcook.entity.Receta;
import co.edu.unbosque.artcook.entity.TipoReceta;
import co.edu.unbosque.artcook.exception.LanzadorExcepciones;
import co.edu.unbosque.artcook.exception.PromptVacioException;
import co.edu.unbosque.artcook.exception.RegistroNoEncontradoException;
import co.edu.unbosque.artcook.exception.TipoRecetaException;
import co.edu.unbosque.artcook.repository.RecetaRepository;

/**
 * Servicio que gestiona la creación, consulta, actualización, eliminación
 * y generación de PDF de recetas y manualidades.
 *
 * <p>La invocación a las IAs es responsabilidad del {@code IAController};
 * este servicio solo persiste y opera sobre los datos ya generados.</p>
 */
@Service
public class RecetaService implements CRUDOperation<RecetaDTO> {

    @Autowired
    private RecetaRepository recetaRepo;

    @Autowired
    private ModelMapper mapper;

    /**
     * Constructor por defecto requerido por Spring.
     */
    public RecetaService() {
    }

    /**
     * Persiste una nueva receta en la base de datos con el contenido de las IAs
     * ya generado. El {@code IAController} es quien invoca las IAs; este método
     * solo guarda el resultado.
     *
     * @param data DTO con el prompt, tipo, usuario y contenido de las IAs
     * @return {@code 0} si se guardó correctamente, {@code 1} si el prompt o
     *         tipo es inválido, {@code 2} en caso de error inesperado
     */
    @Override
    public int create(RecetaDTO data) {
        try {
            LanzadorExcepciones.validarPrompt(data.getPromptOriginal());
            LanzadorExcepciones.validarTipoReceta(data.getTipoReceta().toString());

            if (data.getTipoReceta().toString().equalsIgnoreCase("COCINA")) {
                LanzadorExcepciones.validarPorciones(data.getPorciones());
            }

            Receta receta = new Receta(
                data.getTitulo(),
                data.getPromptOriginal(),
                TipoReceta.valueOf(data.getTipoReceta().toString()),
                data.getUsuarioId()
            );
            receta.setPorciones(data.getPorciones());
            receta.setJsonRecetaGPT(data.getJsonRecetaGPT());
            receta.setJsonRecetaGemini(data.getJsonRecetaGemini());
            receta.setJsonRecetaClaude(data.getJsonRecetaClaude());
            receta.setJsonRecetaSeleccionada(data.getJsonRecetaSeleccionada());
            receta.setIaSeleccionada(data.getIaSeleccionada());
            receta.setFechaCreacion(LocalDateTime.now());
            receta.setActiva(true);

            recetaRepo.save(receta);
            data.setId(receta.getId());
            return 0;

        } catch (PromptVacioException | TipoRecetaException e) {
            return 1;
        } catch (Exception e) {
            return 2;
        }
    }

    /**
     * Obtiene todas las recetas registradas en el sistema.
     *
     * @return lista de DTOs de todas las recetas
     */
    @Override
    public List<RecetaDTO> getAll() {
        List<Receta> entityList = (List<Receta>) recetaRepo.findAll();
        List<RecetaDTO> dtoList = new ArrayList<>();
        entityList.forEach(entidad -> dtoList.add(mapper.map(entidad, RecetaDTO.class)));
        return dtoList;
    }

    /**
     * Elimina una receta por su ID.
     *
     * @param id identificador de la receta a eliminar
     * @return {@code 0} si se eliminó correctamente, {@code 1} si no se encontró
     * @throws RegistroNoEncontradoException si la receta no existe
     */
    @Override
    public int deleteByID(Long id) {
        try {
            LanzadorExcepciones.validarId(id);
            if (recetaRepo.existsById(id)) {
                recetaRepo.deleteById(id);
                return 0;
            }
            throw new RegistroNoEncontradoException("La receta con id " + id + " no existe.");
        } catch (RegistroNoEncontradoException e) {
            return 1;
        }
    }

    /**
     * Actualiza campos específicos de una receta existente. Solo se actualizan
     * los campos que no sean {@code null} en el DTO recibido.
     *
     * @param id   identificador de la receta a actualizar
     * @param data DTO con los nuevos valores; los campos {@code null} se ignoran
     * @return {@code 0} si se actualizó correctamente, {@code 1} si no se encontró
     */
    @Override
    public int updateByID(Long id, RecetaDTO data) {
        try {
            LanzadorExcepciones.validarId(id);
            Optional<Receta> encontrada = recetaRepo.findById(id);
            if (encontrada.isPresent()) {
                Receta temp = encontrada.get();
                if (data.getTitulo() != null) temp.setTitulo(data.getTitulo());
                if (data.getJsonRecetaSeleccionada() != null) temp.setJsonRecetaSeleccionada(data.getJsonRecetaSeleccionada());
                if (data.getIaSeleccionada() != null) temp.setIaSeleccionada(data.getIaSeleccionada());
                if (data.getUrlVideo() != null) temp.setUrlVideo(data.getUrlVideo());
                if (data.getUrlAudio() != null) temp.setUrlAudio(data.getUrlAudio());
                if (data.getGuionNaracion() != null) temp.setGuionNaracion(data.getGuionNaracion());
                temp.setFechaActualizacion(LocalDateTime.now());
                recetaRepo.save(temp);
                return 0;
            }
            throw new RegistroNoEncontradoException("La receta con id " + id + " no existe.");
        } catch (RegistroNoEncontradoException e) {
            return 1;
        }
    }

    /**
     * Retorna el total de recetas registradas en el sistema.
     *
     * @return número total de recetas
     */
    @Override
    public long count() {
        return recetaRepo.count();
    }

    /**
     * Verifica si existe una receta con el ID indicado.
     *
     * @param id identificador a verificar
     * @return {@code true} si existe, {@code false} en caso contrario
     */
    @Override
    public boolean exist(Long id) {
        return recetaRepo.existsById(id);
    }

    /**
     * Obtiene una receta por su ID.
     *
     * @param id identificador de la receta
     * @return DTO con los datos de la receta
     * @throws RegistroNoEncontradoException si la receta no existe
     */
    public RecetaDTO obtenerPorId(Long id) throws RegistroNoEncontradoException {
        LanzadorExcepciones.validarId(id);
        Optional<Receta> receta = recetaRepo.findById(id);
        if (!receta.isPresent()) {
            throw new RegistroNoEncontradoException("La receta con id " + id + " no existe.");
        }
        return mapper.map(receta.get(), RecetaDTO.class);
    }

    /**
     * Obtiene todas las recetas asociadas a un usuario específico.
     *
     * @param usuarioId ID del usuario
     * @return lista de DTOs de recetas del usuario
     * @throws RegistroNoEncontradoException si el ID de usuario es inválido
     */
    public List<RecetaDTO> obtenerPorUsuario(Long usuarioId) throws RegistroNoEncontradoException {
        LanzadorExcepciones.validarId(usuarioId);
        return recetaRepo.findByUsuarioId(usuarioId).stream()
                .map(r -> mapper.map(r, RecetaDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las recetas filtradas por tipo.
     *
     * @param tipo tipo de receta como String: {@code COCINA} o {@code MANUALIDAD}
     * @return lista de DTOs filtrados por tipo
     * @throws TipoRecetaException si el tipo no es válido
     */
    public List<RecetaDTO> obtenerPorTipo(String tipo) throws TipoRecetaException {
        LanzadorExcepciones.validarTipoReceta(tipo);
        TipoReceta tipoReceta = TipoReceta.valueOf(tipo.toUpperCase());
        return recetaRepo.findByTipoReceta(tipoReceta).stream()
                .map(r -> mapper.map(r, RecetaDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Permite al usuario seleccionar cuál de las tres respuestas de IA prefiere
     * como contenido final de la receta. Actualiza {@code jsonRecetaSeleccionada}
     * e {@code iaSeleccionada} en la base de datos.
     *
     * @param recetaId ID de la receta
     * @param ia       nombre de la IA seleccionada: {@code gpt}, {@code gemini} o {@code claude}
     * @return {@code 0} éxito, {@code 1} receta no encontrada,
     *         {@code 2} la IA seleccionada no tiene contenido, {@code 3} error inesperado
     */
    public int seleccionarReceta(Long recetaId, String ia) {
        try {
            LanzadorExcepciones.validarId(recetaId);
            Optional<Receta> opt = recetaRepo.findById(recetaId);
            if (!opt.isPresent()) return 1;

            Receta receta = opt.get();
            String jsonSeleccionado = switch (ia.toLowerCase()) {
                case "gpt"    -> receta.getJsonRecetaGPT();
                case "gemini" -> receta.getJsonRecetaGemini();
                case "claude" -> receta.getJsonRecetaClaude();
                default -> null;
            };

            if (jsonSeleccionado == null) return 2;

            receta.setJsonRecetaSeleccionada(jsonSeleccionado);
            receta.setIaSeleccionada(ia);
            receta.setFechaActualizacion(LocalDateTime.now());
            recetaRepo.save(receta);
            return 0;

        } catch (RegistroNoEncontradoException e) {
            return 1;
        } catch (Exception e) {
            return 3;
        }
    }

    /**
     * Desactiva una receta sin eliminarla de la base de datos.
     *
     * @param id identificador de la receta
     * @return {@code 0} si se desactivó correctamente
     * @throws RegistroNoEncontradoException si la receta no existe
     */
    public int desactivarReceta(Long id) throws RegistroNoEncontradoException {
        LanzadorExcepciones.validarId(id);
        Optional<Receta> receta = recetaRepo.findById(id);
        if (!receta.isPresent()) {
            throw new RegistroNoEncontradoException("La receta con id " + id + " no existe.");
        }
        Receta encontrada = receta.get();
        encontrada.setActiva(false);
        encontrada.setFechaActualizacion(LocalDateTime.now());
        recetaRepo.save(encontrada);
        return 0;
    }

    /**
     * Genera un PDF descargable con el contenido legible de la receta indicada.
     *
     * @param idReceta identificador de la receta
     * @return arreglo de bytes del PDF generado
     * @throws RegistroNoEncontradoException si la receta no existe
     */
    public byte[] generarPdfReceta(Long idReceta) throws RegistroNoEncontradoException {
        LanzadorExcepciones.validarId(idReceta);
        Optional<Receta> receta = recetaRepo.findById(idReceta);
        if (!receta.isPresent()) {
            throw new RegistroNoEncontradoException("La receta con id " + idReceta + " no existe.");
        }
        return generarPdfDesdeReceta(receta.get());
    }

    /**
     * Construye el PDF a partir del objeto {@link Receta}.
     *
     * <p>Si {@code jsonRecetaSeleccionada} es {@code null} o está vacío, se aplica
     * un fallback en el orden: GPT → Gemini → Claude. El contenido JSON se parsea
     * y formatea en texto legible antes de escribirlo en el documento. Las porciones
     * del encabezado solo se muestran cuando el tipo es {@code COCINA}.</p>
     *
     * @param receta entidad con los datos de la receta
     * @return arreglo de bytes del PDF; arreglo vacío si ocurre un error
     */
    public byte[] generarPdfDesdeReceta(Receta receta) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            String iaUsada = receta.getIaSeleccionada();
            String contenidoReceta = receta.getJsonRecetaSeleccionada();

            if (contenidoReceta == null || contenidoReceta.isBlank()) {
                if (receta.getJsonRecetaGPT() != null && !receta.getJsonRecetaGPT().isBlank()) {
                    contenidoReceta = receta.getJsonRecetaGPT();
                    iaUsada = "gpt";
                } else if (receta.getJsonRecetaGemini() != null && !receta.getJsonRecetaGemini().isBlank()) {
                    contenidoReceta = receta.getJsonRecetaGemini();
                    iaUsada = "gemini";
                } else if (receta.getJsonRecetaClaude() != null && !receta.getJsonRecetaClaude().isBlank()) {
                    contenidoReceta = receta.getJsonRecetaClaude();
                    iaUsada = "claude";
                }
            }

            document.add(new Paragraph("ARTCOOK")
                    .setBold()
                    .setFontSize(22)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY));

            document.add(new Paragraph(" "));

            document.add(new Paragraph(receta.getTitulo() != null ? receta.getTitulo() : "Sin titulo")
                    .setBold()
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(" "));

            document.add(new Paragraph("Tipo: " + receta.getTipoReceta()).setFontSize(12));

            boolean esCocina = receta.getTipoReceta() != null
                    && receta.getTipoReceta().toString().equalsIgnoreCase("COCINA");

            if (esCocina && receta.getPorciones() != null && receta.getPorciones() > 0) {
                document.add(new Paragraph("Porciones: " + receta.getPorciones()).setFontSize(12));
            }

            document.add(new Paragraph("IA utilizada: " + (iaUsada != null ? iaUsada : "No especificada"))
                    .setFontSize(12));
            document.add(new Paragraph("Fecha de creacion: " + receta.getFechaCreacion()).setFontSize(12));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Receta:")
                    .setBold()
                    .setFontSize(14)
                    .setFontColor(ColorConstants.DARK_GRAY));
            document.add(new Paragraph(" "));

            String contenidoFormateado = formatearContenidoParaPdf(
                    contenidoReceta != null ? contenidoReceta : "Sin contenido");
            document.add(new Paragraph(contenidoFormateado).setFontSize(11));

            if (receta.getGuionNaracion() != null) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Narracion:")
                        .setBold()
                        .setFontSize(14)
                        .setFontColor(ColorConstants.DARK_GRAY));
                document.add(new Paragraph(receta.getGuionNaracion()).setFontSize(11).setItalic());
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * Parsea el JSON de la receta o manualidad y lo convierte en texto
     * legible con secciones claramente separadas.
     *
     * <p>Soporta tanto el formato de receta ({@code ingredientes}, {@code porciones},
     * {@code tiempo_preparacion}) como el de manualidad ({@code materiales},
     * {@code tiempo_estimado}). Si el JSON no puede parsearse, retorna el
     * contenido original como fallback.</p>
     *
     * @param jsonContenido cadena JSON generada por la IA
     * @return texto formateado listo para incluir en el PDF
     */
    private String formatearContenidoParaPdf(String jsonContenido) {
        try {
            String limpio = jsonContenido
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(limpio);

            StringBuilder sb = new StringBuilder();

            if (root.has("nombre")) {
                sb.append("Nombre: ").append(root.get("nombre").asText()).append("\n\n");
            }
            if (root.has("porciones")) {
                sb.append("Porciones: ").append(root.get("porciones").asText()).append("\n");
            }
            if (root.has("tiempo_preparacion")) {
                sb.append("Tiempo de preparacion: ").append(root.get("tiempo_preparacion").asText()).append("\n");
            }
            if (root.has("tiempo_estimado")) {
                sb.append("Tiempo estimado: ").append(root.get("tiempo_estimado").asText()).append("\n");
            }
            if (root.has("dificultad")) {
                sb.append("Dificultad: ").append(root.get("dificultad").asText()).append("\n");
            }

            sb.append("\n");

            if (root.has("ingredientes")) {
                sb.append("INGREDIENTES:\n");
                for (JsonNode ing : root.get("ingredientes")) {
                    sb.append("  - ")
                      .append(ing.has("nombre") ? ing.get("nombre").asText() : "")
                      .append(": ")
                      .append(ing.has("cantidad") ? ing.get("cantidad").asText() : "")
                      .append("\n");
                }
                sb.append("\n");
            }

            if (root.has("materiales")) {
                sb.append("MATERIALES:\n");
                for (JsonNode mat : root.get("materiales")) {
                    sb.append("  - ")
                      .append(mat.has("nombre") ? mat.get("nombre").asText() : "")
                      .append(": ")
                      .append(mat.has("cantidad") ? mat.get("cantidad").asText() : "")
                      .append("\n");
                }
                sb.append("\n");
            }

            if (root.has("pasos")) {
                sb.append("PASOS:\n");
                int numero = 1;
                for (JsonNode paso : root.get("pasos")) {
                    sb.append(numero++).append(". ").append(paso.asText()).append("\n");
                }
            }

            return sb.toString();

        } catch (Exception e) {
            return jsonContenido;
        }
    }
}