package Imperium.ListMaster.Application.listview;

import Imperium.ListMaster.Application.filtering.FilterService;
import Imperium.ListMaster.Application.listview.model.ListView;
import Imperium.ListMaster.Application.listview.model.ListViewDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class ListViewService {
    private static final Logger logger = LoggerFactory.getLogger(ListViewService.class);

    @Value("${paths.list-view}")
    private String listViewDirectory;

    @Autowired
    private FilterService filterService;

    public List<String> getAllViewNamesForList(String listName) throws IOException {
        final List<String> allViewNames = new ArrayList<>();
        final String fullFileName = listName + ".yaml";
        logger.info("Loading list view from file: {}", fullFileName);
        final File file = new File(listViewDirectory, fullFileName);
        final ObjectMapper mapper = new YAMLMapper(new YAMLFactory());

        try {
            final List<ListViewDTO> viewDTOs = mapper.readValue(
                    file,
                    mapper.getTypeFactory().constructCollectionType(List.class, ListViewDTO.class)
            );
            for (ListViewDTO viewDTO : viewDTOs) {
                allViewNames.add(viewDTO.name);
            }

            return allViewNames;
        }
        catch (IOException e) {
            logger.error("Failed to read the list views from file", e);
        }

        return Collections.emptyList();
    }


    public ListView loadListView(String listName, String viewName) {
        final String fullFileName = listName + ".yaml";
        logger.info("Loading list view from file: {}", fullFileName);

        final File file = new File(listViewDirectory, fullFileName);
        final ObjectMapper mapper = new YAMLMapper(new YAMLFactory());

        try {
            final List<ListViewDTO> viewDTOs = mapper.readValue(
                    file,
                    mapper.getTypeFactory().constructCollectionType(List.class, ListViewDTO.class)
            );
            final Optional<ListViewDTO> viewDTO = viewDTOs
                    .stream()
                    .filter(dto -> dto.name.equals(viewName))
                    .findFirst();

            logger.info("Loaded list view from file: {}", listName);

            return viewDTO.map(this::getListViewFromDTO).orElse(null);
        }
        catch (IOException e) {
            logger.error("Failed to read the viewDTO from file", e);
        }

        return null;
    }

    public ListView getListViewFromDTO(ListViewDTO listViewDTO) {
        final ListView listView = new ListView();

        listView.listName = listViewDTO.listName;
        listView.name = listViewDTO.name;
        listView.itemSorting = listViewDTO.itemSorting;

        try {
            listView.filterSets = filterService.getFilterSets(listViewDTO.filterSetNames);
        }
        catch (IOException e) {
            logger.error("Could not load list view file");
        }

        return listView;
    }

}
