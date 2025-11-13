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

/**
 * AuthController
 *
 * Purpose:
 * - Handles user authentication (login, logout) and registration for the ServiExpress application.
 * - Uses Spring MVC annotations for routing and form processing.
 * - Manages session state for authenticated users.
 *
 * Dependencies:
 * - UsuarioRepository: to query and persist user entities.
 * - PasswordEncoder: for secure password hashing and verification.
 *
 * Notes:
 * - Uses server-side validation through Jakarta Validation annotations.
 * - Stores logged-in user information in HttpSession.
 * - RedirectAttributes are used for flash messages between redirects.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository; // Repository for user entity persistence and retrieval.
    private final PasswordEncoder passwordEncoder;     // Handles password hashing and matching.

    /**
     * Displays the login page.
     *
     * Route: GET /auth/login
     * Returns: View name for login form (auth/login).
     */
    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    /**
     * Processes login credentials and establishes a session.
     *
     * Route: POST /auth/login
     *
     * @param correo   Email address entered by the user.
     * @param password Plain text password input.
     * @param session  HttpSession used to store logged-in user data.
     * @param ra       RedirectAttributes for flash messages (e.g., invalid credentials).
     * @return Redirect to the intended URL or default page if login is successful,
     *         otherwise redirect back to login page with an error message.
     *
     * Notes:
     * - Passwords are compared using the PasswordEncoder to ensure secure hash verification.
     * - If the session contains a "urlPrevista", the user is redirected there after successful login.
     */
    @PostMapping("/auth/login")
    public String login(@RequestParam("correo") String correo,
                        @RequestParam("password") String password,
                        HttpSession session,
                        RedirectAttributes ra) {

        // Attempt to find the user by email (case-insensitive)
        UsuarioEntity usuario = usuarioRepository.findByCorreoIgnoreCase(correo).orElse(null);

        // Validate user existence and password match
        if (usuario != null && passwordEncoder.matches(password, usuario.getClave())) {
            // Store the authenticated user in session
            session.setAttribute("usuario", usuario);

            // Redirect to previously requested URL, if present
            String urlPrevista = (String) session.getAttribute("urlPrevista");
            if (urlPrevista != null) {
                session.removeAttribute("urlPrevista");
                return "redirect:" + urlPrevista;
            }

            // Default redirect after login (no urlPrevista found)
            return "redirect:/servicio";
        }

        // Invalid credentials: return to login with error message
        ra.addFlashAttribute("error", "Credenciales inválidas.");
        return "redirect:/auth/login";
    }

    /**
     * Logs the user out by invalidating the session.
     *
     * Route: GET /auth/logout
     *
     * @param session HttpSession to invalidate.
     * @return Redirect to login page after session termination.
     *
     * Notes:
     * - Completely clears session attributes.
     */
    @GetMapping("/auth/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    /**
     * Displays the registration form.
     *
     * Route: GET /auth/registro
     *
     * @param model Spring Model to hold form data.
     * @return View name for registration page (auth/registro).
     */
    @GetMapping("/auth/registro")
    public String registro(Model model) {
        model.addAttribute("form", new RegistroForm());
        return "auth/registro";
    }

    /**
     * Handles user registration form submission.
     *
     * Route: POST /auth/registro
     *
     * @param form  Form data containing registration information.
     * @param br    BindingResult containing validation results.
     * @param ra    RedirectAttributes for flash messages.
     * @param model Model used for returning to the form on validation errors.
     * @return Redirect to login page upon success, or back to the registration page if validation fails.
     *
     * Logic details:
     * - Ensures both password fields match.
     * - Validates email uniqueness in the database.
     * - Password is securely encoded before persisting the user entity.
     *
     * Exception handling:
     * - Catches DataIntegrityViolationException for potential database constraint errors.
     */
    @PostMapping("/auth/registro")
    public String registrar(@Valid @ModelAttribute("form") RegistroForm form,
                            BindingResult br,
                            RedirectAttributes ra,
                            Model model) {

        // Validate password and confirmation match
        if (!br.hasFieldErrors("password") && !br.hasFieldErrors("confirmarPassword")) {
            if (!form.getPassword().equals(form.getConfirmarPassword())) {
                br.rejectValue("confirmarPassword", "mismatch", "Las contraseñas no coinciden.");
            }
        }

        // Stop and return to form if validation failed
        if (br.hasErrors()) return "auth/registro";

        // Check if email is already registered
        var existente = usuarioRepository.findByCorreoIgnoreCase(form.getCorreo());
        if (existente.isPresent()) {
            br.rejectValue("correo", "unique", "Ya existe una cuenta con este correo.");
            return "auth/registro";
        }

        // Create and populate new user entity
        UsuarioEntity u = new UsuarioEntity();
        u.setNombreUsuario(form.getNombreUsuario());
        u.setCorreo(form.getCorreo());
        u.setClave(passwordEncoder.encode(form.getPassword())); // Hash the password before saving
        u.setTelefono(form.getTelefono());
        u.setCiudad(form.getCiudad());

        try {
            usuarioRepository.save(u);
        } catch (DataIntegrityViolationException ex) {
            // Handle unexpected database constraint violations
            br.reject("db", "No se pudo crear la cuenta. Verifica los datos.");
            return "auth/registro";
        }

        // Success: add flash message and redirect to login page
        ra.addFlashAttribute("success", "Cuenta creada. ¡Inicia sesión!");
        return "redirect:/auth/login";
    }

    /**
     * RegistroForm
     *
     * Purpose:
     * - DTO (Data Transfer Object) for capturing user registration form input.
     * - Uses Jakarta Validation annotations for automatic server-side validation.
     *
     * Fields:
     * - nombreUsuario: Display name of the user (max 80 chars).
     * - correo: Unique email (max 120 chars), validated as a proper email format.
     * - password / confirmarPassword: Password fields (min 6 chars).
     * - telefono: Contact number (max 30 chars).
     * - ciudad: User’s city or region (max 80 chars).
     *
     * Notes:
     * - The password fields are compared manually during registration validation.
     * - Validation annotations ensure form-level input safety and consistency.
     */
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

/*
Summary (Technical Note):
AuthController provides all authentication-related endpoints for the ServiExpress system.
It handles login, logout, and registration workflows using Spring MVC and validation annotations.
Passwords are securely hashed with PasswordEncoder, and authenticated users are stored in session.
Registration includes validation for password confirmation and email uniqueness.
In case of persistence issues, friendly error messages are displayed to the user.
*/
