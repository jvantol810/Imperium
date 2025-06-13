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


    public Map<String, Class> getAllFilterableFieldsFromClass(Class clazz) {
        final Field[] fields = clazz.getDeclaredFields();  // only fields declared in this class
        final Map<String, Class> fieldTypeMap = new HashMap<>();

        for (Field field : fields) {
            System.out.println("Field: " + field.getName() + ", Type: " + field.getType().getSimpleName());
            fieldTypeMap.put(field.getName(), field.getType());
        }
        return fieldTypeMap;
    }

}
