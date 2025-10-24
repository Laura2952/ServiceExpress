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

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ServicioService servicioService;

    // (FALTABA) Mostrar página de servicios disponibles para solicitar
    @GetMapping("/{idCliente}/solicitarServicio")
    public String mostrarServiciosDisponibles(@PathVariable Long idCliente,
                                              org.springframework.ui.Model model) {
        UsuarioEntity cliente = usuarioService.findById(idCliente);
        if (cliente == null) {
            model.addAttribute("error", "Cliente no encontrado con ID: " + idCliente);
            return "error/clienteNoEncontrado";
        }

        // Obtener disponibles y filtrar los que tienen proveedor
        List<ServicioEntity> disponibles = servicioService.findDisponibles();
        disponibles.removeIf(s -> s.getProveedor() == null);

        model.addAttribute("serviciosDisponibles", disponibles);
        model.addAttribute("idCliente", idCliente);
        return "Clientes/solicitarServicio";
    }

    // 1. Solicitar un servicio
    @PostMapping("/{idCliente}/solicitarServicio/{idServicio}")
    public String solicitarServicio(@PathVariable("idCliente") Long idCliente,
                                    @PathVariable("idServicio") Long idServicio,
                                    RedirectAttributes redirectAttributes) {

        UsuarioEntity cliente = usuarioService.findById(idCliente);
        ServicioEntity servicio = servicioService.findById(idServicio);

        if (cliente == null || !cliente.getRol().getRol().equalsIgnoreCase("CLIENTE")) {
            redirectAttributes.addFlashAttribute("error", "Cliente no encontrado o inválido");
            return "redirect:/usuarios";
        }

        if (servicio == null) {
            redirectAttributes.addFlashAttribute("error", "Servicio no encontrado");
            return "redirect:/cliente/" + idCliente + "/servicios";
        }

        servicio.setCliente(cliente); // debe existir el campo cliente en ServicioEntity
        servicioService.save(servicio);

        redirectAttributes.addFlashAttribute("success", "Servicio solicitado correctamente");
        return "redirect:/cliente/" + idCliente + "/servicios";
    }

    // 2. Cancelar un servicio
    @PostMapping("/{idCliente}/cancelarServicio/{idServicio}")
    public String cancelarServicio(@PathVariable("idCliente") Long idCliente,
                                   @PathVariable("idServicio") Long idServicio,
                                   RedirectAttributes redirectAttributes) {

        ServicioEntity servicio = servicioService.findById(idServicio);

        if (servicio == null) {
            redirectAttributes.addFlashAttribute("error", "Servicio no encontrado");
            return "redirect:/cliente/" + idCliente + "/servicios";
        }

        servicio.setCliente(null);
        servicioService.save(servicio);

        redirectAttributes.addFlashAttribute("success", "Servicio cancelado correctamente");
        return "redirect:/cliente/" + idCliente + "/servicios";
    }

    // 3. Historial de servicios del cliente
    @GetMapping("/{idCliente}/historialServicios")
    public String historialServicios(@PathVariable("idCliente") Long idCliente,
                                     org.springframework.ui.Model model,
                                     RedirectAttributes redirectAttributes) {

        UsuarioEntity cliente = usuarioService.findById(idCliente);

        if (cliente == null || !cliente.getRol().getRol().equalsIgnoreCase("CLIENTE")) {
            redirectAttributes.addFlashAttribute("error", "Cliente no encontrado o inválido");
            return "redirect:/usuarios";
        }

        List<ServicioEntity> historial = servicioService.findByCliente(cliente);
        model.addAttribute("historial", historial);

        return "Cliente/historialServicios";
    }
}