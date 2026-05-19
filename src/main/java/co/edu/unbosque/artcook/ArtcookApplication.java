package co.edu.unbosque.artcook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.modelmapper.ModelMapper;

/**
 * Clase principal que inicia la aplicación de Spring Boot.
 * Se encarga de ejecutar el proyecto y configurar los beans necesarios.
 */
@SpringBootApplication
public class ArtcookApplication {

    /**
     * Método principal que arranca la aplicación.
     *
     * @param args argumentos de ejecución
     */
    public static void main(String[] args) {
        SpringApplication.run(ArtcookApplication.class, args);
    }

    /**
     * Crea y retorna una instancia de ModelMapper para mapeo entre entidades y DTOs.
     *
     * @return instancia de ModelMapper
     */
    @Bean
    public ModelMapper getModelMapper() {
        return new ModelMapper();
    }
}