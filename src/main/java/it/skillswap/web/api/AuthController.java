package it.skillswap.web.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.skillswap.domain.Student;
import it.skillswap.web.ApplicationState;
import it.skillswap.web.api.dto.StudentDto;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ApplicationState app;

    public AuthController(ApplicationState app) {
        this.app = app;
    }

    @PostMapping("/register")
    public ResponseEntity<StudentDto> register(@RequestBody Map<String, String> body) {
        Student student = app.getAuthService().register(
                body.getOrDefault("name", "").trim(),
                body.getOrDefault("className", "").trim(),
                body.getOrDefault("email", "").trim(),
                body.getOrDefault("password", ""));
        app.persist();
        return ResponseEntity.ok(StudentDto.from(student));
    }

    @PostMapping("/login")
    public ResponseEntity<StudentDto> login(@RequestBody Map<String, String> body) {
        Student student = app.getAuthService().login(
                body.getOrDefault("email", "").trim(),
                body.getOrDefault("password", ""));
        return ResponseEntity.ok(StudentDto.from(student));
    }
}
