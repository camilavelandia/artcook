package co.edu.unbosque.artcook.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI (Swagger) para documentar la API REST de ArtCook.
 * Define la información general de la API, esquema de seguridad JWT
 * y componentes reutilizables para la documentación.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Define los componentes y la información general de la documentación OpenAPI.
     * Incluye descripción de endpoints, flujo de autenticación JWT y roles de usuario.
     *
     * @return configuración personalizada de OpenAPI para ArtCook
     */
    @Bean
    public OpenAPI customOpenAPI() {

        String mainDescription =
            "<h2>API REST de ArtCook</h2>"
            + "<p>Aplicativo web para generar recetas de cocina y manualidades usando Inteligencia Artificial.</p>"
            + "<h3>Funcionalidades principales:</h3>"
            + "<ul>"
            + "<li><strong>Recetas de cocina</strong>: Genera recetas detalladas con ingredientes y pasos usando GPT, Gemini y Claude.</li>"
            + "<li><strong>Manualidades</strong>: Genera instrucciones detalladas para proyectos de manualidades.</li>"
            + "<li><strong>Comparación de IAs</strong>: Compara las respuestas de las tres IAs y elige la mejor.</li>"
            + "<li><strong>Descarga de recetas</strong>: Exporta la receta seleccionada en formato PDF.</li>"
            + "</ul>"
            + "<h3>Flujo básico de uso:</h3>"
            + "<ol>"
            + "<li>Regístrate con <code>POST /usuario/registrar</code></li>"
            + "<li>Verifica tu correo con el enlace recibido</li>"
            + "<li>Inicia sesión con <code>POST /usuario/login</code> para obtener tu token JWT</li>"
            + "<li>Incluye el token en las peticiones: <code>Authorization: Bearer tu_token</code></li>"
            + "<li>Genera recetas con <code>POST /ia/generar-todas</code></li>"
            + "<li>Selecciona la mejor respuesta con <code>PUT /receta/seleccionar</code></li>"
            + "<li>Descarga el PDF con <code>GET /receta/generarPdf</code></li>"
            + "</ol>"
            + "<h3>Roles de usuario:</h3>"
            + "<ul>"
            + "<li><strong>USER</strong>: Puede generar y gestionar sus propias recetas.</li>"
            + "<li><strong>ADMIN</strong>: Puede ver todas las recetas, usuarios y registros de auditoría.</li>"
            + "</ul>"
            + "<h3>Códigos de estado HTTP:</h3>"
            + "<ul>"
            + "<li><strong>200/201</strong>: Operación exitosa</li>"
            + "<li><strong>400</strong>: Error en la solicitud (datos incorrectos)</li>"
            + "<li><strong>401</strong>: No autenticado (token inválido o expirado)</li>"
            + "<li><strong>403</strong>: No autorizado (permisos insuficientes)</li>"
            + "<li><strong>404</strong>: Recurso no encontrado</li>"
            + "<li><strong>409</strong>: Conflicto (email ya registrado)</li>"
            + "</ul>";

        String securityDescription =
            "Autenticación mediante JWT (JSON Web Token)."
            + "<p>Para autenticarte:</p>"
            + "<ol>"
            + "<li>Obtén un token JWT con <code>POST /usuario/login</code></li>"
            + "<li>Copia el token de la respuesta</li>"
            + "<li>Haz clic en el botón \"Authorize\" en la parte superior</li>"
            + "<li>Escribe: <code>Bearer tu_token_jwt</code></li>"
            + "<li>Haz clic en \"Authorize\" y luego en \"Close\"</li>"
            + "</ol>";

        io.swagger.v3.oas.models.info.Info info =
            new io.swagger.v3.oas.models.info.Info()
                .title("ArtCook API")
                .version("1.0")
                .description(mainDescription)
                .contact(new io.swagger.v3.oas.models.info.Contact()
                    .name("Equipo ArtCook")
                    .email("soporte@artcook.com"))
                .license(new io.swagger.v3.oas.models.info.License()
                    .name("Licencia MIT")
                    .url("https://opensource.org/licenses/MIT"));

        io.swagger.v3.oas.models.security.SecurityScheme securityScheme =
            new io.swagger.v3.oas.models.security.SecurityScheme()
                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description(securityDescription);

        return new OpenAPI()
            .info(info)
            .components(new Components()
                .addSecuritySchemes("bearerAuth", securityScheme)
                .addResponses("UnauthorizedError",
                    new ApiResponse()
                        .description("No autenticado - Token JWT invalido o expirado")
                        .content(new Content().addMediaType("application/json",
                            new MediaType().addExamples("error",
                                new Example().value(
                                    "{\"error\": \"No autorizado\", \"mensaje\": \"Token invalido o expirado\"}")))))
                .addResponses("ForbiddenError",
                    new ApiResponse()
                        .description("Acceso prohibido - No tienes permisos suficientes")
                        .content(new Content().addMediaType("application/json",
                            new MediaType().addExamples("error",
                                new Example().value(
                                    "{\"error\": \"Acceso prohibido\", \"mensaje\": \"No tienes permisos para esta operacion\"}")))))
                .addResponses("NotFoundError",
                    new ApiResponse()
                        .description("Recurso no encontrado")
                        .content(new Content().addMediaType("application/json",
                            new MediaType().addExamples("error",
                                new Example().value(
                                    "{\"error\": \"No encontrado\", \"mensaje\": \"El recurso solicitado no existe\"}"))))));
    }
}