package dev.protoskey.point;

import dev.protoskey.exception.AuthenticationException;
import dev.protoskey.exception.EntityNotFoundException;
import dev.protoskey.exception.ValidationException;
import dev.protoskey.model.ErrorDto;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class ApplicationExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOG = Logger.getLogger(ApplicationExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof ValidationException validation) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorDto(validation.getMessage(), validation.getFieldErrors()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (isMalformedJson(exception)) {
            return error(Response.Status.BAD_REQUEST, "Некорректный формат данных");
        }

        if (exception instanceof WebApplicationException web) {
            Response.StatusType status = web.getResponse().getStatusInfo();
            return error(status, status.getReasonPhrase());
        }

        if (exception instanceof RuntimeException) {
            LOG.log(Level.SEVERE, "Непредвиденная ошибка", exception);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }

        Response.Status status = switch (exception) {
            case EntityNotFoundException ignored -> Response.Status.NOT_FOUND;
            case AuthenticationException ignored -> Response.Status.UNAUTHORIZED;
            default -> Response.Status.BAD_REQUEST;
        };

        return error(status, exception.getMessage());
    }

    private boolean isMalformedJson(Throwable exception) {
        for (Throwable t = exception; t != null; t = t.getCause()) {
            if (t instanceof JsonbException || t instanceof ProcessingException) {
                return true;
            }
        }
        return false;
    }

    private Response error(Response.StatusType status, String message) {
        return Response.status(status)
                .entity(new ErrorDto(message))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
