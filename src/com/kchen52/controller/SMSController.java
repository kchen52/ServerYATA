package com.kchen52.controller;
 

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kchen52.model.SMSProcessor;
import com.twilio.sdk.verbs.TwiMLResponse;

@Controller
public class SMSController {
 
	@RequestMapping(value="/sms", method=RequestMethod.GET)
	public ModelAndView processSMS(HttpServletRequest request, HttpServletResponse response) {

		// TODO: Validate input with regex
		
		// Assuming it's legit
		SMSProcessor processor = SMSProcessor.getProcessor();
		processor.someFunction(request);

		return new ModelAndView("test", "message", "this be the actual msg lol");
	}
	
		



}