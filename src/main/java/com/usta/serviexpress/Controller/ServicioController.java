package com.usta.serviexpress.Controller;

import com.usta.serviexpress.Entity.ServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Service.ServicioService;
import com.usta.serviexpress.Service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/servicio")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private UsuarioService usuarioService;

    // ========================= LISTAR SEGÚN ROL =========================
    @GetMapping
    public String listarServicios(@RequestParam(required = false) String nombre,
                                  @RequestParam(defaultValue = "0") int page,
                                  HttpSession session,
                                  Model model) {
        UsuarioEntity usuarioSesion = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (usuarioSesion == null) {
            return "redirect:/auth/login";
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

        return "redirect:/";
    }

    // ========================= CREAR SERVICIO =========================
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

    @PostMapping("/guardar")
    public String guardarServicio(@Valid @ModelAttribute("servicio") ServicioEntity servicio,
                                  BindingResult result,
                                  @RequestParam(required = false) Long proveedorId,
                                  HttpSession session,
                                  Model model) {
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

    // ========================= EDITAR =========================
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

    @PostMapping("/editar/{id}")
    public String actualizarServicio(@PathVariable Long id,
                                     @Valid @ModelAttribute("servicio") ServicioEntity servicioActualizado,
                                     BindingResult result,
                                     @RequestParam(required = false) Long proveedorId,
                                     HttpSession session,
                                     Model model) {

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

    // ========================= ELIMINAR =========================
    @PostMapping("/eliminar/{id}")
    public String eliminarServicio(@PathVariable Long id,
                                   @RequestParam(defaultValue = "0") int page,
                                   HttpSession session,
                                   Model model) {
        try {
            servicioService.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "❌ No se puede eliminar el servicio porque está asociado a clientes o calificaciones.");

            UsuarioEntity usuarioSesion = (UsuarioEntity) session.getAttribute("usuarioSesion");
            String rol = usuarioSesion.getRol().getRol();
            model.addAttribute("rol", rol);
            model.addAttribute("usuarioSesion", usuarioSesion);

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
}