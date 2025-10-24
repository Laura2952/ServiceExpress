package com.usta.serviexpress.Controller;

import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // ====== Página de Login ======
    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    // ====== Procesar Login ======
    @PostMapping("/auth/login")
    public String login(@RequestParam("correo") String correo,
                        @RequestParam("password") String password,
                        HttpSession session,
                        RedirectAttributes ra) {

        UsuarioEntity usuario = usuarioRepository.findByCorreoIgnoreCase(correo).orElse(null);

        if (usuario != null && passwordEncoder.matches(password, usuario.getClave())) {
            // Guardar usuario en sesión
            session.setAttribute("usuario", usuario);

            // Redirigir a la URL prevista si existía
            String urlPrevista = (String) session.getAttribute("urlPrevista");
            if (urlPrevista != null) {
                session.removeAttribute("urlPrevista");
                return "redirect:" + urlPrevista;
            }

            // Si no había urlPrevista, mandar al módulo de servicios
            return "redirect:/servicio";
        }

        ra.addFlashAttribute("error", "Credenciales inválidas.");
        return "redirect:/auth/login";
    }

    // ====== Logout ======
    @GetMapping("/auth/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    // ====== Registro (mostrar form) ======
    @GetMapping("/auth/registro")
    public String registro(Model model) {
        model.addAttribute("form", new RegistroForm());
        return "auth/registro";
    }

    // ====== Procesar Registro ======
    @PostMapping("/auth/registro")
    public String registrar(@Valid @ModelAttribute("form") RegistroForm form,
                            BindingResult br,
                            RedirectAttributes ra,
                            Model model) {

        if (!br.hasFieldErrors("password") && !br.hasFieldErrors("confirmarPassword")) {
            if (!form.getPassword().equals(form.getConfirmarPassword())) {
                br.rejectValue("confirmarPassword", "mismatch", "Las contraseñas no coinciden.");
            }
        }
        if (br.hasErrors()) return "auth/registro";

        var existente = usuarioRepository.findByCorreoIgnoreCase(form.getCorreo());
        if (existente.isPresent()) {
            br.rejectValue("correo", "unique", "Ya existe una cuenta con este correo.");
            return "auth/registro";
        }

        UsuarioEntity u = new UsuarioEntity();
        u.setNombreUsuario(form.getNombreUsuario());
        u.setCorreo(form.getCorreo());
        u.setClave(passwordEncoder.encode(form.getPassword()));
        u.setTelefono(form.getTelefono());
        u.setCiudad(form.getCiudad());

        try {
            usuarioRepository.save(u);
        } catch (DataIntegrityViolationException ex) {
            br.reject("db", "No se pudo crear la cuenta. Verifica los datos.");
            return "auth/registro";
        }

        ra.addFlashAttribute("success", "Cuenta creada. ¡Inicia sesión!");
        return "redirect:/auth/login";
    }

    // ====== Clase interna para el registro ======
    @Data
    public static class RegistroForm {
        @NotBlank @Size(max = 80)
        private String nombreUsuario;

        @NotBlank @Email @Size(max = 120)
        private String correo;

        @NotBlank @Size(min = 6, max = 120)
        private String password;

        @NotBlank @Size(min = 6, max = 120)
        private String confirmarPassword;

        @NotBlank @Size(max = 30)
        private String telefono;

        @NotBlank @Size(max = 80)
        private String ciudad;
    }
}