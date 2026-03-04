package com.cic.inventory.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InventoryException extends RuntimeException {
    private final HttpStatus status;
    public InventoryException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
