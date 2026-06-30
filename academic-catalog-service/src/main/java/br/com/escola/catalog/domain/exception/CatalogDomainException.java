package br.com.escola.catalog.domain.exception;

public class CatalogDomainException extends RuntimeException {


	private static final long serialVersionUID = 1L;

	public CatalogDomainException(String message) {
        super(message);
    }
}

