package Imperium.ListMaster.Application.sorting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListItemSorting {
    private String field;
    private SortingMethod sortingMethod;
}
