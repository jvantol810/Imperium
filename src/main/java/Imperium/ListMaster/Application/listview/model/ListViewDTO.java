package Imperium.ListMaster.Application.listview.model;

import Imperium.ListMaster.Application.filtering.model.Filter;
import Imperium.ListMaster.Application.sorting.model.ListItemSorting;
import lombok.Data;

import java.util.List;

@Data
public class ListViewDTO {
    public String name;
    public String listName;
    public ListItemSorting itemSorting;
    public List<Filter> filters;
    public List<String> filterSetNames;
}
