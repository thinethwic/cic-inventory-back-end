package com.cic.inventory.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class AbstractController {
    protected <T> ResponseEntity<T> sendOkResponse(T response) {
        return ResponseEntity.ok(response);
    }

    protected <T> ResponseEntity<T> sendCreatedResponse(T response) {
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    protected <T> ResponseEntity<T> sendNotFoundResponse() {
        return ResponseEntity.notFound().build();
    }

    protected <T> ResponseEntity<T> sendNoContentResponse() {
        return ResponseEntity.noContent().build();
    }
}
