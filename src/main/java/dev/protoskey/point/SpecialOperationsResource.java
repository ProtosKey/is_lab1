package dev.protoskey.point;

import dev.protoskey.entity.City;
import dev.protoskey.entity.Climate;
import dev.protoskey.exception.*;
import dev.protoskey.mapper.CityMapper;
import dev.protoskey.model.CityDto;
import dev.protoskey.service.SpecialOperationsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Path("/special")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SpecialOperationsResource {
    @Inject
    private SpecialOperationsService specialOperations;

    @Inject
    private CityMapper cityMapper;

    @DELETE
    @Path("/by-climate")
    public CityDto deleteOneByClimate(@QueryParam("climate") String climate)
            throws NoCityWithClimateException, ValidationException {
        return cityMapper.toDto(specialOperations.deleteOneByClimate(parseClimate(climate)));
    }

    @GET
    @Path("/count-by-meters")
    public Map<String, Long> countByMeters(@QueryParam("metersAboveSeaLevel") Float meters)
            throws ValidationException {
        if (meters == null) {
            throw single("metersAboveSeaLevel", "Укажите высоту над уровнем моря");
        }
        return Map.of("count", specialOperations.countByMetersAboveSeaLevel(meters));
    }

    @GET
    @Path("/by-name-part")
    public List<CityDto> byNamePart(@QueryParam("part") String part) throws ValidationException {
        if (part == null || part.isBlank()) {
            throw single("part", "Укажите подстроку для поиска");
        }
        return specialOperations.findByNameContains(part).stream().map(cityMapper::toDto).toList();
    }

    @POST
    @Path("/move-population")
    public CityDto movePopulation(@QueryParam("sourceId") Integer sourceId,
                                  @QueryParam("targetId") Integer targetId)
            throws ValidationException, SameObjectException, EntityNotFoundException, PopulationOverflowException {
        if (sourceId == null) {
            throw single("sourceId", "Укажите город-источник");
        }
        if (targetId == null) {
            throw single("targetId", "Укажите город-получатель");
        }
        return cityMapper.toDto(specialOperations.movePopulation(sourceId, targetId));
    }

    @POST
    @Path("/move-population-to-smallest")
    public CityDto moveToSmallest(@QueryParam("sourceId") Integer sourceId)
            throws ValidationException, EntityNotFoundException, PopulationOverflowException, NoCityToMoveException {
        if (sourceId == null) {
            throw single("sourceId", "Укажите город-источник");
        }
        City target = specialOperations.movePopulationToSmallest(sourceId);
        return cityMapper.toDto(target);
    }

    private Climate parseClimate(String value) throws ValidationException {
        boolean known = value != null
                && Arrays.stream(Climate.values()).anyMatch(v -> v.name().equals(value));
        if (!known) {
            throw single("climate", "Выберите климат");
        }
        return Climate.valueOf(value);
    }

    private ValidationException single(String field, String message) {
        InvalidFieldException invalid = new InvalidFieldException(field, message);
        return new ValidationException(Map.of(invalid.getField(), invalid.getMessage()));
    }
}
