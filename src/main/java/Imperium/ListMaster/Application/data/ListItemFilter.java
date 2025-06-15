package Imperium.ListMaster.Application.data;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Data
public class ListItemFilter {
    private String field;
    private FilterComparator comparator;
    private String comparedValue;
}
