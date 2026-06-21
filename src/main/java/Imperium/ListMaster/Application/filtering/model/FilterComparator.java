package Imperium.ListMaster.Application.filtering.model;

public enum FilterComparator {
    EQUALS("EQUALS"),
    GREATER("GREATER"),
    CONTAINS("CONTAINS"),
    LESS("LESS");

    private final String value;

    FilterComparator(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
