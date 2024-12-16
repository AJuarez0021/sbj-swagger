package com.work.swagger.controller;

import com.work.swagger.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author linux
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/v1")
@Slf4j
@Tag(name = "Swagger")
public class SwaggerController {

    @GetMapping(path = "/hello", produces = {MediaType.APPLICATION_JSON_VALUE})
    
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Results are ok", content = { @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ClassResponseDTO.class)) }),
        @ApiResponse(responseCode = "500", description = "An error occurred",
                content = @Content) })
    @Operation(operationId = "hello", summary = "Open api sample API")
    public ResponseEntity<ResponseDTO<String>> hello(@RequestParam("name") String name) {
        ResponseDTO<String> response = new ResponseDTO<>();
        response.setContent("Hi " + (name == null ? "" : name));
        response.setMessage(HttpStatus.OK.getReasonPhrase());
        return ResponseEntity.ok(response);
    }
    
    private class ClassResponseDTO extends ResponseDTO<String> {
        
    }
}
