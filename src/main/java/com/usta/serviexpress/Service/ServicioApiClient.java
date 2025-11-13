package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.ServicioEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ServicioApiClient
 *
 * Purpose:
 * - Service class responsible for fetching ServicioEntity information from an external REST API.
 * - Converts API responses into ServicioEntity objects usable within the application.
 *
 * Notes:
 * - Currently uses a simple RestTemplate; in production consider using WebClient or adding timeout/error handling.
 * - Assumes API returns a JSON object with keys: "nombre", "descripcion", "precio".
 * - Throws RuntimeException if API response is null.
 */
@Service
public class ServicioApiClient {

    /**
     * RestTemplate instance used for making HTTP requests.
     * - Can be replaced with a configured bean for timeouts, interceptors, etc.
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetches service information from an external API and maps it to ServicioEntity.
     *
     * @param tipoServicio Type or identifier of the service to fetch from the API
     * @return ServicioEntity object containing service information (name, description, price, state)
     *
     * Notes:
     * - Constructs the API URL dynamically using tipoServicio.
     * - Converts API response Map values to the corresponding fields in ServicioEntity.
     * - Sets EstadoServicio to DISPONIBLE by default.
     * - Throws RuntimeException if API does not return any data.
     * 
     * Limitations:
     * - No detailed error handling for HTTP errors (4xx/5xx) or network issues.
     * - Assumes "precio" can always be converted to BigDecimal.
     */
    public ServicioEntity obtenerServicioDesdeApi(String tipoServicio) {
        // Example URL — replace with the real API endpoint
        String url = "https://api.ejemplo.com/servicios/" + tipoServicio;

        // Perform GET request and map the JSON response to a Map
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> datos = response.getBody();

        if (datos == null) {
            throw new RuntimeException("No response received from the API");
        }

        // Create a ServicioEntity using the API response data
        ServicioEntity servicio = new ServicioEntity();
        servicio.setNombre((String) datos.get("nombre"));
        servicio.setDescripcion((String) datos.get("descripcion"));
        servicio.setPrecio(new BigDecimal(datos.get("precio").toString()));
        servicio.setEstado(ServicioEntity.EstadoServicio.DISPONIBLE);

        return servicio;
    }
}

/*
Summary (Technical Note):
ServicioApiClient fetches service details from an external REST API and maps them into
ServicioEntity instances. It currently uses RestTemplate for HTTP requests and expects
the API to return a JSON object containing "nombre", "descripcion", and "precio".
The fetched entity is set to DISPONIBLE by default. Error handling for missing API
responses is included, but more robust handling for network or HTTP errors should be
added in production.
*/
