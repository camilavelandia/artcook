package co.edu.unbosque.artcook;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Clase que permite desplegar la aplicación en un servidor de aplicaciones externo.
 * Extiende SpringBootServletInitializer para configurar el arranque en modo WAR.
 */
public class ServletInitializer extends SpringBootServletInitializer {

    /**
     * Configura la fuente principal de la aplicación para el despliegue externo.
     *
     * @param application constructor de la aplicación Spring
     * @return el builder configurado con la clase principal de la aplicación
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ArtcookApplication.class);
    }
}