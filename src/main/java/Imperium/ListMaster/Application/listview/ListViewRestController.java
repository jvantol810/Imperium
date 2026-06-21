package Imperium.ListMaster.Application.listview;

import Imperium.ListMaster.Application.listview.model.ListView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/list-view")
public class ListViewRestController {

    @Autowired
    private ListViewService listViewService;

    @GetMapping
    public ListView getListView(@RequestParam String listName, @RequestParam String viewName) throws IOException {
        return listViewService.loadListView(listName, viewName);
    }
}
