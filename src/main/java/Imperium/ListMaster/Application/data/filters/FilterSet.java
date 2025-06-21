package Imperium.ListMaster.Application.data.filters;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class FilterSet {
    String name;
    String listName;
    List<ListItemFilter> filters;
}
