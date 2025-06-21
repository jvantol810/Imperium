package Imperium.ListMaster.Application.services;

import Imperium.ListMaster.Application.data.filters.FilterSet;
import Imperium.ListMaster.Application.data.filters.ListItemFilter;
import Imperium.ListMaster.Application.data.ListItemSorting;
import Imperium.ListMaster.Application.data.SortingMethod;
import Imperium.ListMaster.Application.data.ToDoListItem;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ListService {

    private static final Logger logger = LoggerFactory.getLogger(ListService.class);

    @Value("${list.rootFilePath}")
    private String dataDirectory;

    private final FilterService filterService;

    public ListService(FilterService pFilterService) {
        filterService = pFilterService;
    }

    public List<ToDoListItem> loadList(String fileName) throws IOException {
        logger.info("Loading list from file: {}", fileName);
        File file = new File(dataDirectory, fileName);
        ObjectMapper mapper = new YAMLMapper(new YAMLFactory());
        List<ToDoListItem> list = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, ToDoListItem.class));
        logger.info("Loaded {} items from file: {}", list.size(), fileName);
        return list;
    }

    public List<ToDoListItem> sortList(List<ToDoListItem> pList, ListItemSorting listItemSorting) throws IOException {
        logger.info("Sorting list with sorting method: {}", listItemSorting.getSortingMethod());
        if (listItemSorting.getField() == null || listItemSorting.getSortingMethod() == null) {
            logger.warn("No sorting field or method provided, returning unsorted list.");
            return pList;
        }

        String fieldName = listItemSorting.getField();
        SortingMethod sortingMethod = listItemSorting.getSortingMethod();

        try {
            pList.sort((item1, item2) -> {
                try {
                    Field field = ToDoListItem.class.getDeclaredField(fieldName);
                    field.setAccessible(true);

                    Object value1 = field.get(item1);
                    Object value2 = field.get(item2);
                    if (value2 == null) {
                        return -1; // nulls last
                    }
                    if (value1 instanceof Comparable && value2 instanceof Comparable) {
                        int comparison = ((Comparable) value1).compareTo(value2);
                        return sortingMethod == SortingMethod.ASCENDING ? comparison : -comparison;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    logger.error("Error accessing field: {}", fieldName, e);
                }
                return 0;
            });
        } catch (Exception e) {
            logger.error("Error sorting list by field: {}", fieldName, e);
        }
        return pList;
    }

    public List<ToDoListItem> filterList(List<ToDoListItem> pList, ListItemFilter pFilter) throws IOException {
        // Implement filtering logic based on the property and value
        List<Object> objectList = (List<Object>) (List<?>) pList;

        List<Object> filteredObjects = filterService.filterList(objectList, pFilter);

        return filteredObjects.stream()
                .map(item -> (ToDoListItem) item)
                .collect(Collectors.toList());
    }

    public List<ToDoListItem> filterBySavedFilterSet(List<ToDoListItem> pList, String pFilterSetName) throws IOException {
        // Implement filtering logic based on the property and value
        List<Object> objectList = (List<Object>) (List<?>) pList;

        List<Object> filteredObjects = filterService.applySavedFilterSet(objectList, pFilterSetName);

        return filteredObjects.stream()
                .map(item -> (ToDoListItem) item)
                .collect(Collectors.toList());
    }

    public void saveList(String fileName, List<ToDoListItem> list) throws IOException {
        logger.info("Saving list to file: {}", fileName);
        File file = new File(dataDirectory, fileName);
        ObjectMapper mapper = new YAMLMapper(new YAMLFactory());
        mapper.writeValue(file, list);
        logger.info("Saved {} items to file: {}", list.size(), fileName);
    }

    public ToDoListItem getItemById(String fileName, String id) throws IOException {
        logger.info("Getting item with id: {} from file: {}", id, fileName);
        List<ToDoListItem> list = loadList(fileName);
        UUID uuid = UUID.fromString(id);
        for (ToDoListItem item : list) {
            if (item.getId().equals(uuid)) {
                logger.info("Found item with id: {} in list", id);
                return item;
            }
        }
        logger.warn("Item with id: {} not found in list", id);
        return null;
    }

    public void createItem(String fileName, ToDoListItem newItem) throws IOException {
        logger.info("Creating new item in file: {}", fileName);
        List<ToDoListItem> list = loadList(fileName);
        if(newItem.getId() == null || newItem.getId().toString().isEmpty()) {
            logger.info("No id for item, creating one.");
            newItem.setId(UUID.randomUUID());
        }
        list.add(newItem);
        saveList(fileName, list);
        logger.info("Created new item with id: {} in list", newItem.getId());
    }

    public void updateItem(String fileName, ToDoListItem updatedItem) throws IOException {
        logger.info("Updating item with id: {} in file: {}", updatedItem.getId(), fileName);
        List<ToDoListItem> list = loadList(fileName);
        for (int i = 0; i < list.size(); i++) {
            ToDoListItem currentItem = list.get(i);
            if (currentItem.getId().equals(updatedItem.getId())) {
                updateNonNullAttributes(currentItem, updatedItem);
                list.set(i, currentItem);
                logger.info("Updated item with id: {} in list", updatedItem.getId());
                break;
            }
        }
        saveList(fileName, list);
    }

    private void updateNonNullAttributes(ToDoListItem currentItem, ToDoListItem updatedItem) {
        if (updatedItem.getTitle() != null && !updatedItem.getTitle().isEmpty()) {
            currentItem.setTitle(updatedItem.getTitle());
        }
        if (updatedItem.getDescription() != null && !updatedItem.getDescription().isEmpty()) {
            currentItem.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getDueDate() != null) {
            currentItem.setDueDate(updatedItem.getDueDate());
        }
        currentItem.setCompleted(updatedItem.isCompleted());
    }

    public void deleteItem(String fileName, String id) throws IOException {
        logger.info("Deleting item with id: {} from file: {}", id, fileName);
        List<ToDoListItem> list = loadList(fileName);
        UUID uuid = UUID.fromString(id);
        list.removeIf(item -> item.getId().equals(uuid));
        saveList(fileName, list);
        logger.info("Deleted item with id: {} from list", id);
    }
}