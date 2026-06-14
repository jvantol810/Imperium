package Imperium.ListMaster.Application.data.filters;

import lombok.Data;

import java.util.List;

@Data
public class FilterSet {
    String name;
    String listName;
    List<ListItemFilter> filters;
}
