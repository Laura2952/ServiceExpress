package com.usta.serviexpress.Controller;

import com.usta.serviexpress.Entity.ServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Repository.ServicioRepository;
import com.usta.serviexpress.Service.ServicioApiClient;
import com.usta.serviexpress.Service.ServicioService;
import com.usta.serviexpress.Service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ServicioController
 *
 * Purpose:
 * - Handles all web requests related to ServicioEntity management.
 * - Supports CRUD operations (Create, Read, Update, Delete) and listing services according to user roles (CLIENTE, PROVEEDOR, ADMIN).
 * - Supports automatic service creation via external API calls.
 *
 * Key dependencies:
 * - ServicioService: business logic for ServicioEntity.
 * - UsuarioService: used for retrieving provider users.
 * - RestTemplate: used to call external APIs for automatic service creation.
 *
 * Notes:
 * - Session attribute "usuarioSesion" must be set for access control.
 * - Role-based rendering is applied for different views.
 * - Validations are applied for mandatory fields and non-negative pricing.
 * - Deletion may fail if the service is associated with clients or ratings.
 */
@Controller
@RequestMapping("/servicio")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RestTemplate restTemplate;

    // ========================= LIST SERVICES BASED ON ROLE =========================
    /**
     * Displays a paginated list of services filtered by role.
     *
     * Parameters:
     * - nombre (optional): search term to filter services by name (case-insensitive).
     * - page (optional): zero-based page index for pagination.
     * - session: HTTP session to retrieve the currently logged-in user.
     * - model: Model to add attributes for Thymeleaf views.
     *
     * Returns:
     * - String: name of the view template corresponding to the user's role.
     *
     * Notes:
     * - CLIENTE sees only services with EstadoServicio.DISPONIBLE.
     * - PROVEEDOR sees only their own services.
     * - ADMIN sees all services.
     */
    @GetMapping
    public String listarServicios(@RequestParam(required = false) String nombre,
                                  @RequestParam(defaultValue = "0") int page,
                                  HttpSession session,
                                  Model model) {
        UsuarioEntity usuarioSesion = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (usuarioSesion == null) {
            return "redirect:/auth/login"; // Redirect to login if session is missing
        }

        String rol = usuarioSesion.getRol().getRol();
        model.addAttribute("rol", rol);
        model.addAttribute("usuarioSesion", usuarioSesion);

        Page<ServicioEntity> serviciosPage;

        if ("CLIENTE".equals(rol)) {
            List<ServicioEntity> serviciosDisponibles;
            if (nombre != null && !nombre.isEmpty()) {
                serviciosDisponibles = servicioService.findByNombreContainingIgnoreCase(nombre)
                        .stream()
                        .filter(s -> s.getEstado() == ServicioEntity.EstadoServicio.DISPONIBLE)
                        .collect(Collectors.toList());
                model.addAttribute("busqueda", nombre);
            } else {
                serviciosPage = servicioService.listar(PageRequest.of(page, 10));
                serviciosDisponibles = serviciosPage.getContent()
                        .stream()
                        .filter(s -> s.getEstado() == ServicioEntity.EstadoServicio.DISPONIBLE)
                        .collect(Collectors.toList());
            }
            model.addAttribute("servicios", serviciosDisponibles);
            return "Servicio/cliente/listarServicios";

        } else if ("PROVEEDOR".equals(rol)) {
            List<ServicioEntity> serviciosProveedor;
            if (nombre != null && !nombre.isEmpty()) {
                serviciosProveedor = servicioService.findByProveedorAndNombreContainingIgnoreCase(
                        usuarioSesion.getIdUsuario(), nombre);
                model.addAttribute("busqueda", nombre);
            } else {
                serviciosProveedor = servicioService.findByProveedor(usuarioSesion.getIdUsuario());
            }
            model.addAttribute("servicios", serviciosProveedor);
            return "Servicio/proveedor/listarServicios";

        } else if ("ADMIN".equals(rol)) {
            if (nombre != null && !nombre.isEmpty()) {
                model.addAttribute("servicios", servicioService.findByNombreContainingIgnoreCase(nombre));
                model.addAttribute("busqueda", nombre);
            } else {
                serviciosPage = servicioService.listar(PageRequest.of(page, 10));
                model.addAttribute("servicios", serviciosPage.getContent());
            }
            return "Servicio/admin/listarServicios";
        }

        return "redirect:/"; // Redirect to home if role is unrecognized
    }

    // ========================= CREATE SERVICE =========================
    /**
     * Displays the form for creating a new service.
     *
     * Parameters:
     * - model: Model to add attributes for Thymeleaf.
     * - session: HTTP session to retrieve the current user.
     *
     * Returns:
     * - String: view template for service creation.
     */
    @GetMapping("/crearServicio")
    public String crearServicioForm(Model model, HttpSession session) {
        UsuarioEntity usuarioSesion = (UsuarioEntity) session.getAttribute("usuarioSesion");
        String rol = usuarioSesion.getRol().getRol();

        model.addAttribute("servicio", new ServicioEntity());
        model.addAttribute("proveedores", usuarioService.findAllProveedores());
        model.addAttribute("estados", ServicioEntity.EstadoServicio.values());
        model.addAttribute("rol", rol);

        return "Servicio/admin/crearServicio";
    }

    /**
     * Handles the submission of a new service.
     *
     * Parameters:
     * - servicio: ServicioEntity bound from the form.
     * - result: BindingResult to capture validation errors.
     * - proveedorId (optional): ID of the provider for ADMIN-created services.
     * - session: HTTP session to retrieve the current user.
     * - model: Model for returning errors to the view if validation fails.
     *
     * Returns:
     * - Redirect to "/servicio" on success.
     * - Returns creation form with errors if validation fails.
     *
     * Notes:
     * - Automatically assigns EstadoServicio.DISPONIBLE if missing.
     * - For PROVEEDOR role, the logged-in user is assigned as the provider.
     * - Ensures non-empty name, description, and non-negative price.
     */
    @PostMapping("/guardar")
    public String guardarServicio(@Valid @ModelAttribute("servicio") ServicioEntity servicio,
                                  BindingResult result,
                                  @RequestParam(required = false) Long proveedorId,
                                  HttpSession session,
                                  Model model) {
        // Validate required fields
        if (servicio.getNombre() == null || servicio.getNombre().trim().isEmpty()) {
            result.rejectValue("nombre", "error.nombre", "El nombre no puede estar vacío");
        }
        if (servicio.getDescripcion() == null || servicio.getDescripcion().trim().isEmpty()) {
            result.rejectValue("descripcion", "error.descripcion", "La descripción no puede estar vacía");
        }
        if (servicio.getPrecio() == null) {
            result.rejectValue("precio", "error.precio", "El precio no puede estar vacío");
        } else if (servicio.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            result.rejectValue("precio", "error.precio", "El precio no puede ser negativo");
        }

        if (result.hasErrors()) {
            UsuarioEntity usuarioSesion = (UsuarioEntity) session.getAttribute("usuarioSesion");
            String rol = usuarioSesion.getRol().getRol();
            model.addAttribute("rol", rol);
            model.addAttribute("proveedores", usuarioService.findAllProveedores());
            model.addAttribute("estados", ServicioEntity.EstadoServicio.values());
            return "Servicio/admin/crearServicio";
        }

        if (servicio.getEstado() == null) {
            servicio.setEstado(ServicioEntity.EstadoServicio.DISPONIBLE);
        }

        UsuarioEntity usuarioSesion = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (usuarioSesion != null && "PROVEEDOR".equals(usuarioSesion.getRol().getRol())) {
            servicio.setProveedor(usuarioSesion);
        } else if (proveedorId != null) {
            UsuarioEntity proveedor = usuarioService.findById(proveedorId);
            servicio.setProveedor(proveedor);
        }

        servicioService.save(servicio);
        return "redirect:/servicio";
    }

    // ========================= EDIT SERVICE =========================
    /**
     * Displays the edit form for a specific service by ID.
     *
     * Parameters:
     * - id: ID of the service to edit.
     * - model: Model for Thymeleaf view attributes.
     * - session: HTTP session to get current user and role.
     *
     * Returns:
     * - String: view template for service editing.
     */
    @GetMapping("/editar/{id}")
    public String editarServicio(@PathVariable Long id, Model model, HttpSession session) {
        UsuarioEntity usuarioSesion = (UsuarioEntity) session.getAttribute("usuarioSesion");
        String rol = usuarioSesion.getRol().getRol();

        ServicioEntity servicio = servicioService.findById(id);
        model.addAttribute("servicio", servicio);
        model.addAttribute("proveedores", usuarioService.findAllProveedores());
        model.addAttribute("estados", ServicioEntity.EstadoServicio.values());
        model.addAttribute("rol", rol);

        return "Servicio/admin/editarServicio";
    }

    /**
     * Handles updating an existing service.
     *
     * Parameters:
     * - id: ID of the service being updated.
     * - servicioActualizado: ServicioEntity bound from the edit form.
     * - result: BindingResult for validation errors.
     * - proveedorId (optional): provider ID if ADMIN updates the provider.
     * - session: HTTP session for current user and role.
     * - model: Model for returning errors to view if validation fails.
     *
     * Returns:
     * - Redirect to "/servicio" on success.
     * - Returns edit form with errors if validation fails.
     */
    @PostMapping("/editar/{id}")
    public String actualizarServicio(@PathVariable Long id,
                                     @Valid @ModelAttribute("servicio") ServicioEntity servicioActualizado,
                                     BindingResult result,
                                     @RequestParam(required = false) Long proveedorId,
                                     HttpSession session,
                                     Model model) {

        // Validation logic (same as creation)
        if (servicioActualizado.getNombre() == null || servicioActualizado.getNombre().trim().isEmpty()) {
            result.rejectValue("nombre", "error.nombre", "El nombre no puede estar vacío");
        }
        if (servicioActualizado.getDescripcion() == null || servicioActualizado.getDescripcion().trim().isEmpty()) {
            result.rejectValue("descripcion", "error.descripcion", "La descripción no puede estar vacía");
        }
        if (servicioActualizado.getPrecio() == null) {
            result.rejectValue("precio", "error.precio", "El precio no puede estar vacío");
        } else if (servicioActualizado.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            result.rejectValue("precio", "error.precio", "El precio no puede ser negativo");
        }

        if (result.hasErrors()) {
            UsuarioEntity usuarioSesion = (UsuarioEntity) session.getAttribute("usuarioSesion");
            String rol = usuarioSesion.getRol().getRol();
            model.addAttribute("rol", rol);
            model.addAttribute("proveedores", usuarioService.findAllProveedores());
            model.addAttribute("estados", ServicioEntity.EstadoServicio.values());
            return "Servicio/admin/editarServicio";
        }

        ServicioEntity servicioExistente = servicioService.findById(id);
        if (servicioExistente != null) {
            // Update fields
            servicioExistente.setNombre(servicioActualizado.getNombre());
            servicioExistente.setDescripcion(servicioActualizado.getDescripcion());
            servicioExistente.setPrecio(servicioActualizado.getPrecio());
            servicioExistente.setEstado(servicioActualizado.getEstado());

            UsuarioEntity usuarioSesion = (UsuarioEntity) session.getAttribute("usuarioSesion");
            if (usuarioSesion != null && "PROVEEDOR".equals(usuarioSesion.getRol().getRol())) {
                servicioExistente.setProveedor(usuarioSesion);
            } else if (proveedorId != null) {
                UsuarioEntity proveedor = usuarioService.findById(proveedorId);
                servicioExistente.setProveedor(proveedor);
            }

            servicioService.save(servicioExistente);
        }
        return "redirect:/servicio";
    }

    // ========================= DELETE SERVICE =========================
    /**
     * Deletes a service by ID.
     *
     * Parameters:
     * - id: ID of the service to delete.
     * - page: current pagination page (default 0).
     * - session: HTTP session for current user and role.
     * - model: Model to pass error messages if deletion fails.
     *
     * Returns:
     * - Redirect to "/servicio" if deletion succeeds.
     * - Returns role-specific listing page with error message if deletion fails due to foreign key constraints.
     *
     * Notes:
     * - DataIntegrityViolationException is caught if the service is linked to clients or ratings.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarServicio(@PathVariable Long id,
                                   @RequestParam(defaultValue = "0") int page,
                                   HttpSession session,
                                   Model model) {
        try {
            servicioService.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "❌ Cannot delete service because it is associated with clients or ratings.");

            UsuarioEntity usuarioSesion = (UsuarioEntity) session.getAttribute("usuarioSesion");
            String rol = usuarioSesion.getRol().getRol();
            model.addAttribute("rol", rol);
            model.addAttribute("usuarioSesion", usuarioSesion);

            // Re-render service list based on role
            if ("CLIENTE".equals(rol)) {
                List<ServicioEntity> serviciosDisponibles = servicioService.listar(PageRequest.of(page, 10))
                        .getContent()
                        .stream()
                        .filter(s -> s.getEstado() == ServicioEntity.EstadoServicio.DISPONIBLE)
                        .collect(Collectors.toList());
                model.addAttribute("servicios", serviciosDisponibles);
                return "Servicio/cliente/listarServicios";

            } else if ("PROVEEDOR".equals(rol)) {
                List<ServicioEntity> serviciosProveedor = servicioService.findByProveedor(usuarioSesion.getIdUsuario());
                model.addAttribute("servicios", serviciosProveedor);
                return "Servicio/proveedor/listarServicios";

            } else {
                Page<ServicioEntity> serviciosPage = servicioService.listar(PageRequest.of(page, 10));
                model.addAttribute("servicios", serviciosPage.getContent());
                return "Servicio/admin/listarServicios";
            }
        }
        return "redirect:/servicio";
    }

    // ========================= AUTOMATIC SERVICE CREATION VIA EXTERNAL API =========================
    /**
     * Creates a new service automatically by calling an external API.
     *
     * Parameters:
     * - tipo: type of service used to construct the API URL.
     *
     * Returns:
     * - ResponseEntity containing the created ServicioEntity on success.
     * - BadRequest if API returns null.
     * - InternalServerError on exceptions.
     *
     * Notes:
     * - Sets EstadoServicio.PENDIENTE if missing.
     */
    @PostMapping("/auto/{tipo}")
    public ResponseEntity<ServicioEntity> crearServicioAutomatico(@PathVariable String tipo) {
        try {
            String apiUrl = "https://api.ejemplo.com/servicios/" + tipo;
            ServicioEntity servicioExterno = restTemplate.getForObject(apiUrl, ServicioEntity.class);

            if (servicioExterno == null) {
                return ResponseEntity.badRequest().build();
            }

            if (servicioExterno.getEstado() == null) {
                servicioExterno.setEstado(ServicioEntity.EstadoServicio.PENDIENTE);
            }

            servicioService.save(servicioExterno);

            return ResponseEntity.ok(servicioExterno);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Alternative method for creating a service from an external API by service name.
     *
     * Parameters:
     * - nombreServicio: name of the service used to query the external API.
     *
     * Returns:
     * - Success message if service was created.
     * - Error message if API data could not be retrieved.
     *
     * Note:
     * - Currently uses a placeholder API URL.
     */
    @GetMapping("/auto/{nombreServicio}")
    public String crearServicioDesdeApi(@PathVariable String nombreServicio) {
        String url = "https://api.ejemplo.com/servicios/" + nombreServicio;

        ServicioEntity servicioApi = restTemplate.getForObject(url, ServicioEntity.class);

        if (servicioApi != null) {
            servicioService.save(servicioApi);
            return "Servicio " + nombreServicio + " creado exitosamente desde la API.";
        } else {
            return "No se pudo obtener información del servicio " + nombreServicio;
        }
    }

}

/*
Summary (Technical Note):
ServicioController handles CRUD operations for ServicioEntity, including role-based listings
for CLIENTE, PROVEEDOR, and ADMIN users. It validates inputs (name, description, price), ensures
non-negative prices, and assigns providers automatically based on user role or selection. It
supports deletion with integrity checks and automatic creation of services via external APIs.
Views are dynamically rendered based on role, and session management is enforced.
*/
