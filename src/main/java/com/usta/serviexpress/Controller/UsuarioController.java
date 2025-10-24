package com.usta.serviexpress.Controller;

import com.usta.serviexpress.Entity.RolEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Service.RolService;
import com.usta.serviexpress.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private RolService rolService;

    // Registro público (si lo quieres fuera de /usuarios, déjalo como está)
    @GetMapping("/register")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new UsuarioEntity());
        model.addAttribute("title", "Registro de Usuario");
        return "register";
    }

    @PostMapping("/register")
    public String registrarUsuario(@ModelAttribute("usuario") @Valid UsuarioEntity usuario,
                                   BindingResult result,
                                   @RequestParam("confirmarClave") String confirmarClave,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   SessionStatus status) {

        // Validaciones básicas del form
        if (result.hasErrors()) {
            model.addAttribute("title", "Registro de Usuario");
            return "register";
        }

        if (usuario.getClave() == null || confirmarClave == null || !usuario.getClave().equals(confirmarClave)) {
            result.rejectValue("clave", "error.usuario", "Las contraseñas no coinciden.");
            model.addAttribute("title", "Registro de Usuario");
            return "register";
        }

        // (Opcional) si tienes un existsByCorreo bien implementado:
        // if (usuarioService.existsByCorreo(usuario.getCorreo())) {
        //     result.rejectValue("correo", "error.usuario", "El correo ya está registrado.");
        //     model.addAttribute("title", "Registro de Usuario");
        //     return "register";
        // }

        // 1) Codificar contraseña
        usuario.setClave(new BCryptPasswordEncoder().encode(usuario.getClave()));

        // 2) Asignar rol CLIENTE (id = 3) desde el servicio
        RolEntity rolCliente = rolService.findById(3L);
        if (rolCliente == null) {
            // Protegemos por si alguien borró el rol de la tabla
            throw new IllegalStateException("No existe el rol CLIENTE (id=3) en la tabla roles");
        }
        usuario.setRol(rolCliente);

        // 3) Guardar
        usuarioService.save(usuario);
        status.setComplete();
        redirectAttributes.addFlashAttribute("success", "¡Usuario registrado correctamente!");
        return "redirect:/login";
    }


    // Listado
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("title", "Gestionar Usuarios");
        model.addAttribute("urlRegisterUser", "/usuarios/crear"); // botón "nuevo"
        List<UsuarioEntity> lista = usuarioService.findAll();
        lista.sort(Comparator.comparing(UsuarioEntity::getIdUsuario));
        model.addAttribute("Usuarios", lista);
        return "Administrador/ListarUsuario";
    }

    // Crear
    @GetMapping("/crear")
    public String crearForm(Model model) {
        model.addAttribute("title", "Nuevo Usuario");
        model.addAttribute("usuario", new UsuarioEntity());
        model.addAttribute("listaRoles", rolService.findAll());
        return "Administrador/formUsuario";
    }

    @PostMapping("/crear")
    public String crear(@Valid @ModelAttribute("usuario") UsuarioEntity usuario,
                        BindingResult result,
                        RedirectAttributes ra,
                        Model model) {
        if (result.hasErrors()) {
            model.addAttribute("listaRoles", rolService.findAll());
            return "Administrador/formUsuario";
        }
        // Si no seleccionaron rol en el form, forzamos CLIENTE
        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            RolEntity rolCliente = rolService.findById(3L);
            if (rolCliente == null) throw new IllegalStateException("Falta rol CLIENTE (id=3)");
            usuario.setRol(rolCliente);
        }
        usuarioService.save(usuario);
        ra.addFlashAttribute("mensajeExito", "Usuario creado correctamente");
        return "redirect:/usuarios";
    }

    // Editar
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable("id") Long idUsuario, Model model) {
        UsuarioEntity usuario = usuarioService.findById(idUsuario);
        if (usuario == null) {
            return "redirect:/usuarios";
        }
        model.addAttribute("title", "Editar Usuario");
        model.addAttribute("usuarioEdit", usuario);
        model.addAttribute("listaRoles", rolService.findAll());
        return "Administrador/editarUsuario";
    }

    @PostMapping("/{id}/editar")
    public String editar(@Valid @ModelAttribute("usuarioEdit") UsuarioEntity usuario,
                         BindingResult result,
                         @PathVariable("id") Long idUsuario,
                         RedirectAttributes ra,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("listaRoles", rolService.findAll());
            model.addAttribute("title", "Editar Usuario");
            return "Administrador/editarUsuario";
        }
        UsuarioEntity existente = usuarioService.findById(idUsuario);
        if (existente == null) {
            ra.addFlashAttribute("errorMensaje", "Usuario no encontrado");
            return "redirect:/usuarios";
        }
        existente.setCorreo(usuario.getCorreo());
        existente.setClave(usuario.getClave());
        existente.setNombreUsuario(usuario.getNombreUsuario());
        existente.setRol(usuario.getRol());
        usuarioService.save(existente);

        ra.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente");
        return "redirect:/usuarios";
    }

    // Eliminar
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") long id, RedirectAttributes ra) {
        UsuarioEntity usuario = usuarioService.findById(id);
        if (usuario == null) {
            ra.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/usuarios";
        }
        try {
            usuarioService.deleteById(id);
            ra.addFlashAttribute("success", "Usuario eliminado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }
}