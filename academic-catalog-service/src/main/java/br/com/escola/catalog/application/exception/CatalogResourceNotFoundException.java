package br.com.escola.catalog.application.exception;

import java.util.UUID;

public class CatalogResourceNotFoundException extends RuntimeException {


	private static final long serialVersionUID = 1L;

	public CatalogResourceNotFoundException(String resource, UUID id) {
        super(resource + " nao encontrado: " + id);
    }
}
