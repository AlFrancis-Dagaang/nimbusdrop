package dev.pollywag.nimbusdrop.controller;

import dev.pollywag.nimbusdrop.dto.ApiResponse;
import dev.pollywag.nimbusdrop.dto.CreateNimbusRequest;
import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.service.NimbusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/user/nimbus")
public class NimbusController {
    private final NimbusService nimbusService;

    public NimbusController(NimbusService nimbusService) {
        this.nimbusService = nimbusService;
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Nimbus>> createNimbus(@RequestBody CreateNimbusRequest body, Principal principal) {
        Nimbus nimbus = nimbusService.createNimbus(body.getNimbusName(), principal.getName());
        //Bug recursive Sending Data(Sol'n: Create DTO or annonation for JSON in each class)
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Success nimbus created", nimbus));
    }
}
