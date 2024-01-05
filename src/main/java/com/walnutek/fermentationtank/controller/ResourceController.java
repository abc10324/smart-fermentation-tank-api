package com.walnutek.fermentationtank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ResourceController {

	@GetMapping("/backend")
	public String redirectBackendSite() {
		return "redirect:/backend/";
	}
	
	@GetMapping("/backend/")
	public String backendSite() {
		return "/backend/index.html";
	}
	
}
