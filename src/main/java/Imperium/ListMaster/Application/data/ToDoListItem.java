package Imperium.ListMaster.Application.data;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class ToDoListItem {
    UUID id;
    String title;
    String description;
    boolean completed;
    Date creationTime;
    Date dueDate;
}
