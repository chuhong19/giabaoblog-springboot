package vn.giabaoblog.giabaoblogserver.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.giabaoblog.giabaoblogserver.data.dto.request.AuthenticationRequest;
import vn.giabaoblog.giabaoblogserver.data.dto.response.AuthenticationResponse;
import vn.giabaoblog.giabaoblogserver.data.dto.request.RegisterRequest;
import vn.giabaoblog.giabaoblogserver.data.dto.response.StandardResponse;
import vn.giabaoblog.giabaoblogserver.services.authentication.AuthenticationService;
import vn.giabaoblog.giabaoblogserver.services.authentication.EncryptionService;


import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> internalRegister(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(StandardResponse.create(
                authenticationService.internalRegister(request)
        ));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<StandardResponse<AuthenticationResponse>> internalAuthenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(StandardResponse.create(
                authenticationService.internalAuthenticate(request)
        ));
    }

    @PostMapping("/internal/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.refreshToken(request, response);
    }

    // salt generation
    @GetMapping("/salt")
    public ResponseEntity<String> generateSalt() {
        return new ResponseEntity<String>(EncryptionService.generateSalt(32), HttpStatus.CREATED);
    }
}
