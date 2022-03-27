package server;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import server.api.ActivityController;

@Controller
@RequestMapping("/")
public class WebController {

    private final ActivityController ctrl;

    public WebController(ActivityController ctrl) {
        this.ctrl = ctrl;
    }

}