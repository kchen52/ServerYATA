package com.kchen52.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kchen52.model.SMSProcessor;
import com.twilio.sdk.verbs.TwiMLResponse;

@Controller
public class SMSController {

    @RequestMapping(value="/sms", method=RequestMethod.GET)
	public void processSMS(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// TODO: Validate input with regex

		// Assuming it's legitimate
		SMSProcessor processor = SMSProcessor.getProcessor();
		processor.handleRequest(request);

		// Create an empty TwiML response for the twilio request
		TwiMLResponse twiml = new TwiMLResponse();
		response.setContentType("application/xml");
		response.getWriter().print(twiml.toXML());
	}

    @RequestMapping(value="/test", method=RequestMethod.GET)
	public String showTestPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	return "test";
	}

    @RequestMapping(value="/test", method=RequestMethod.POST)
	public String handleTestPagePost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String msg = request.getParameter("message");
		SMSProcessor processor = SMSProcessor.getProcessor();
		processor.sendSMS("+17786554235", msg);
    	return "test";
	}
}
