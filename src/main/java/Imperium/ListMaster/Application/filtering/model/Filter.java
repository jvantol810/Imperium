package Imperium.ListMaster.Application.filtering.model;

import lombok.Data;

@Data
public class Filter {
    private String field;
    private FilterComparator comparator;
    private String comparedValue;
}
