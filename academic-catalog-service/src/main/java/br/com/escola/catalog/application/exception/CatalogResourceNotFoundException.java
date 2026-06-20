package br.com.escola.catalog.application.exception;

import java.util.UUID;

public class CatalogResourceNotFoundException extends RuntimeException {

    public CatalogResourceNotFoundException(String resource, UUID id) {
        super(resource + " nao encontrado: " + id);
    }
}
