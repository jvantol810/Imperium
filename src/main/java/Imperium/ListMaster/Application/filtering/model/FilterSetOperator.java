package Imperium.ListMaster.Application.filtering.model;

public enum FilterSetOperator {
    AND("AND"),
    OR("OR");

    private final String value;

    FilterSetOperator(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
