package com.mycompany.projectmanagementapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The React frontend (built into src/main/resources/static by the Docker build) uses client-side routing.
 * When the browser reloads /projects, /team, /login... or opens such a link directly, Spring must answer with
 * index.html so React Router can handle the path.
 *
 * Only single-segment paths without a dot are matched, so /api/**, /assets/** and files such as favicon.ico
 * are never affected.
 */
@Controller
public class SpaController {

    @GetMapping({"/", "/{path:[^\\.]*}"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
