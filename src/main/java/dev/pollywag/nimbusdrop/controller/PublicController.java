package dev.pollywag.nimbusdrop.controller;

import dev.pollywag.nimbusdrop.dto.respondeDTO.ApiResponse;
import dev.pollywag.nimbusdrop.service.DropSharedLinkService;
import org.springframework.core.io.Resource;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.security.Principal;

@RestController
@RequestMapping("/public")
public class PublicController {

    private final DropSharedLinkService sharedLinkService;

    public PublicController( DropSharedLinkService sharedLinkService) {
        this.sharedLinkService = sharedLinkService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Resource>createDropSharedLink(@PathVariable String token) throws MalformedURLException {
        Resource resource = sharedLinkService.dropSharedLink(token);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
