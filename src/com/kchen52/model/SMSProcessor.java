package com.kchen52.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.factory.MessageFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class SMSProcessor {
	private static String ACCOUNT_SID = "NULL";
	private static String AUTH_TOKEN = "NULL";
	private static String TWILIO_NUMBER = "NULL";
	private static String TRANSLINK_API = "NULL";
	private final static int MAX_OUTGOING_MESSAGE_LENGTH = 1600;
	private final static String CREDENTIALS_FILENAME = "/home/ubuntu/credentials";
	
	// Have this be a singleton
	private static SMSProcessor processor = null;
	private SMSProcessor() {
		initCredentials(CREDENTIALS_FILENAME);
	}

	public static SMSProcessor getProcessor() {
		if (processor == null) {
			processor = new SMSProcessor();
		}
		return processor;
	}
	
	private void initCredentials(String fileName) {
		// We know there will only be 3 lines, so just do this sloppy for now
        // TODO: Make this unsloppy
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            String firstLine = bufferedReader.readLine();
            String secondLine = bufferedReader.readLine();
            String thirdLine = bufferedReader.readLine();
            String fourthLine = bufferedReader.readLine();

            ACCOUNT_SID = firstLine.split("ACCOUNT_SID = ")[1];
            AUTH_TOKEN = secondLine.split("AUTH_TOKEN = ")[1];
            TWILIO_NUMBER = thirdLine.split("TWILIO_NUMBER = ")[1];
            TRANSLINK_API = fourthLine.split("TRANSLINK_API = ")[1];

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public void someFunction(HttpServletRequest request) {
		String requestPhoneNumber = request.getParameter("From");
		String bodyOfRequest = request.getParameter("Body");
		
		// TODO: Use regex to verify that body follows format.
		// Split the body of the text into an array of Strings, each of which is a bus # (e.g., 320, C28, etc.)
		String[] busesRequested = (bodyOfRequest.split("Request: ")[1]).split(", ");
		
		if (busesRequested.length == 0) {
			//return new ModelAndView("test", "message", "No buses requested lol");
			return;
		}
		
		ArrayList<Bus> buses = new ArrayList<>();
		for (String currentBus : busesRequested) {
			String busRequestURL = "http://api.translink.ca/rttiapi/v1/buses?apikey=" + 
					TRANSLINK_API + "&routeNo=" + currentBus;
			String busInformation = getHTML(busRequestURL);
			
			// Separate buses using regex
			Pattern busPattern = Pattern.compile("<Bus>(.*?)</Bus>");
			Matcher matcher = busPattern.matcher(busInformation);
			while (matcher.find()) {
				Bus bus = new Bus();
				bus.init(matcher.group());
				buses.add(bus);
			}
		}
		
		// Group buses by destination name so there will be less information to send
		buses = (ArrayList<Bus>)mergeSort(buses);
		
		String formattedInformation = prepareBusInformationForSending(buses);
		sendSMS(requestPhoneNumber, formattedInformation);
	}
	
	private String prepareBusInformationForSending(ArrayList<Bus> buses) {
		StringBuilder builder = new StringBuilder();
		String lastDestination = "NULL";
		for (Bus bus : buses) {
			String currentBusDestination = bus.getDestination();
			if (!currentBusDestination.equals(lastDestination)) {
				builder.append(currentBusDestination);
				builder.append(">");
			}
			builder.append(bus.getVehicleNumber());
			builder.append(":");
			builder.append(bus.getLatitude());
			builder.append(",");
			builder.append(bus.getLongitude());
			builder.append("|");
			lastDestination = currentBusDestination;
		}
		return builder.toString();
	}
	
	private void sendSMS(String recipient, String messageToSend) {
		// NOTE: There is a 1600 character limit imposed by Twilio, so split messages
		// accordingly.
		ArrayList<String> splitMessages = new ArrayList<>();
		int lowerIndex = Math.min(MAX_OUTGOING_MESSAGE_LENGTH, messageToSend.length());
		while (lowerIndex >= MAX_OUTGOING_MESSAGE_LENGTH) {
			splitMessages.add(messageToSend.substring(0, MAX_OUTGOING_MESSAGE_LENGTH));
			messageToSend = messageToSend.substring(MAX_OUTGOING_MESSAGE_LENGTH, messageToSend.length());
			lowerIndex = Math.min(MAX_OUTGOING_MESSAGE_LENGTH, messageToSend.length());
		}
		
		// However, if the message was less than 1600 chars, send it as is
		if (splitMessages.size() == 0) {
			splitMessages.add(messageToSend);
		}
		
		try {
			TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
			Account account = client.getAccount();
			MessageFactory messageFactory = account.getMessageFactory();
			for (String subMessage : splitMessages) {
				List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("To", recipient));
				params.add(new BasicNameValuePair("From", TWILIO_NUMBER));
				params.add(new BasicNameValuePair("Body", subMessage));
				messageFactory.create(params);
			}
		} catch (TwilioRestException e) {
			e.printStackTrace();
		}
	}
	
	private String getHTML(String urlToHit) {
		StringBuilder result = new StringBuilder();
		try {
			URL url = new URL(urlToHit);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String currentLine;
			while ((currentLine = reader.readLine()) != null) {
				result.append(currentLine);
			}
			reader.close();
		} catch (IOException e) {
			return "No results available.";
		}
		return result.toString();
	}
	
	 // Run of the mill mergesort. In our case, we're sorting by bus destination
    private List<Bus> mergeSort(List<Bus> arrayToSort) {
        if (arrayToSort.size() <= 1) {
            return arrayToSort;
        }
        // Note that startIndex is inclusive
        // while endIndex is exclusive. That's why the following looks like it would have duplicate
        // values, but actually sorts properly
        List<Bus> firstHalf = mergeSort(arrayToSort.subList(0, arrayToSort.size()/2));
        List<Bus> secondHalf = mergeSort(arrayToSort.subList(arrayToSort.size()/2, arrayToSort.size()));
        List<Bus> mergedResult = merge(firstHalf, secondHalf);
        return mergedResult;
    }

    private List<Bus> merge(List<Bus> firstHalf, List<Bus> secondHalf) {
        List<Bus> merge = new ArrayList<Bus>();
        int firstHalfIndex = 0;
        int secondHalfIndex = 0;
        while (firstHalfIndex < firstHalf.size() && secondHalfIndex < secondHalf.size()) {
            String firstHalfValue = firstHalf.get(firstHalfIndex).getDestination();
            String secondHalfValue = secondHalf.get(secondHalfIndex).getDestination();

            if (firstHalfValue.compareTo(secondHalfValue) <= 0) {
                merge.add(firstHalf.get(firstHalfIndex));
                firstHalfIndex++;
            } else if (firstHalfValue.compareTo(secondHalfValue) > 0) {
                merge.add(secondHalf.get(secondHalfIndex));
                secondHalfIndex++;
            }
        }

        while (firstHalfIndex < firstHalf.size()) {
            merge.add(firstHalf.get(firstHalfIndex));
            firstHalfIndex++;
        }

        while (secondHalfIndex < secondHalf.size()) {
            merge.add(secondHalf.get(secondHalfIndex));
            secondHalfIndex++;
        }
        return merge;
    }

}
