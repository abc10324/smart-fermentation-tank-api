package com.walnutek.fermentationtank.controller;

import com.walnutek.fermentationtank.model.service.LineNotifyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "LineNotify")
@RestController
@RequestMapping("/line-notify")
public class LineNotifyController {

    @Autowired
    private LineNotifyService lineNotifyService;
}
