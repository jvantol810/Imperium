package Imperium.ListMaster.Application.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import Imperium.ListMaster.Application.list.model.ToDoListItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ListRestController {

    @Autowired
    private ListService listService;

    @GetMapping("/list")
    public List<ToDoListItem> getList(
            @RequestParam String name,
            @RequestParam(required = false) String filterSet
    ) throws IOException {
        List<ToDoListItem> list = listService.loadList(name + ".yaml");
        if (filterSet != null) {
            list = listService.filterBySavedFilterSet(list, filterSet);
        }
        return list;
    }

    @GetMapping("/list/{listName}/filterable-fields")
    public List<String> getFilterableFieldsForList(@RequestParam String name) throws IOException {
        List<ToDoListItem> list = listService.loadList(name + ".yaml");
//        Arrays.stream(ToDoListItem.class.getDeclaredFields())
//                .map(Field::getName)
//                .collect(Collectors.toList());
        return new ArrayList<>();
    }

    @GetMapping("/list/{listName}/item/{id}")
    public ToDoListItem getItemById(@PathVariable String listName, @PathVariable String id) throws IOException {
        return listService.getItemById(listName + ".yaml", id);
    }

    @PutMapping("/list/{listName}/item/{id}")
    public void updateItem(@PathVariable String listName,
                           @PathVariable String id,
                           @RequestBody ToDoListItem updatedItem) throws IOException {
        updatedItem.setId(UUID.fromString(id));
        listService.updateItem(listName + ".yaml", updatedItem);
    }

    @PostMapping("/list/{listName}/item")
    public void createItem(@PathVariable String listName, @RequestBody ToDoListItem newItem) throws IOException {
        listService.createItem(listName + ".yaml", newItem);
    }

    @DeleteMapping("/list/{listName}/item/{id}")
    public void deleteItem(@PathVariable String listName, @PathVariable String id) throws IOException {
        listService.deleteItem(listName + ".yaml", id);
    }

}
