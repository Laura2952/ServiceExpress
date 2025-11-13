package com.usta.serviexpress.Controller;

import com.usta.serviexpress.Entity.ServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Service.ServicioService;
import com.usta.serviexpress.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * ClienteController
 *
 * Purpose:
 * - Handles client-facing operations in the ServiExpress application.
 * - Provides endpoints for viewing available services, requesting or canceling a service,
 *   and viewing a client's service history.
 *
 * Responsibilities:
 * - Validates client existence and role before performing actions.
 * - Manages association between Cliente (UsuarioEntity) and ServicioEntity.
 * - Communicates user feedback through RedirectAttributes.
 *
 * View templates expected:
 * - "Clientes/solicitarServicio"
 * - "Cliente/historialServicios"
 * - "error/clienteNoEncontrado"
 *
 * Notes:
 * - All routes are prefixed with "/cliente".
 * - Uses path variables for identifying client and service IDs.
 */
@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private UsuarioService usuarioService;   // Service for managing user-related operations.

    @Autowired
    private ServicioService servicioService; // Service for managing service entities and assignments.

    /**
     * Displays a page listing all available services that can be requested by a specific client.
     *
     * Route: GET /cliente/{idCliente}/solicitarServicio
     *
     * @param idCliente ID of the client attempting to request a service.
     * @param model     Spring model to populate view attributes.
     * @return The service request view ("Clientes/solicitarServicio") or an error page if the client is not found.
     *
     * Behavior:
     * - Verifies that the client exists.
     * - Retrieves all available services from the system (using servicioService.findDisponibles()).
     * - Filters out services without assigned providers.
     * - Passes the filtered list and client ID to the view for rendering.
     */
    @GetMapping("/{idCliente}/solicitarServicio")
    public String mostrarServiciosDisponibles(@PathVariable Long idCliente,
                                              org.springframework.ui.Model model) {
        UsuarioEntity cliente = usuarioService.findById(idCliente);
        if (cliente == null) {
            model.addAttribute("error", "Cliente no encontrado con ID: " + idCliente);
            return "error/clienteNoEncontrado";
        }

        // Obtain available services and filter out those without providers
        List<ServicioEntity> disponibles = servicioService.findDisponibles();
        disponibles.removeIf(s -> s.getProveedor() == null);

        model.addAttribute("serviciosDisponibles", disponibles);
        model.addAttribute("idCliente", idCliente);

        return "Clientes/solicitarServicio";
    }

    /**
     * Handles the request of a service by a client.
     *
     * Route: POST /cliente/{idCliente}/solicitarServicio/{idServicio}
     *
     * @param idCliente          ID of the client requesting the service.
     * @param idServicio         ID of the service to be requested.
     * @param redirectAttributes RedirectAttributes for displaying flash messages.
     * @return Redirect to the client’s service list upon success or an appropriate redirect on error.
     *
     * Logic:
     * - Validates that the client exists and has the "CLIENTE" role.
     * - Validates that the service exists.
     * - Assigns the client to the service (servicio.setCliente(cliente)).
     * - Persists the updated service.
     *
     * Assumptions:
     * - ServicioEntity contains a field "cliente" with a proper relationship mapping.
     */
    @PostMapping("/{idCliente}/solicitarServicio/{idServicio}")
    public String solicitarServicio(@PathVariable("idCliente") Long idCliente,
                                    @PathVariable("idServicio") Long idServicio,
                                    RedirectAttributes redirectAttributes) {

        UsuarioEntity cliente = usuarioService.findById(idCliente);
        ServicioEntity servicio = servicioService.findById(idServicio);

        // Validate client existence and role
        if (cliente == null || !cliente.getRol().getRol().equalsIgnoreCase("CLIENTE")) {
            redirectAttributes.addFlashAttribute("error", "Cliente no encontrado o inválido");
            return "redirect:/usuarios";
        }

        // Validate service existence
        if (servicio == null) {
            redirectAttributes.addFlashAttribute("error", "Servicio no encontrado");
            return "redirect:/cliente/" + idCliente + "/servicios";
        }

        // Assign client to the service (assuming one-to-one or one-to-many mapping)
        servicio.setCliente(cliente);
        servicioService.save(servicio);

        redirectAttributes.addFlashAttribute("success", "Servicio solicitado correctamente");
        return "redirect:/cliente/" + idCliente + "/servicios";
    }

    /**
     * Cancels a previously requested service for a client.
     *
     * Route: POST /cliente/{idCliente}/cancelarServicio/{idServicio}
     *
     * @param idCliente          ID of the client canceling the service.
     * @param idServicio         ID of the service to cancel.
     * @param redirectAttributes RedirectAttributes for success/error feedback.
     * @return Redirect to the client’s service list.
     *
     * Logic:
     * - Validates that the service exists.
     * - Removes the client association from the service (sets cliente to null).
     * - Saves the updated service entity.
     */
    @PostMapping("/{idCliente}/cancelarServicio/{idServicio}")
    public String cancelarServicio(@PathVariable("idCliente") Long idCliente,
                                   @PathVariable("idServicio") Long idServicio,
                                   RedirectAttributes redirectAttributes) {

        ServicioEntity servicio = servicioService.findById(idServicio);

        // Handle missing service
        if (servicio == null) {
            redirectAttributes.addFlashAttribute("error", "Servicio no encontrado");
            return "redirect:/cliente/" + idCliente + "/servicios";
        }

        // Remove client association
        servicio.setCliente(null);
        servicioService.save(servicio);

        redirectAttributes.addFlashAttribute("success", "Servicio cancelado correctamente");
        return "redirect:/cliente/" + idCliente + "/servicios";
    }

    /**
     * Displays the service history for a specific client.
     *
     * Route: GET /cliente/{idCliente}/historialServicios
     *
     * @param idCliente          ID of the client whose history is being requested.
     * @param model              Model for adding attributes to the view.
     * @param redirectAttributes RedirectAttributes for redirection messages.
     * @return The view name "Cliente/historialServicios" if valid, otherwise redirects to /usuarios.
     *
     * Behavior:
     * - Validates that the user exists and is a client.
     * - Retrieves all services linked to that client.
     * - Passes the service history list to the view.
     */
    @GetMapping("/{idCliente}/historialServicios")
    public String historialServicios(@PathVariable("idCliente") Long idCliente,
                                     org.springframework.ui.Model model,
                                     RedirectAttributes redirectAttributes) {

        UsuarioEntity cliente = usuarioService.findById(idCliente);

        // Validate client existence and role
        if (cliente == null || !cliente.getRol().getRol().equalsIgnoreCase("CLIENTE")) {
            redirectAttributes.addFlashAttribute("error", "Cliente no encontrado o inválido");
            return "redirect:/usuarios";
        }

        // Retrieve client’s service history
        List<ServicioEntity> historial = servicioService.findByCliente(cliente);
        model.addAttribute("historial", historial);

        return "Cliente/historialServicios";
    }
}

/*
Summary (Technical Note):
ClienteController manages service-related actions for clients in ServiExpress. 
It enables clients to view available services, request and cancel them, and review 
their service history. The controller validates the user's role before performing 
client-only actions and handles redirects and error feedback via flash attributes. 
It interacts closely with UsuarioService and ServicioService for persistence and data retrieval.
*/
