package co.edu.unbosque.artcook.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Clase de configuración para el manejo de recursos estáticos de la aplicación.
 * Permite que los videos almacenados en el servidor sean accesibles desde el cliente.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Ruta del sistema de archivos donde se almacenan los videos de las recetas.
     * Se obtiene desde el archivo de propiedades de la aplicación.
     */
    @Value("${app.videos.path}")
    private String videosPath;

    /**
     * Registra el manejador de recursos para servir videos desde una carpeta externa.
     * Mapea las peticiones que lleguen a /videos/** hacia la ruta configurada en el sistema.
     *
     * @param registry registro de manejadores de recursos de Spring MVC
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:" + Paths.get(videosPath).toAbsolutePath() + "/");
    }
}