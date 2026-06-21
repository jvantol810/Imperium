package Imperium.ListMaster.Application.filtering.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FilterSet {
    String name;
    String listName;
    Map<FilterSetOperator, List<Filter>> set;
}
