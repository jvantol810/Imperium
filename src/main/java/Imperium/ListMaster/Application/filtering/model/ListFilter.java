package Imperium.ListMaster.Application.filtering.model;

import lombok.Data;

import java.util.List;

@Data
public class ListFilter {
    String listName;
    List<Filter> filters;
    List<FilterSet> sets;
}
