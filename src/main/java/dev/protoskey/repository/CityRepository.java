package dev.protoskey.repository;

import dev.protoskey.entity.City;
import dev.protoskey.entity.Climate;
import dev.protoskey.entity.StandardOfLiving;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class CityRepository {
    private static final Map<String, String> SORT_FIELDS = Map.ofEntries(
            Map.entry("id", "c.id"),
            Map.entry("name", "c.name"),
            Map.entry("coordinatesX", "c.coordinates.x"),
            Map.entry("coordinatesY", "c.coordinates.y"),
            Map.entry("creationDate", "c.creationDate"),
            Map.entry("area", "c.area"),
            Map.entry("population", "c.population"),
            Map.entry("establishmentDate", "c.establishmentDate"),
            Map.entry("capital", "c.capital"),
            Map.entry("metersAboveSeaLevel", "c.metersAboveSeaLevel"),
            Map.entry("populationDensity", "c.populationDensity"),
            Map.entry("climate", "c.climate"),
            Map.entry("standardOfLiving", "c.standardOfLiving"),
            Map.entry("governor", "c.governor.name")
    );

    private static final Set<String> FILTER_FIELDS =
            Set.of("name", "governor", "climate", "standardOfLiving");

    @PersistenceContext(unitName = "is_lab_1PU")
    private EntityManager em;

    public Optional<City> findById(Integer id) {
        return Optional.ofNullable(em.find(City.class, id));
    }

    public List<City> findAll() {
        return em.createQuery("select c from City c order by c.id", City.class).getResultList();
    }

    public List<City> findPage(CityQuery query) {
        String jpql = "select c from City c" + whereClause(query) + orderClause(query);
        TypedQuery<City> typedQuery = em.createQuery(jpql, City.class);
        applyFilter(typedQuery, query);
        return typedQuery
                .setFirstResult(query.offset())
                .setMaxResults(query.limit())
                .getResultList();
    }

    public long countAll(CityQuery query) {
        String jpql = "select count(c) from City c" + whereClause(query);
        TypedQuery<Long> typedQuery = em.createQuery(jpql, Long.class);
        applyFilter(typedQuery, query);
        return typedQuery.getSingleResult();
    }

    public List<City> findByNameContains(String part) {
        return em.createQuery(
                        "select c from City c where lower(c.name) like :part order by c.id", City.class)
                .setParameter("part", "%" + part.toLowerCase() + "%")
                .getResultList();
    }

    public long countByMetersAboveSeaLevel(Float value) {
        return em.createQuery(
                        "select count(c) from City c where c.metersAboveSeaLevel = :value", Long.class)
                .setParameter("value", value)
                .getSingleResult();
    }

    public Optional<City> findAnyByClimate(Climate climate) {
        return em.createQuery("select c from City c where c.climate = :climate", City.class)
                .setParameter("climate", climate)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    public Optional<City> findWithSmallestPopulation() {
        return em.createQuery("select c from City c order by c.population asc, c.id asc", City.class)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    public List<City> findByCoordinates(Integer coordinatesId) {
        return em.createQuery("select c from City c where c.coordinates.id = :id", City.class)
                .setParameter("id", coordinatesId)
                .getResultList();
    }

    public List<City> findByGovernor(Integer governorId) {
        return em.createQuery("select c from City c where c.governor.id = :id", City.class)
                .setParameter("id", governorId)
                .getResultList();
    }

    public City save(City city) {
        em.persist(city);
        return city;
    }

    public City update(City city) {
        return em.merge(city);
    }

    public void delete(City city) {
        em.remove(em.contains(city) ? city : em.merge(city));
    }

    private String whereClause(CityQuery query) {
        if (!hasFilter(query)) {
            return "";
        }
        return switch (query.filterField()) {
            case "name" -> " where lower(c.name) like :filter";
            case "governor" -> " where lower(c.governor.name) like :filter";
            case "climate", "standardOfLiving" -> matchingEnums(query).isEmpty()
                    ? " where 1 = 0"
                    : " where c." + query.filterField() + " in :filter";
            default -> "";
        };
    }

    private String orderClause(CityQuery query) {
        String path = SORT_FIELDS.getOrDefault(query.sortField(), "c.id");
        return " order by " + path + (query.ascending() ? " asc" : " desc");
    }

    private void applyFilter(TypedQuery<?> typedQuery, CityQuery query) {
        if (!hasFilter(query)) {
            return;
        }
        switch (query.filterField()) {
            case "name", "governor" ->
                    typedQuery.setParameter("filter", "%" + query.filterValue().toLowerCase() + "%");
            case "climate", "standardOfLiving" -> {
                List<? extends Enum<?>> values = matchingEnums(query);
                if (!values.isEmpty()) {
                    typedQuery.setParameter("filter", values);
                }
            }
            default -> { }
        }
    }

    private boolean hasFilter(CityQuery query) {
        return query.filterField() != null
                && FILTER_FIELDS.contains(query.filterField())
                && query.filterValue() != null
                && !query.filterValue().isBlank();
    }

    private List<? extends Enum<?>> matchingEnums(CityQuery query) {
        Enum<?>[] values = "climate".equals(query.filterField())
                ? Climate.values()
                : StandardOfLiving.values();
        String part = query.filterValue().toLowerCase();
        return Arrays.stream(values)
                .filter(value -> value.name().toLowerCase().contains(part))
                .toList();
    }
}
