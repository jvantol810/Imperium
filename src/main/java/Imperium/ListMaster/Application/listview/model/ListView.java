package Imperium.ListMaster.Application.listview.model;

import Imperium.ListMaster.Application.filtering.model.FilterSet;
import Imperium.ListMaster.Application.sorting.model.ListItemSorting;
import lombok.Data;

import java.util.List;

@Data
public class ListView {
    public String name;
    public String listName;
    public ListItemSorting itemSorting;
    public List<FilterSet> filterSets;
}
