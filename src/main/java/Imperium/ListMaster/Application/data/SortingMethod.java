package Imperium.ListMaster.Application.data;

public enum SortingMethod {
    ASCENDING("ASCENDING"),
    DESCENDING("DESCENDING");

    private final String value;

    SortingMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
