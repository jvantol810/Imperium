package Imperium.ListMaster.Application.data;

import lombok.Data;

@Data
public class ListItemSorting {
    private String field;
    private SortingMethod sortingMethod;
}
