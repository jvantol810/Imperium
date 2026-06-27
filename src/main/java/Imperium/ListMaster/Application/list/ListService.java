package Imperium.ListMaster.Application.list;

import Imperium.ListMaster.Application.filtering.FilterService;
import Imperium.ListMaster.Application.filtering.model.FilterSet;
import Imperium.ListMaster.Application.filtering.model.Filter;
import Imperium.ListMaster.Application.list.model.ToDoListItem;
import Imperium.ListMaster.Application.listview.ListViewService;
import Imperium.ListMaster.Application.listview.model.ListView;
import Imperium.ListMaster.Application.sorting.SortingService;
import Imperium.ListMaster.Application.sorting.model.ListItemSorting;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ListService {

    private static final Logger logger = LoggerFactory.getLogger(ListService.class);

    @Value("${paths.appdata}")
    private String dataDirectory;

    private final FilterService filterService;
    private final SortingService sortingService;
    private final ListViewService listViewService;

    public ListService(
            FilterService pFilterService,
            SortingService pSortingService,
            ListViewService pListViewService)
    {
        filterService = pFilterService;
        sortingService = pSortingService;
        listViewService = pListViewService;
    }

    public List<ToDoListItem> loadList(String fileName) throws IOException {
        logger.info("Loading list from file: {}", fileName);
        File file = new File(dataDirectory, fileName);
        ObjectMapper mapper = new YAMLMapper(new YAMLFactory());
        List<ToDoListItem> list = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, ToDoListItem.class));
        logger.info("Loaded {} items from file: {}", list.size(), fileName);
        return list;
    }

    public List<String> getAllListNames() throws IOException {
        File directory = new File(dataDirectory);
        List<String> listNames = new ArrayList<>();

        File[] files = directory.listFiles();
        if (files == null) {
            // directory doesn't exist or isn't a directory
            return listNames;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".yaml")) {
                listNames.add(stripExtension(file.getName()));
            }
        }

        return listNames;
    }

    private String stripExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    public List<ToDoListItem> applyViewToList(String listName, String viewName) throws IOException {
        final ListView listView = listViewService.loadListView(listName, viewName);
        final List<ToDoListItem> listItems = loadList(listName + ".yaml");

        // Apply filters from the view
        List<Object> objectList = (List<Object>) (List<?>) listItems;

        for (FilterSet filterSet : listView.getFilterSets()) {
            objectList = filterService.applyFilterSet(objectList, filterSet);
        }

        final List<ToDoListItem> filteredItems = objectList.stream()
                .map(item -> (ToDoListItem) item)
                .toList();

        return sortList(filteredItems, listView.itemSorting);
    }

    public List<ToDoListItem> filterList(List<ToDoListItem> pList, Filter pFilter) throws IOException {
        // Implement filtering logic based on the property and value
        List<Object> objectList = (List<Object>) (List<?>) pList;

        List<Object> filteredObjects = filterService.filterList(objectList, pFilter);

        return filteredObjects.stream()
                .map(item -> (ToDoListItem) item)
                .toList();
    }

    public List<ToDoListItem> sortList(List<ToDoListItem> pList, ListItemSorting pListItemSorting) {
        return sortingService.sortList(pList, ToDoListItem.class, pListItemSorting);
    }

    public List<ToDoListItem> filterBySavedFilterSet(List<ToDoListItem> pList, String pFilterSetName) {
        // Implement filtering logic based on the property and value
        List<Object> objectList = (List<Object>) (List<?>) pList;

        List<Object> filteredObjects = filterService.applySavedFilterSet(objectList, pFilterSetName);

        return filteredObjects.stream()
                .map(item -> (ToDoListItem) item)
                .toList();
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

        final List<ToDoListItem> list = loadList(fileName);
        if(newItem.getId() == null || newItem.getId().toString().isEmpty()) {
            logger.info("No id for item, creating one.");
            newItem.setId(UUID.randomUUID());
        }

        newItem.setCreationTime(new Date());

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

        // If the completed
        if (!currentItem.isCompleted() && updatedItem.isCompleted()) {
            completeItem(currentItem);
        }
        else { currentItem.setCompleted(updatedItem.isCompleted()); }
    }

    private void completeItem(ToDoListItem item) {
        item.setCompleted(true);
        item.setCompletedAt(new Date());
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