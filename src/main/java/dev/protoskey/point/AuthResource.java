package dev.protoskey.point;

import dev.protoskey.entity.User;
import dev.protoskey.exception.AuthenticationException;
import dev.protoskey.exception.LoginAlreadyTakenException;
import dev.protoskey.exception.ValidationException;
import dev.protoskey.model.CredentialsDto;
import dev.protoskey.service.AuthService;
import dev.protoskey.validator.CredentialsValidator;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    static final String SESSION_LOGIN = "login";

    @Inject
    private AuthService authService;

    @Inject
    private CredentialsValidator credentialsValidator;

    @Context
    private HttpServletRequest request;

    @POST
    @Path("/login")
    public Response login(CredentialsDto credentials)
            throws ValidationException, AuthenticationException {
        credentialsValidator.validate(credentials);
        User user = authService.login(credentials.getLogin(), credentials.getPassword());
        request.getSession(true).setAttribute(SESSION_LOGIN, user.getLogin());
        return Response.ok(Map.of("login", user.getLogin())).build();
    }

    @POST
    @Path("/register")
    public Response register(CredentialsDto credentials)
            throws ValidationException, LoginAlreadyTakenException {
        credentialsValidator.validate(credentials);
        User user = authService.register(credentials.getLogin(), credentials.getPassword());
        request.getSession(true).setAttribute(SESSION_LOGIN, user.getLogin());
        return Response.ok(Map.of("login", user.getLogin())).build();
    }

    @POST
    @Path("/logout")
    public Response logout() {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return Response.noContent().build();
    }

    @GET
    @Path("/me")
    public Response current() {
        Object login = request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute(SESSION_LOGIN);

        if (login == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.ok(Map.of("login", login.toString())).build();
    }
}
