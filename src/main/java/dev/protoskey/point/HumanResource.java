package dev.protoskey.point;

import dev.protoskey.entity.Human;
import dev.protoskey.exception.EntityNotFoundException;
import dev.protoskey.exception.ReplacementRequiredException;
import dev.protoskey.exception.SameObjectException;
import dev.protoskey.exception.ValidationException;
import dev.protoskey.mapper.CityMapper;
import dev.protoskey.mapper.HumanMapper;
import dev.protoskey.model.CityDto;
import dev.protoskey.model.HumanDto;
import dev.protoskey.service.ReferenceService;
import dev.protoskey.validator.HumanValidator;
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

@Path("/humans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HumanResource {
    @Inject
    private ReferenceService referenceService;

    @Inject
    private HumanMapper humanMapper;

    @Inject
    private CityMapper cityMapper;

    @Inject
    private HumanValidator humanValidator;

    @GET
    public List<HumanDto> all() {
        return referenceService.findAllHumans().stream().map(humanMapper::toDto).toList();
    }

    @GET
    @Path("/{id}/cities")
    public List<CityDto> linkedCities(@PathParam("id") Integer id) {
        return referenceService.citiesWithGovernor(id).stream().map(cityMapper::toDto).toList();
    }

    @POST
    public Response create(HumanDto dto) throws ValidationException {
        humanValidator.validate(dto);
        Human created = referenceService.createHuman(humanMapper.toEntity(dto));
        return Response.status(Response.Status.CREATED).entity(humanMapper.toDto(created)).build();
    }

    @PUT
    @Path("/{id}")
    public HumanDto update(@PathParam("id") Integer id, HumanDto dto)
            throws ValidationException, EntityNotFoundException {
        humanValidator.validate(dto);
        Human updated = referenceService.updateHuman(id, humanMapper.toEntity(dto));
        return humanMapper.toDto(updated);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id,
                           @QueryParam("replacementId") Integer replacementId)
            throws ValidationException, EntityNotFoundException, ReplacementRequiredException, SameObjectException {
        referenceService.deleteHuman(id, replacementId);
        return Response.noContent().build();
    }
}
