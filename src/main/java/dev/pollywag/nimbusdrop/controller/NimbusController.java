package dev.pollywag.nimbusdrop.controller;

import dev.pollywag.nimbusdrop.dto.respondeDTO.ApiResponse;
import dev.pollywag.nimbusdrop.dto.requestDTO.CreateNimbusRequest;
import dev.pollywag.nimbusdrop.dto.respondeDTO.DropResponse;
import dev.pollywag.nimbusdrop.dto.respondeDTO.NimbusResponse;
import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.service.DropService;
import dev.pollywag.nimbusdrop.service.NimbusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/user/nimbus")
public class NimbusController {
    private final NimbusService nimbusService;
    private final DropService dropService;

    public NimbusController(NimbusService nimbusService, DropService dropService) {
        this.nimbusService = nimbusService;
        this.dropService = dropService;
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

    @PostMapping("{id}/drops")
    public ResponseEntity<ApiResponse<DropResponse>> uploadDrop(@PathVariable Long id, @RequestParam("file") MultipartFile file, Principal principal) {
        DropResponse response = dropService.uploadDrop(id, file, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Successfully created the drop", response));
    }
}
