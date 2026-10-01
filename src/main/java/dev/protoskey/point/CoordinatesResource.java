package dev.protoskey.point;

import dev.protoskey.entity.Coordinates;
import dev.protoskey.exception.EntityNotFoundException;
import dev.protoskey.exception.ReplacementRequiredException;
import dev.protoskey.exception.SameObjectException;
import dev.protoskey.exception.ValidationException;
import dev.protoskey.mapper.CityMapper;
import dev.protoskey.mapper.CoordinatesMapper;
import dev.protoskey.model.CityDto;
import dev.protoskey.model.CoordinatesDto;
import dev.protoskey.service.ReferenceService;
import dev.protoskey.validator.CoordinatesValidator;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/coordinates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoordinatesResource {
    @Inject
    private ReferenceService referenceService;

    @Inject
    private CoordinatesMapper coordinatesMapper;

    @Inject
    private CityMapper cityMapper;

    @Inject
    private CoordinatesValidator coordinatesValidator;

    @GET
    public List<CoordinatesDto> all() {
        return referenceService.findAllCoordinates().stream().map(coordinatesMapper::toDto).toList();
    }

    @GET
    @Path("/{id}/cities")
    public List<CityDto> linkedCities(@PathParam("id") Integer id) {
        return referenceService.citiesWithCoordinates(id).stream().map(cityMapper::toDto).toList();
    }

    @POST
    public Response create(CoordinatesDto dto) throws ValidationException {
        coordinatesValidator.validate(dto);
        Coordinates created = referenceService.createCoordinates(coordinatesMapper.toEntity(dto));
        return Response.status(Response.Status.CREATED)
                .entity(coordinatesMapper.toDto(created))
                .build();
    }

    @PUT
    @Path("/{id}")
    public CoordinatesDto update(@PathParam("id") Integer id, CoordinatesDto dto)
            throws ValidationException, EntityNotFoundException {
        coordinatesValidator.validate(dto);
        Coordinates updated = referenceService.updateCoordinates(id, coordinatesMapper.toEntity(dto));
        return coordinatesMapper.toDto(updated);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id,
                           @QueryParam("replacementId") Integer replacementId)
            throws EntityNotFoundException, ReplacementRequiredException, SameObjectException {
        referenceService.deleteCoordinates(id, replacementId);
        return Response.noContent().build();
    }
}
