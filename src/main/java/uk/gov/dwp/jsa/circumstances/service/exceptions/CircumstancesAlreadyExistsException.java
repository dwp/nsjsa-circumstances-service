package uk.gov.dwp.jsa.circumstances.service.exceptions;

import org.springframework.http.HttpStatus;

public class CircumstancesAlreadyExistsException extends RuntimeException {
    static final String CODE = HttpStatus.CONFLICT.toString();
    static final String MESSAGE = "Circumstances already exists";
}
