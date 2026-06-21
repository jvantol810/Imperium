package Imperium.ListMaster.Application.sorting;

import Imperium.ListMaster.Application.list.ListService;
import Imperium.ListMaster.Application.list.model.ToDoListItem;
import Imperium.ListMaster.Application.sorting.model.ListItemSorting;
import Imperium.ListMaster.Application.sorting.model.SortingMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Service
public class SortingService {
    private static final Logger logger = LoggerFactory.getLogger(SortingService.class);

    public <T> List<T> sortList(List<T> pList, Class<T> pListClass, ListItemSorting pListItemSorting) {
        logger.info("Sorting list with sorting method: {}", pListItemSorting.getSortingMethod());

        if (pListItemSorting.getField() == null || pListItemSorting.getSortingMethod() == null) {
            logger.warn("No sorting field or method provided, returning unsorted list.");
            return pList;
        }

        final String fieldName = pListItemSorting.getField();
        final SortingMethod sortingMethod = pListItemSorting.getSortingMethod();
        final List<T> sortableList = new ArrayList(pList);

        try {
            sortableList.sort((item1, item2) -> {
                try {
                    Field field = pListClass.getDeclaredField(fieldName);
                    field.setAccessible(true);

                    Object value1 = field.get(item1);
                    Object value2 = field.get(item2);
                    if (value2 == null) {
                        return -1; // nulls last
                    }
                    if (value1 instanceof Comparable && value2 instanceof Comparable) {
                        int comparison = ((Comparable) value1).compareTo(value2);
                        return sortingMethod == SortingMethod.ASCENDING ? comparison : -comparison;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    logger.error("Error accessing field: {}", fieldName, e);
                }
                return 0;
            });
        } catch (Exception e) {
            logger.error("Error sorting list by field: {}", fieldName, e);
        }
        return sortableList;
    }
}
