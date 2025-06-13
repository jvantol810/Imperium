package Imperium.ListMaster.Application;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import Imperium.ListMaster.Application.data.ToDoListItem;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

@Getter
@Setter
public class ImperiumList {
    private static final Logger logger = LoggerFactory.getLogger(ImperiumList.class);

    private final UUID id;
    private final String listName;
    private final String listFilePath;
    private final List<ToDoListItem> list;

    public ImperiumList(String listName, String listFilePath) throws IOException {
        this.id = UUID.randomUUID();
        this.listName = listName;
        this.listFilePath = listFilePath;
        this.list = deserializeYaml(listFilePath);
        logger.info("ImperiumList created with id: {}", this.id);
    }

    private List<ToDoListItem> deserializeYaml(String filePath) throws IOException {
        logger.info("Deserializing YAML file: {}", filePath);
        ObjectMapper mapper = new YAMLMapper(new YAMLFactory());
        List<ToDoListItem> items = mapper.readValue(new File(filePath), mapper.getTypeFactory().constructCollectionType(List.class, ToDoListItem.class));
        logger.info("Deserialization complete. Number of items: {}", items.size());
        return items;
    }

}
