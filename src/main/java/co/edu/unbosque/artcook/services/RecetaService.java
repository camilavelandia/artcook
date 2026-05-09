package co.edu.unbosque.artcook.services;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

@Service
public class RecetaService implements CRUDOperation<RecetaDTO> {

	@Autowired
	private RecetaRepository recetaRepo;

	@Autowired
	private ModelMapper mapper;

	@Override
	public int create(RecetaDTO newData) {
		try {
			LanzadorExcepciones.validarPrompt(newData.getPromptOriginal());
			LanzadorExcepciones.validarTipoReceta(newData.getTipoReceta().toString());

			if (newData.getTipoReceta().toString().equalsIgnoreCase("COCINA")) {
				LanzadorExcepciones.validarPorciones(newData.getPorciones());
			}

			Receta receta = new Receta(
				newData.getTitulo(),
				newData.getPromptOriginal(),
				TipoReceta.valueOf(newData.getTipoReceta().toString()),
				newData.getUsuarioId()
			);
			receta.setPorciones(newData.getPorciones());
			receta.setJsonRecetaGPT(newData.getJsonRecetaGPT());
			receta.setJsonRecetaGemini(newData.getJsonRecetaGemini());
			receta.setJsonRecetaClaude(newData.getJsonRecetaClaude());
			receta.setFechaCreacion(LocalDateTime.now());
			receta.setActiva(true);

			recetaRepo.save(receta);

			newData.setId(receta.getId());
			return 0;
		} catch (PromptVacioException | TipoRecetaException e) {
			return 1;
		} catch (Exception e) {
			return 2;
		}
	}

	@Override
	public List<RecetaDTO> getAll() {
		List<Receta> entityList = (List<Receta>) recetaRepo.findAll();
		List<RecetaDTO> dtoList = new ArrayList<>();
		entityList.forEach((entidad) -> {
			RecetaDTO dto = mapper.map(entidad, RecetaDTO.class);
			dtoList.add(dto);
		});
		return dtoList;
	}

	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.validarId(id);
			if (recetaRepo.existsById(id)) {
				recetaRepo.deleteById(id);
				return 0;
			}
			throw new RegistroNoEncontradoException("La receta con id " + id + " no existe");
		} catch (RegistroNoEncontradoException e) {
			return 1;
		}
	}

	@Override
	public int updateById(Long id, RecetaDTO newData) {
		try {
			LanzadorExcepciones.validarId(id);
			Optional<Receta> encontrada = recetaRepo.findById(id);
			if (encontrada.isPresent()) {
				Receta temp = encontrada.get();
				if (newData.getTitulo() != null) temp.setTitulo(newData.getTitulo());
				if (newData.getJsonRecetaSeleccionada() != null) temp.setJsonRecetaSeleccionada(newData.getJsonRecetaSeleccionada());
				if (newData.getIaSeleccionada() != null) temp.setIaSeleccionada(newData.getIaSeleccionada());
				if (newData.getUrlVideo() != null) temp.setUrlVideo(newData.getUrlVideo());
				if (newData.getUrlAudio() != null) temp.setUrlAudio(newData.getUrlAudio());
				if (newData.getGuionNaracion() != null) temp.setGuionNaracion(newData.getGuionNaracion());
				temp.setFechaActualizacion(LocalDateTime.now());
				recetaRepo.save(temp);
				return 0;
			}
			throw new RegistroNoEncontradoException("La receta con id " + id + " no existe");
		} catch (RegistroNoEncontradoException e) {
			return 1;
		}
	}

	@Override
	public long count() {
		return recetaRepo.count();
	}

	@Override
	public boolean exist(Long id) {
		return recetaRepo.existsById(id);
	}

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

	public List<RecetaDTO> obtenerPorUsuario(Long usuarioId) throws RegistroNoEncontradoException {
		try {
			LanzadorExcepciones.validarId(usuarioId);
			List<Receta> recetas = recetaRepo.findByUsuarioId(usuarioId);
			List<RecetaDTO> dtoList = new ArrayList<>();
			recetas.forEach((receta) -> {
				RecetaDTO dto = mapper.map(receta, RecetaDTO.class);
				dtoList.add(dto);
			});
			return dtoList;
		} catch (RegistroNoEncontradoException e) {
			throw e;
		}
	}

	public List<RecetaDTO> obtenerPorTipo(String tipo) throws TipoRecetaException {
		try {
			LanzadorExcepciones.validarTipoReceta(tipo);
			TipoReceta tipoReceta = TipoReceta.valueOf(tipo.toUpperCase());
			List<Receta> recetas = recetaRepo.findByTipoReceta(tipoReceta);
			List<RecetaDTO> dtoList = new ArrayList<>();
			recetas.forEach((receta) -> {
				RecetaDTO dto = mapper.map(receta, RecetaDTO.class);
				dtoList.add(dto);
			});
			return dtoList;
		} catch (TipoRecetaException e) {
			throw e;
		}
	}

	public RecetaDTO obtenerPorId(Long id) throws RegistroNoEncontradoException {
		try {
			LanzadorExcepciones.validarId(id);
			Optional<Receta> receta = recetaRepo.findById(id);
			if (!receta.isPresent()) {
				throw new RegistroNoEncontradoException("La receta con id " + id + " no existe");
			}
			return mapper.map(receta.get(), RecetaDTO.class);
		} catch (RegistroNoEncontradoException e) {
			throw e;
		}
	}

	public int desactivarReceta(Long id) throws RegistroNoEncontradoException {
		try {
			LanzadorExcepciones.validarId(id);
			Optional<Receta> receta = recetaRepo.findById(id);
			if (!receta.isPresent()) {
				throw new RegistroNoEncontradoException("La receta con id " + id + " no existe");
			}
			Receta encontrada = receta.get();
			encontrada.setActiva(false);
			encontrada.setFechaActualizacion(LocalDateTime.now());
			recetaRepo.save(encontrada);
			return 0;
		} catch (RegistroNoEncontradoException e) {
			return 1;
		}
	}

	public byte[] generarPdfReceta(Long idReceta) throws RegistroNoEncontradoException {
		try {
			LanzadorExcepciones.validarId(idReceta);
			Optional<Receta> receta = recetaRepo.findById(idReceta);
			if (!receta.isPresent()) {
				throw new RegistroNoEncontradoException("La receta con id " + idReceta + " no existe");
			}
			return generarPdfDesdeJson(receta.get());
		} catch (RegistroNoEncontradoException e) {
			throw e;
		}
	}

	public byte[] generarPdfDesdeJson(Receta receta) {
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

	        document.add(new Paragraph("Tipo: " + receta.getTipoReceta())
	                .setFontSize(12));

	        if (receta.getPorciones() != null && receta.getPorciones() > 0) {
	            document.add(new Paragraph("Porciones: " + receta.getPorciones())
	                    .setFontSize(12));
	        }

	        document.add(new Paragraph("IA utilizada: " + receta.getIaSeleccionada())
	                .setFontSize(12));

	        document.add(new Paragraph("Fecha de creacion: " + receta.getFechaCreacion())
	                .setFontSize(12));

	        document.add(new Paragraph(" "));

	        document.add(new Paragraph("Receta:")
	                .setBold()
	                .setFontSize(14)
	                .setFontColor(ColorConstants.DARK_GRAY));

	        document.add(new Paragraph(" "));

	        document.add(new Paragraph(receta.getJsonRecetaSeleccionada() != null
	                ? receta.getJsonRecetaSeleccionada()
	                : "Sin contenido")
	                .setFontSize(11));

	        if (receta.getGuionNaracion() != null) {
	            document.add(new Paragraph(" "));
	            document.add(new Paragraph("Narracion:")
	                    .setBold()
	                    .setFontSize(14)
	                    .setFontColor(ColorConstants.DARK_GRAY));
	            document.add(new Paragraph(receta.getGuionNaracion())
	                    .setFontSize(11)
	                    .setItalic());
	        }

	        document.close();
	        return baos.toByteArray();

	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
}