package Imperium.ListMaster.Application.controllers.pages;

import Imperium.ListMaster.Application.data.ToDoListItem;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MainController {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @GetMapping("/home")
    public void getHomePage(HttpServletResponse response) throws IOException {
        if(activeProfile.equals("local")) {
            response.sendRedirect("/list-page?name=TESTtodolist");
        }
        else {
            response.sendRedirect("/list-page?name=todolist");
        }
    }
}
