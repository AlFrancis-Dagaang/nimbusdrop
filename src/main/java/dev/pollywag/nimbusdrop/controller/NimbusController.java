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
import org.modelmapper.ModelMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user/nimbus")
public class NimbusController {
    private final NimbusService nimbusService;
    private final DropService dropService;
    private final ModelMapper modelMapper;


    public NimbusController(NimbusService nimbusService, DropService dropService, ModelMapper modelMapper) {
        this.nimbusService = nimbusService;
        this.dropService = dropService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<NimbusResponse>> createNimbus(@RequestBody CreateNimbusRequest request, Principal principal) {
        String nimbusName = request.getNimbusName();
        String email = principal.getName();

        Nimbus createdNimbus = nimbusService.createNimbus(nimbusName, email);

        NimbusResponse response= modelMapper.map(createdNimbus, NimbusResponse.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Nimbus created",response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteNimbus(@PathVariable("id") Long id, Principal principal){
        String email = principal.getName();
        nimbusService.deleteNimbus(id, email);
        return ResponseEntity.ok(ApiResponse.success("Success deleted the nimbus "));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NimbusResponse>> getNimbusById(@PathVariable("id") Long id, Principal principal) {
        String email = principal.getName();

        Nimbus nimbus = nimbusService.getNimbusById(id, email);

        NimbusResponse response = modelMapper.map(nimbus, NimbusResponse.class);

        return ResponseEntity.ok(ApiResponse.success("Nimbus found", response));
    }

    @GetMapping("/{id}/drops")
    public ResponseEntity<ApiResponse<List<DropResponse>>> getAllDropByNimbusId (@PathVariable Long id, Principal principal){
        String email = principal.getName();
        List <Drop> dropList = nimbusService.getAllDropByNimbusId(id, email);
        List<DropResponse> dropResponseList = dropList.stream()
                .map(drop -> {
                    DropResponse response = modelMapper.map(drop, DropResponse.class);
                    setDropUrl(response, drop);
                    return response;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Drops found", dropResponseList));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NimbusResponse>> updateNimbusName (@PathVariable("id") Long id, @RequestBody UpdateNimbusNameRequest request, Principal principal){
        String newNimbusName = request.getNimbusName();
        String email = principal.getName();

        Nimbus updatedNimbus = nimbusService.updateNimbusName(id, newNimbusName, email);

        NimbusResponse response= modelMapper.map(updatedNimbus, NimbusResponse.class);

        return ResponseEntity.ok(ApiResponse.success("Nimbus updated", response));
    }

    @DeleteMapping("/{id}/empty")
    public ResponseEntity<ApiResponse<String>> emptyNimbus(@PathVariable("id") Long id, Principal principal){
        nimbusService.emptyNimbus(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Successfully empty nimbus "));
    }

    @PostMapping("{id}/drops")
    public ResponseEntity<ApiResponse<DropResponse>> uploadDrop(@PathVariable Long id, @RequestParam("file") MultipartFile file, Principal principal) throws IOException {
        Drop uploadedDrop = dropService.uploadDrop(id, file, principal.getName());
        DropResponse response= modelMapper.map(uploadedDrop, DropResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Successfully created the drop", response));
    }

    @GetMapping("/drop/{id}")
    public ResponseEntity<ApiResponse<DropResponse>> getDrop (@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        Drop drop = dropService.getDropById(id, email);
        DropResponse response= modelMapper.map(drop, DropResponse.class);
        setDropUrl(response, drop);
        return ResponseEntity.ok(ApiResponse.success("Drop found", response));
    }

    @GetMapping("/drop/{dropId}/open")
    public ResponseEntity<Resource> openDrop(@PathVariable Long dropId, Principal principal) throws MalformedURLException {
        String email = principal.getName();

        Drop drop = dropService.getDropById(dropId, email);
        Resource resource = dropService.openDrop(dropId, email);

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

        Resource resource = dropService.downloadDropFile(dropKey, principal.getName(),nimbusId);

        // Set headers so browser downloads the file
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/drop/{dropId}/copy-url")
    public ResponseEntity<ApiResponse<?>>createDropSharedLink(@PathVariable Long dropId, Principal principal) {
        String response = nimbusService.createShareLink(dropId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    public void setDropUrl(DropResponse response, Drop drop){
        Long userId = drop.getNimbus().getUser().getId();
        Long nimbusId = drop.getNimbus().getId();
        String fileName = drop.getDropKey();

        String[] parts = fileName.split("/");
        fileName = parts[parts.length - 1];

        String url = "/drop/download/" + userId + "/" + nimbusId + "/" + fileName;
        response.setUrl(url);
    }
}
