package Imperium.ListMaster.Application.list;

import Imperium.ListMaster.Application.listview.ListViewService;
import Imperium.ListMaster.Application.sorting.model.ListItemSorting;
import Imperium.ListMaster.Application.filtering.model.Filter;
import Imperium.ListMaster.Application.list.model.ToDoListItem;
import Imperium.ListMaster.Application.sorting.model.SortingMethod;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ListPageController {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Autowired
    private ListService listService;

    @Autowired
    private ListViewService listViewService;

    @GetMapping("/")
    public void getHomePage(
            HttpServletResponse response,
            @Value("${landing-page.list}") String list,
            @Value("${landing-page.view}") String view
    ) throws IOException {
        response.sendRedirect(String.format("/list-page?listName=%s&viewName=%s", list, view));
    }

    @GetMapping("/list-page")
    public String getListPage(
            @RequestParam String listName,
            @RequestParam String viewName,
            Model model
    ) throws IOException {
        final List<ToDoListItem> listItems = listService.applyViewToList(listName, viewName);
        final List<List<ToDoListItem>> lists = Arrays.asList(listItems);
        final List<String> titles = Arrays.asList(viewName);

        // Apply the view to the list
        model.addAttribute("currentListName", listName);
        model.addAttribute("allListNames", listService.getAllListNames());

        model.addAttribute("currentListViewName", viewName);
        model.addAttribute("allListViewNames", listViewService.getAllViewNamesForList(listName));

        model.addAttribute("itemLists", lists);
        model.addAttribute("titles", titles);

        return "list";
    }


    @GetMapping("/old-list-page")
    public String getOldListPage(@RequestParam String name,
                              @RequestParam(required = false) Filter filter,
                              @RequestParam(required = false) String sortingField,
                              @RequestParam(required = false) SortingMethod sortingMethod,
                              Model model) throws IOException {
        List<ToDoListItem> allItems = listService.loadList(name + ".yaml");
        if (filter != null && filter.getField() != null && filter.getComparedValue() != null) {
            allItems = listService.filterList(allItems, filter);
        }

        // If sorting data was passed in, try to sort the list
        if (sortingMethod != null && sortingField != null) {
            final ListItemSorting sorting = new ListItemSorting(sortingField, sortingMethod);
            allItems = listService.sortList(allItems, sorting);
        }

        List<ToDoListItem> nonCompletedItems = allItems.stream()
                .filter(item -> !item.isCompleted())
                .collect(Collectors.toList());
        List<ToDoListItem> completedItems = allItems.stream()
                .filter(ToDoListItem::isCompleted)
                .collect(Collectors.toList());

        List<List<ToDoListItem>> itemLists = Arrays.asList(nonCompletedItems, completedItems);
        List<String> titles = Arrays.asList("To-Do", "Completed");

        model.addAttribute("listName", name);
        model.addAttribute("itemLists", itemLists);
        model.addAttribute("titles", titles);

        return "list";
    }
}
