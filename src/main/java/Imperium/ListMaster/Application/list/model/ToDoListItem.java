package Imperium.ListMaster.Application.list.model;

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
    String dueDate;
    Date completedAt;
}
