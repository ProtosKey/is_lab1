package dev.protoskey.point;

import dev.protoskey.entity.City;
import dev.protoskey.exception.EntityNotFoundException;
import dev.protoskey.exception.ValidationException;
import dev.protoskey.mapper.CityMapper;
import dev.protoskey.model.CityDto;
import dev.protoskey.model.PageDto;
import dev.protoskey.repository.CityQuery;
import dev.protoskey.service.CityService;
import dev.protoskey.validator.CityValidator;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/cities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CityResource {
    private static final int MAX_PAGE_SIZE = 100;

    @Inject
    private CityService cityService;

    @Inject
    private CityMapper cityMapper;

    @Inject
    private CityValidator cityValidator;

    @GET
    public PageDto<CityDto> page(@QueryParam("filterField") String filterField,
                                 @QueryParam("filterValue") String filterValue,
                                 @QueryParam("sortField") @DefaultValue("id") String sortField,
                                 @QueryParam("ascending") @DefaultValue("true") boolean ascending,
                                 @QueryParam("page") @DefaultValue("1") int page,
                                 @QueryParam("pageSize") @DefaultValue("10") int pageSize)
            throws ValidationException {
        validatePaging(page, pageSize);
        CityQuery query = new CityQuery(filterField, filterValue, sortField, ascending,
                (page - 1) * pageSize, pageSize);

        List<CityDto> items = cityService.findPage(query).stream().map(cityMapper::toDto).toList();
        return new PageDto<>(items, cityService.count(query));
    }

    @GET
    @Path("/{id}")
    public CityDto byId(@PathParam("id") Integer id) throws EntityNotFoundException {
        return cityMapper.toDto(cityService.findById(id));
    }

    @POST
    public Response create(CityDto dto)
            throws ValidationException, EntityNotFoundException {
        cityValidator.validate(dto);
        City created = cityService.create(cityMapper.toEntity(dto),
                dto.getCoordinatesId(), dto.getGovernorId());
        return Response.status(Response.Status.CREATED).entity(cityMapper.toDto(created)).build();
    }

    @PUT
    @Path("/{id}")
    public CityDto update(@PathParam("id") Integer id, CityDto dto)
            throws ValidationException, EntityNotFoundException {
        cityValidator.validate(dto);
        City updated = cityService.update(id, cityMapper.toEntity(dto),
                dto.getCoordinatesId(), dto.getGovernorId());
        return cityMapper.toDto(updated);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) throws EntityNotFoundException {
        cityService.delete(id);
        return Response.noContent().build();
    }

    private void validatePaging(int page, int pageSize) throws ValidationException {
        Map<String, String> errors = new LinkedHashMap<>();
        if (page < 1) {
            errors.put("page", "Номер страницы должен быть не меньше 1");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            errors.put("pageSize", "Размер страницы должен быть от 1 до " + MAX_PAGE_SIZE);
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
