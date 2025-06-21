package Imperium.ListMaster.Application.data.filters;

import lombok.Data;

@Data
public class ListItemFilter {
    private String field;
    private FilterComparator comparator;
    private String comparedValue;
}
