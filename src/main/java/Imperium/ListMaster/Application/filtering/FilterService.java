package Imperium.ListMaster.Application.filtering;

import Imperium.ListMaster.Application.filtering.model.FilterComparator;
import Imperium.ListMaster.Application.filtering.model.FilterSet;
import Imperium.ListMaster.Application.filtering.model.Filter;
import Imperium.ListMaster.Application.filtering.model.FilterSetOperator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class FilterService {

    private static final Logger logger = LoggerFactory.getLogger(FilterService.class);

    @Value("${filterSets.filePath}")
    private String filterSetsFile;

    public List<FilterSet> getFilterSets(List<String> pFilterSetNames) throws IOException {
        logger.info("Loading filter sets: {}", pFilterSetNames);
        File file = new File(filterSetsFile);
        ObjectMapper mapper = new YAMLMapper(new YAMLFactory());

        // Read all filter sets from the YAML file
        return mapper.readValue(
                file,
                mapper.getTypeFactory().constructCollectionType(List.class, FilterSet.class)
        );
    }

    public List<Object> applyFilterSet(List<Object> pList, FilterSet pFilterSet) throws IOException {
        // Iterate through the map of filters and operators
        final Map<FilterSetOperator, List<Filter>> filterSetMap = pFilterSet.getSet();

        AtomicReference<List<Object>> filteredList = new AtomicReference<>(pList);

        filterSetMap.forEach((operator, set) ->
                filteredList.set(applyFiltersWithOperator(filteredList.get(), operator, set))
        );

        return filteredList.get();
    }

    private List<Object> applyFiltersWithOperator(List<Object> pList, FilterSetOperator pOperator, List<Filter> pFilters) {
        List<Object> filteredList = pList;

        if (pOperator.equals(FilterSetOperator.OR)) {
            filteredList = filteredList.stream().filter( item -> {
                // If the item meets NONE of the filters' criteria, remove it from the filteredList. Otherwise, keep it.
                int failedFilterCount = 0;

                for (Filter filter : pFilters) {
                    try {
                        Field field = item.getClass().getDeclaredField(filter.getField());
                        field.setAccessible(true);
                        Object fieldValue = field.get(item);
                        if (fieldValue == null) {
                            failedFilterCount++;
                        } else {
                            final boolean passedFilter = compareFieldToValue(fieldValue, filter.getComparedValue(), filter.getComparator());
                            if (!passedFilter) { failedFilterCount++; };
                        }
                    } catch (NoSuchFieldException | IllegalAccessException | ParseException e) {
                        throw new RuntimeException(e);
                    }
                }

                return failedFilterCount < pFilters.size();
            }).toList();
        }

        return filteredList;
    }

    public List<Object> applySavedFilterSet(List<Object> pList, String pFilterSetName) {
        try {
            final FilterSet filterSet = loadFilterSet(pFilterSetName);
            return applyFilterSet(pList, filterSet);
        } catch (IOException e) {
            logger.error("Error loading filter set: {}", pFilterSetName, e);
            return pList; // Return the original list if there's an error
        }
    }

    private FilterSet loadFilterSet(String pFilterSetName) throws IOException {
        logger.info("Loading filter set: {}", pFilterSetName);
        File file = new File(filterSetsFile);
        ObjectMapper mapper = new YAMLMapper(new YAMLFactory());

        // Read all filter sets from the YAML file
        List<FilterSet> filterSets = mapper.readValue(
                file,
                mapper.getTypeFactory().constructCollectionType(List.class, FilterSet.class)
        );

        // Find the filter set with the matching name
        return filterSets.stream()
                .filter(filterSet -> pFilterSetName.equals(filterSet.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Filter set not found: " + pFilterSetName));
    }

    public List<Object> filterList(List<Object> pList, Filter pFilter) throws IOException {
        // Implement filtering logic based on the property and value
        final List<Object> list = pList;

        List<Object> filteredList = list.stream().filter(item -> {
            try {
                Field field = item.getClass().getDeclaredField(pFilter.getField());
                field.setAccessible(true);
                Object fieldValue = field.get(item);
                if (fieldValue == null) { return false; }
                return compareFieldToValue(fieldValue, pFilter.getComparedValue(), pFilter.getComparator());
//                return fieldValue.toString().equals(pFilter.getComparedValue());
            } catch (NoSuchFieldException | IllegalAccessException | ParseException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        return filteredList;
    }

    private boolean compareFieldToValue(Object field, String value, FilterComparator comparator) throws ParseException {
        // If the field is a LocalDateTime, return the comparison result
        if (field instanceof Date fieldDateTime) {
            return compareDates(fieldDateTime, value, comparator);
        }

        // Check if the field is a raw int or other numeric type
        if (field instanceof Number) {
            try {
                int fieldValue = ((Number) field).intValue();
                int comparedValue = Integer.parseInt(value);

                return compareInts(fieldValue, comparedValue, comparator);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format for comparison value: " + value, e);
            }
        }

        // Check if the field is a String
        if (field instanceof String) {
            String fieldValue = (String) field;

            // For string comparison, we can use equals or contains based on the comparator
            switch (comparator) {
                case EQUALS:
                    return fieldValue.equals(value);
                case CONTAINS:
                    return fieldValue.contains(value);
                default:
                    return false; // Unsupported comparator for String
            }
        }

        // Check if the field is a Boolean
        if (field instanceof Boolean) {
            boolean fieldValue = (Boolean) field;
            boolean comparedValue = Boolean.parseBoolean(value);

            // For boolean comparison, we can only check equality
            if (comparator == FilterComparator.EQUALS) {
                return fieldValue == comparedValue;
            }
            return false; // Unsupported comparator for Boolean
        }

        return false;
    }

    private boolean compareInts(int number, int otherNumber, FilterComparator comparator) {
        return switch (comparator) {
            case GREATER -> number > otherNumber;
            case LESS -> number < otherNumber;
            case EQUALS -> number == otherNumber;
            default -> false;
        };
    }

    private boolean compareDates(Date date, String otherDateString, FilterComparator comparator) {
        switch (otherDateString) {
            case "today":
                return compareDatesIgnoringTime(date, new Date(), comparator);
            case "tomorrow":
                return compareDatesIgnoringTime(date, addDays(new Date(), 1), comparator);
            case "yesterday":
                return compareDatesIgnoringTime(date, addDays(new Date(), -1), comparator);
        }

        final Instant dateInstant = date.toInstant();
        final Instant otherDateInstant = Instant.parse(otherDateString);

        return switch (comparator) {
            case GREATER -> dateInstant.isAfter(otherDateInstant);
            case LESS -> dateInstant.isBefore(otherDateInstant);
            case EQUALS -> dateInstant.equals(otherDateInstant);
            default -> false;
        };
    }

    private boolean compareDatesIgnoringTime(Date date, Date otherDate, FilterComparator comparator) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localOtherDate = otherDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return switch (comparator) {
            case GREATER -> localDate.isAfter(localOtherDate);
            case LESS -> localDate.isBefore(localOtherDate);
            case EQUALS -> localDate.equals(localOtherDate);
            default -> false;
        };
    }

    private static Date addDays(Date date, int days) {
        ZonedDateTime zdt = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .plusDays(days);
        return Date.from(zdt.toInstant());
    }

    private boolean isSameDay(Date date1, Date date2) {
        LocalDate localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate1.equals(localDate2);
    }

    private Map<String, Class> getAllFilterableFieldsFromClass(Class clazz) {
        final Field[] fields = clazz.getDeclaredFields();  // only fields declared in this class
        final Map<String, Class> fieldTypeMap = new HashMap<>();

        for (Field field : fields) {
            System.out.println("Field: " + field.getName() + ", Type: " + field.getType().getSimpleName());
            fieldTypeMap.put(field.getName(), field.getType());
        }
        return fieldTypeMap;
    }

}
