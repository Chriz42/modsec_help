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

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
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

	public static FileBasedConfiguration appProps = loadPropertiesFile();

	public static void main(String[] args) throws IOException, FileParserException {
		// TODO help from args
		// Test this args logic
		// better path handling for in and out files
		// base url / doesn'T work
		// And some point it would be nice to add project spefic config
		// How to handle optional params when they are send empty?
		List<String> argsList = Arrays.asList(args);
		String logFileName = "modsec.log";
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
			InputStream modsecInputStream = Main.class.getResourceAsStream(oldModsecurityFile);
			BufferedReader modsecReader = new BufferedReader(new InputStreamReader(modsecInputStream));
			List<UrlPart> oldUrlPartList = new ModsecFileParser().parseFile(modsecReader);
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

	private static FileBasedConfiguration loadPropertiesFile() {
		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String appConfigPath = rootPath + "app.properties";
		Parameters params = new Parameters();
		File propertiesFile = new File(appConfigPath);

		FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
				PropertiesConfiguration.class)
						.configure(params.fileBased().setListDelimiterHandler(new DefaultListDelimiterHandler(','))
								.setFile(propertiesFile));
		try {
			return builder.getConfiguration();
		} catch (ConfigurationException e) {
			System.out.println("Can't read configuration File: " + e.getMessage());
		}
		return new PropertiesConfiguration();
	}

}
