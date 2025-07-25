package Imperium.ListMaster.Application.services;

import Imperium.ListMaster.Application.data.ToDoListItem;
import Imperium.ListMaster.Application.data.filters.FilterComparator;
import Imperium.ListMaster.Application.data.filters.FilterSet;
import Imperium.ListMaster.Application.data.filters.ListItemFilter;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FilterService {

    private static final Logger logger = LoggerFactory.getLogger(FilterService.class);

    @Value("${filterSets.filePath}")
    private String filterSetsFile;

    public List<Object> applySavedFilterSet(List<Object> pList, String pFilterSetName) {
        try {
            FilterSet filterSet = loadFilterSet(pFilterSetName);
            for (ListItemFilter filter : filterSet.getFilters()) {
                pList = filterList(pList, filter);
            }
            return pList;
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
        List<FilterSet> filterSets = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, FilterSet.class));

        // Find the filter set with the matching name
        return filterSets.stream()
                .filter(filterSet -> pFilterSetName.equals(filterSet.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Filter set not found: " + pFilterSetName));
    }

    public List<Object> filterList(List<Object> pList, ListItemFilter pFilter) throws IOException {
        // Implement filtering logic based on the property and value
        final List<Object> list = pList;

        List<Object> filteredList = list.stream().filter(item -> {
            try {
                Field field = item.getClass().getDeclaredField(pFilter.getField());
                field.setAccessible(true);
                Object fieldValue = field.get(item);
                if(fieldValue == null) { return false; }
                return compareFieldToValue(fieldValue, pFilter.getComparedValue(), pFilter.getComparator());
//                return fieldValue.toString().equals(pFilter.getComparedValue());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        return filteredList;
    }

    private boolean compareFieldToValue(Object field, String value, FilterComparator comparator) {
        // If the field is a LocalDateTime, return the comparison result
        if (field instanceof LocalDateTime fieldDateTime) {
            return compareDates(fieldDateTime,
                                LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                comparator);
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

    private boolean compareDates(LocalDateTime dateTime, LocalDateTime otherDateTime, FilterComparator comparator) {
        return switch (comparator) {
            case GREATER -> dateTime.isAfter(otherDateTime);
            case LESS -> dateTime.isBefore(otherDateTime);
            case EQUALS -> dateTime.isEqual(otherDateTime);
            default -> false;
        };
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
