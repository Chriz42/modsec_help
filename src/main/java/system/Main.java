package system;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import input.FileParser;
import logic.LocationMatchCreator;
import logic.Printer;
import logic.UrlPartsCreator;
import model.LocationMatch;
import model.UrlPart;

public class Main {

	public static Properties appProps = new Properties();

	public static void main(String[] args) throws IOException {
		// TODO help from args
		// Test this args logic
		// better path handling for in and out files
		// load properties file for secid and forbidUnknownPostParams

		loadPropertiesFile();
		// base url / doesn'T work
		boolean forbidUnknownPostParams = Boolean.valueOf(appProps.getProperty("forbidUnknownPostParams", "true"));
		int startRuleId = Integer.valueOf(appProps.getProperty("startRuleId", "666666"));

		List<String> argsList = Arrays.asList(args);
		String logFileName = "modsec.log";
//		String logFileName = "modsecurity-netcetera_admin.log.1";
		String modsecRuleFileName = "modsecRulesFile.conf";
		for (int i = 0; i < args.length; i++) {
			switch (args[i].toLowerCase()) {
			case "logfilename":
				logFileName = args[i + 1];
				break;
			case "outputfile":
				modsecRuleFileName = args[i + 1];
				break;
			}
		}
		System.out.println("Modsecurity log file to read: " + logFileName);

		InputStream inputStream = Main.class.getResourceAsStream(logFileName);

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		HashMap<String, Set<String>> dataMap = new FileParser().parse(reader);
		List<UrlPart> urlList = new UrlPartsCreator().parseRAWData(dataMap);

		LocationMatchCreator locationMatchCreator = new LocationMatchCreator();
		Set<LocationMatch> locationMatchList = locationMatchCreator.createListOfLocationMatch(urlList);

		Printer printer = new Printer(startRuleId, forbidUnknownPostParams);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		for (LocationMatch locationMatch : locationMatchList) {
			printer.printToStream(locationMatch, outStream);
		}
		printer.printDefaultMatchToStream(outStream);

		Path outputFile = Paths.get(modsecRuleFileName);
		Files.write(outputFile, outStream.toByteArray(), StandardOpenOption.CREATE);
		System.out.println("Write rules to file: " + outputFile.getFileName().toAbsolutePath());
	}

	private static void loadPropertiesFile() throws IOException, FileNotFoundException {
		/// Read properties
		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String appConfigPath = rootPath + "app.properties";

		appProps.load(new FileInputStream(appConfigPath));
	}

}
