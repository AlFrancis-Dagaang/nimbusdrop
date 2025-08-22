package dev.pollywag.nimbusdrop.controller;

import dev.pollywag.nimbusdrop.dto.requestDTO.UpdateNimbusNameRequest;
import dev.pollywag.nimbusdrop.dto.respondeDTO.ApiResponse;
import dev.pollywag.nimbusdrop.dto.requestDTO.CreateNimbusRequest;
import dev.pollywag.nimbusdrop.dto.respondeDTO.DropResponse;
import dev.pollywag.nimbusdrop.dto.respondeDTO.NimbusResponse;
import dev.pollywag.nimbusdrop.entity.Drop;
import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.service.DropService;
import dev.pollywag.nimbusdrop.service.NimbusService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NimbusResponse>> updateNimbusName (@PathVariable("id") Long id, @RequestBody UpdateNimbusNameRequest request, Principal principal){
        NimbusResponse response = nimbusService.updateNimbusName(id, request.getNimbusName(), principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Nimbus updated", response));
    }

    @DeleteMapping("/{id}/empty")
    public ResponseEntity<ApiResponse<String>> emptyNimbus(@PathVariable("id") Long id, Principal principal){
        String response = nimbusService.emptyNimbus(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("{id}/drops")
    public ResponseEntity<ApiResponse<DropResponse>> uploadDrop(@PathVariable Long id, @RequestParam("file") MultipartFile file, Principal principal) {
        DropResponse response = dropService.uploadDrop(id, file, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Successfully created the drop", response));
    }

    @GetMapping("/drop/{dropId}/open")
    public ResponseEntity<Resource> openDrop(@PathVariable Long dropId, Principal principal) throws MalformedURLException {
        Drop drop = dropService.getDropById(dropId);
        Resource resource = dropService.openDrop(dropId, principal.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(drop.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + drop.getDropName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/drop/{id}")
    public ResponseEntity<ApiResponse<?>> deleteDrop(@PathVariable Long id, Principal principal) throws IOException {
        dropService.deleteDrop(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Successfully deleted the drop"));
    }

    @GetMapping("/drop/download/{userId}/{nimbusId}/{fileName}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long userId,
            @PathVariable Long nimbusId,
            @PathVariable String fileName,
            Principal principal) throws IOException {

        String dropKey = "user_" + userId + "/nimbus_" + nimbusId + "/" + fileName;

        Resource resource = dropService.downloadDropFile(dropKey, principal.getName(), userId);

        // Set headers so browser downloads the file
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/drop/{dropId}/copy-url")
    public ResponseEntity<ApiResponse<String>>createDropSharedLink(@PathVariable Long dropId, Principal principal) {
        String response = nimbusService.createShareLink(dropId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
