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
 * Servicio que gestiona la generación, almacenamiento y consulta de recetas.
 * Coordina las llamadas a las tres IAs y guarda los resultados.
 */
@Service
public class RecetaService implements CRUDOperation<RecetaDTO> {

    @Autowired
    private RecetaRepository recetaRepo;

    @Autowired
    private ModelMapper mapper;

    /**
     * Constructor por defecto.
     */
    public RecetaService() {
    }

    /**
     * Guarda una nueva receta en la base de datos con el contenido de las IAs ya generado.
     * El IAController es el que llama a las IAs; este servicio solo persiste.
     *
     * @param data DTO con el prompt, tipo, usuario y contenido de las IAs
     * @return código de estado: 0 éxito, otro número indica tipo de error
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
     * @return lista de DTOs de recetas
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
     * @param id identificador de la receta
     * @return código de estado: 0 éxito, 1 no encontrada
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
     * Actualiza campos específicos de una receta existente.
     *
     * @param id   identificador de la receta
     * @param data nuevos datos a actualizar
     * @return código de estado: 0 éxito, 1 no encontrada
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
     * Obtiene el total de recetas generadas.
     *
     * @return número total de recetas
     */
    @Override
    public long count() {
        return recetaRepo.count();
    }

    /**
     * Verifica si existe una receta por su ID.
     *
     * @param id identificador
     * @return true si existe, false en caso contrario
     */
    @Override
    public boolean exist(Long id) {
        return recetaRepo.existsById(id);
    }

    /**
     * Obtiene una receta por su ID.
     *
     * @param id identificador de la receta
     * @return DTO de la receta
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
     * Obtiene todas las recetas de un usuario específico.
     *
     * @param usuarioId ID del usuario
     * @return lista de DTOs de recetas del usuario
     * @throws RegistroNoEncontradoException si el ID es inválido
     */
    public List<RecetaDTO> obtenerPorUsuario(Long usuarioId) throws RegistroNoEncontradoException {
        LanzadorExcepciones.validarId(usuarioId);
        return recetaRepo.findByUsuarioId(usuarioId).stream()
                .map(r -> mapper.map(r, RecetaDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las recetas de un tipo específico.
     *
     * @param tipo tipo de receta como String (COCINA o MANUALIDAD)
     * @return lista de DTOs filtrados por tipo
     * @throws TipoRecetaException si el tipo es inválido
     */
    public List<RecetaDTO> obtenerPorTipo(String tipo) throws TipoRecetaException {
        LanzadorExcepciones.validarTipoReceta(tipo);
        TipoReceta tipoReceta = TipoReceta.valueOf(tipo.toUpperCase());
        return recetaRepo.findByTipoReceta(tipoReceta).stream()
                .map(r -> mapper.map(r, RecetaDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Permite al usuario seleccionar cuál de las tres respuestas de IA prefiere.
     *
     * @param recetaId ID de la receta
     * @param ia       nombre de la IA seleccionada (gpt, gemini, claude)
     * @return 0 éxito, 1 no encontrada, 2 IA sin contenido
     */
    public int seleccionarReceta(Long recetaId, String ia) {
        try {
            LanzadorExcepciones.validarId(recetaId);
            Optional<Receta> opt = recetaRepo.findById(recetaId);
            if (!opt.isPresent()) return 1;

            Receta receta = opt.get();
            String jsonSeleccionado = switch (ia.toLowerCase()) {
                case "gpt" -> receta.getJsonRecetaGPT();
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
     * @return 0 éxito, 1 no encontrada
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
     * Genera un PDF descargable con el contenido de la receta seleccionada.
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
     * Construye el PDF a partir del objeto Receta.
     *
     * @param receta entidad con los datos de la receta
     * @return arreglo de bytes del PDF
     */
    public byte[] generarPdfDesdeReceta(Receta receta) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

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

            if (receta.getPorciones() != null && receta.getPorciones() > 0) {
                document.add(new Paragraph("Porciones: " + receta.getPorciones()).setFontSize(12));
            }

            document.add(new Paragraph("IA utilizada: " + receta.getIaSeleccionada()).setFontSize(12));
            document.add(new Paragraph("Fecha de creacion: " + receta.getFechaCreacion()).setFontSize(12));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Receta:")
                    .setBold()
                    .setFontSize(14)
                    .setFontColor(ColorConstants.DARK_GRAY));
            document.add(new Paragraph(" "));

            document.add(new Paragraph(receta.getJsonRecetaSeleccionada() != null
                    ? receta.getJsonRecetaSeleccionada()
                    : "Sin contenido").setFontSize(11));

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
}
