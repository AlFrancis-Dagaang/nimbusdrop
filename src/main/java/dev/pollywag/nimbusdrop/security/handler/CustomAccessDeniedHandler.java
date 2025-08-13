package dev.pollywag.nimbusdrop.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pollywag.nimbusdrop.dto.respondeDTO.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    private ObjectMapper objectMapper;
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        // Build your JSON object
        ApiResponse<?> apiResponse = new ApiResponse<>(false, "Forbidden - you don't have permission", null);

        // Convert it to JSON and write to HTTP response
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
