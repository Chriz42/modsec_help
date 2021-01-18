package system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import input.FileParser;
import logic.UrlPartsCreator;
import model.UrlPart;

public class Main {

	public static void main(String[] args) throws IOException {
		// TODO READ filename from args
		InputStream inputStream = Main.class.getResourceAsStream("modsecurity.log");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		HashMap<String, Set<String>> dataMap = new FileParser().parse(reader);
		List<UrlPart> urlList = new UrlPartsCreator().parseRAWData(dataMap);
		System.out.println("bla");

		// TODO: Find IDs inside the url path
		// TODO: Set regex for parameter Values (maybe a new model)

	}

}
