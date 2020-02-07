package uk.gov.dwp.jsa.circumstances.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.circumstances.service.services.ResponseBuilder;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(CircumstancesAlreadyExistsException.class)
    public final @ResponseBody
    ResponseEntity<ApiResponse<String>> handlePSQLException(
            final Exception ex,
            final WebRequest request
    ) {
        return new ResponseBuilder<String>()
                .withStatus(HttpStatus.CONFLICT)
                .withApiError(
                        CircumstancesAlreadyExistsException.CODE,
                        CircumstancesAlreadyExistsException.MESSAGE
                ).build();
    }

}
