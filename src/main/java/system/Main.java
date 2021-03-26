package system;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
		// TODO READ filename from args
		// TODO: Add default waf.conf with logging only mode
		InputStream inputStream = Main.class.getResourceAsStream("modsecurity.log");
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
