/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.work.swagger.controller;

import com.work.swagger.dto.ResponseDTO;
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
public class SwaggerController {

    @GetMapping(path = "/hello", produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<ResponseDTO<String>> hello(@RequestParam("name") String name) {
        ResponseDTO<String> response = new ResponseDTO<>();
        response.setContent("Hi " + (name == null ? "" : name));
        response.setMessage(HttpStatus.OK.getReasonPhrase());
        return ResponseEntity.ok(response);
    }
}
