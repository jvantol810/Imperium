package Imperium.ListMaster.Application.sorting.model;

import lombok.Getter;

@Getter
public enum SortingMethod {
    ASCENDING("ASCENDING"),
    DESCENDING("DESCENDING");

    private final String value;

    SortingMethod(String value) {
        this.value = value;
    }
}
