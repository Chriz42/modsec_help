package system;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import input.FileParser;
import logic.LocationMatchCreator;
import logic.Printer;
import logic.UrlPartsCreator;
import model.LocationMatch;
import model.UrlPart;

public class Main {

	public static void main(String[] args) throws IOException {
		// TODO help from args
		// Test this args logic
		List<String> argsList = Arrays.asList(args);
		String logFileName = "modsec.log";
		for (int i = 0; i < args.length; i++) {
			switch (args[i].toLowerCase()) {
			case "logfilename":
				logFileName = args[i + 1];
				break;
			}
		}

		InputStream inputStream = Main.class.getResourceAsStream(logFileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		HashMap<String, Set<String>> dataMap = new FileParser().parse(reader);
		List<UrlPart> urlList = new UrlPartsCreator().parseRAWData(dataMap);

		LocationMatchCreator locationMatchCreator = new LocationMatchCreator();
		Set<LocationMatch> locationMatchList = locationMatchCreator.createListOfLocationMatch(urlList);

		Printer printer = new Printer(100000);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		for (LocationMatch locationMatch : locationMatchList) {
			printer.printToStream(locationMatch, outStream);
		}
		printer.printDefaultMatchToStream(outStream);
		System.out.println(outStream.toString());
		// TODO: print to file
	}

}
