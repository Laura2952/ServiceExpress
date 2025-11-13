package com.usta.serviexpress.Controller;

import com.usta.serviexpress.Entity.ServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Repository.ServicioRepository;
import com.usta.serviexpress.Service.ServicioService;
import com.usta.serviexpress.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * ServicioRestController
 *
 * Purpose:
 * - Exposes REST endpoints to manage ServicioEntity records.
 * - Supports basic CRUD operations: list all, get by ID, save, and delete.
 * - Handles relationships with UsuarioEntity (provider and client) when creating/updating services.
 *
 * API base path: /api/servicios
 *
 * Important considerations:
 * - All responses are in JSON format by default due to @RestController.
 * - The controller does not handle pagination or filtering; all records are returned in listar().
 * - Exception handling is minimal; save operation returns HTTP 500 on any exception.
 */
@RestController
@RequestMapping("/api/servicios")
public class ServicioRestController {

    @Autowired
    private ServicioService servicioService; // Service layer for ServicioEntity operations
    @Autowired
    private UsuarioService usuarioService; // Service layer for UsuarioEntity operations
    @Autowired
    private ServicioRepository servicioRepository; // Repository (currently unused in this controller)

    /**
     * GET /api/servicios
     * List all services.
     *
     * Returns:
     * - List of all ServicioEntity objects in the database.
     * Notes:
     * - No filtering or pagination is applied.
     */
    @GetMapping
    public List<ServicioEntity> listar() {
        return servicioService.findAll();
    }

    /**
     * GET /api/servicios/{id}
     * Find a service by its ID.
     *
     * Parameters:
     * - id: Long, primary key of the service to retrieve.
     *
     * Returns:
     * - ServicioEntity with the specified ID.
     * - If not found, the service may throw a runtime exception (depends on service implementation).
     */
    @GetMapping("/{id}")
    public ServicioEntity buscarPorId(@PathVariable Long id) {
        return servicioService.findById(id);
    }

    /**
     * POST /api/servicios
     * Create or update a ServicioEntity.
     *
     * Parameters:
     * - servicio: ServicioEntity JSON payload from the request body.
     *
     * Behavior:
     * - If the JSON contains a provider or client ID, it fetches the full UsuarioEntity from the database
     *   to maintain proper relationships.
     * - Saves the service to the database.
     *
     * Returns:
     * - HTTP 200 with the saved ServicioEntity if successful.
     * - HTTP 500 Internal Server Error if any exception occurs.
     *
     * Notes:
     * - Logs provider ID to the console for debugging purposes.
     * - Assumes incoming JSON has valid data; no explicit validation is performed here.
     */
    @PostMapping
    public ResponseEntity<ServicioEntity> guardar(@RequestBody ServicioEntity servicio) {
        try {
            // Link provider if ID is present in the request JSON
            if (servicio.getProveedor() != null && servicio.getProveedor().getIdUsuario() != null) {
                UsuarioEntity proveedor = usuarioService.findById(servicio.getProveedor().getIdUsuario());
                servicio.setProveedor(proveedor);
            }

            // Link client if ID is present in the request JSON
            if (servicio.getCliente() != null && servicio.getCliente().getIdUsuario() != null) {
                UsuarioEntity cliente = usuarioService.findById(servicio.getCliente().getIdUsuario());
                servicio.setCliente(cliente);
            }

            // Save service with full linked entities
            servicioService.save(servicio);
            System.out.println("Proveedor recibido: " + (servicio.getProveedor() != null ? servicio.getProveedor().getIdUsuario() : "NULO"));

            return ResponseEntity.ok(servicio);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * DELETE /api/servicios/{id}
     * Delete a service by its ID.
     *
     * Parameters:
     * - id: Long, primary key of the service to delete.
     *
     * Behavior:
     * - Deletes the service from the database.
     * - No error handling if the ID does not exist; exceptions may propagate from the service layer.
     */
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicioService.deleteById(id);
    }
}

/*
Summary (Technical Note):
ServicioRestController provides RESTful endpoints for managing ServicioEntity records. 
It supports listing all services, retrieving by ID, creating/updating with linked provider/client,
and deleting services by ID. All operations are JSON-based. Validation and error handling are minimal.
The controller relies on ServicioService and UsuarioService to manage business logic and relationships.
*/
