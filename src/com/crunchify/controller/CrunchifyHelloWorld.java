package com.crunchify.controller;
 
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
 
/*
 * author: Crunchify.com
 * 
 */
 
@Controller
public class CrunchifyHelloWorld {
 
	@RequestMapping("/welcome")
	public ModelAndView helloWorld() {
 
		String message = "Hello world!";
		return new ModelAndView("welcome", "message", message);
	}
	
	@RequestMapping("/test")
	public ModelAndView test() {
		String message = "test";
		return new ModelAndView("welcome", "message", message);
	}
}