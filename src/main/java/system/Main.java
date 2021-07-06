package system;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;

import input.LogFileParser;
import input.ModsecFileParser;
import input.exceptions.FileParserException;
import logic.LocationMatchCreator;
import logic.Printer;
import logic.UrlPartsCreator;
import model.LocationMatch;
import model.UrlPart;

public class Main {

	public static PropertiesConfiguration appProps = loadPropertiesFile();

	public static void main(String[] args) throws IOException, FileParserException {
		// TODO help from args
		// Test this args logic
		// better path handling for in and out files
		// base url / doesn'T work

		List<String> argsList = Arrays.asList(args);
		String logFileName = "modsec.log";
//		String logFileName = "modsecurity-netcetera_admin.log.1";
		String oldModsecurityFile = "modsecRulesFile_10.conf_old";
		String modsecRuleFileName = "modsecRulesFile.conf";
		for (int i = 0; i < args.length; i++) {
			switch (args[i].toLowerCase()) {
			case "oldModsecurityFile":
				oldModsecurityFile = args[i + 1];
			case "logfilename":
				logFileName = args[i + 1];
				break;
			case "outputfile":
				modsecRuleFileName = args[i + 1];
				break;
			}
		}
		System.out.println("Modsecurity log file to read: " + logFileName);

		InputStream logFileInputStream = Main.class.getResourceAsStream(logFileName);

		BufferedReader logFileReader = new BufferedReader(new InputStreamReader(logFileInputStream));
		HashMap<String, Set<String>> dataMap = new LogFileParser().parse(logFileReader);

		List<UrlPart> urlPartList = new ArrayList<UrlPart>();

		if (StringUtils.isNotBlank(oldModsecurityFile)) {
			System.out.println("Parse old modsecurity file: " + oldModsecurityFile);
			InputStream inputStream = Main.class.getResourceAsStream(logFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			List<UrlPart> oldUrlPartList = new ModsecFileParser().parseFile(reader);
			urlPartList = new UrlPartsCreator().parseRAWData(dataMap, oldUrlPartList);
		} else {
			urlPartList = new UrlPartsCreator().parseRAWData(dataMap);
		}

		Set<LocationMatch> locationMatchList = new LocationMatchCreator().createListOfLocationMatch(urlPartList);

		Printer printer = new Printer();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		for (LocationMatch locationMatch : locationMatchList) {
			printer.printToStream(locationMatch, outStream);
		}
		if (appProps.getBoolean("denyAccessToUnknownUrl", true)) {
			printer.printDefaultMatchToStream(outStream);
		}

		Path outputFile = Paths.get(modsecRuleFileName);
		Files.write(outputFile, outStream.toByteArray(), StandardOpenOption.CREATE);
		System.out.println("Write rules to file: " + outputFile.getFileName().toAbsolutePath());
	}

	private static PropertiesConfiguration loadPropertiesFile() {
		PropertiesConfiguration appPropsLoaded = new PropertiesConfiguration();
		try {
			/// Read properties
			String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
			String appConfigPath = rootPath + "app.properties";
			appPropsLoaded = new Configurations().properties(new File(appConfigPath));
			appPropsLoaded.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
		} catch (ConfigurationException e) {
			System.out.println("Error reading properties. message: " + e.getMessage() + " will use default config");

		}
		return appPropsLoaded;
	}

}
