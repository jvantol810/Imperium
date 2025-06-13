package Imperium.ListMaster.Application.controllers.pages;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import Imperium.ListMaster.Application.data.ListItemFilter;
import Imperium.ListMaster.Application.data.ToDoListItem;
import Imperium.ListMaster.Application.services.ListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ListController {

    @Autowired
    private ListService listService;

    @GetMapping("/list-page")
    public String getListPage(@RequestParam String name, ListItemFilter filter, Model model) throws IOException {
        List<ToDoListItem> allItems = listService.loadList(name + ".yaml");
        if (filter != null && filter.getField() != null && filter.getComparedValue() != null) {
            allItems = listService.filterList(allItems, filter);
        }

        List<ToDoListItem> nonCompletedItems = allItems.stream()
                .filter(item -> !item.isCompleted())
                .collect(Collectors.toList());
        List<ToDoListItem> completedItems = allItems.stream()
                .filter(ToDoListItem::isCompleted)
                .collect(Collectors.toList());

        List<List<ToDoListItem>> itemLists = Arrays.asList(nonCompletedItems, completedItems);
        List<String> titles = Arrays.asList("Non-Completed Items", "Completed Items");

        model.addAttribute("listName", name);
        model.addAttribute("itemLists", itemLists);
        model.addAttribute("titles", titles);
        return "list";
    }
}