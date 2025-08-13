package dev.pollywag.nimbusdrop.controller;

import dev.pollywag.nimbusdrop.dto.respondeDTO.ApiResponse;
import dev.pollywag.nimbusdrop.dto.requestDTO.CreateNimbusRequest;
import dev.pollywag.nimbusdrop.dto.respondeDTO.NimbusResponse;
import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.service.NimbusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/user/nimbus")
public class NimbusController {
    private final NimbusService nimbusService;

    public NimbusController(NimbusService nimbusService) {
        this.nimbusService = nimbusService;
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<NimbusResponse>> createNimbus(@RequestBody CreateNimbusRequest body, Principal principal) {
        NimbusResponse nimbus = nimbusService.createNimbus(body.getNimbusName(), principal.getName());
        //Bug recursive Sending Data(Sol'n: Create DTO or annonation for JSON in each class)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Nimbus created", nimbus));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteNimbus(@PathVariable("id") Long id, Principal principal){
        nimbusService.deleteNimbus(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Success deleted the nimbus "));
    }
}
