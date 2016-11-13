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
            processor.someFunction(request);

            // Create an empty TwiML response for the twilio request
            TwiMLResponse twiml = new TwiMLResponse();
            response.setContentType("application/xml");
            response.getWriter().print(twiml.toXML());
            
            //return new ModelAndView("test", "message", "this be the actual msg lol1");
        }
}
