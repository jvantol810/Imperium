package Imperium.ListMaster.Application.services;

import Imperium.ListMaster.Application.data.ListItemFilter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<ToDoListItem> filterList(List<ToDoListItem> pList, ListItemFilter pFilter) throws IOException {
        // Implement filtering logic based on the property and value
        List<Object> objectList = (List<Object>) (List<?>) pList;

        List<Object> filteredObjects = filterService.filterList(objectList, pFilter);

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