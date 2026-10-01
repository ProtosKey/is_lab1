package dev.protoskey.point;

import dev.protoskey.exception.AuthenticationException;
import dev.protoskey.exception.EntityNotFoundException;
import dev.protoskey.exception.ValidationException;
import dev.protoskey.model.ErrorDto;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApplicationExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof ValidationException validation) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorDto(validation.getMessage(), validation.getFieldErrors()))
                    .build();
        }

        Response.Status status = switch (exception) {
            case EntityNotFoundException ignored -> Response.Status.NOT_FOUND;
            case AuthenticationException ignored -> Response.Status.UNAUTHORIZED;
            default -> Response.Status.BAD_REQUEST;
        };

        return Response.status(status).entity(new ErrorDto(exception.getMessage())).build();
    }
}
