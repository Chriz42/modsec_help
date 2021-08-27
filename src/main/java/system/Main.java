package system;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
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

	private static final String pathDelimiter = "\\";

	public static void main(String[] args) throws IOException, FileParserException, URISyntaxException {
		// base url / doesn'T work
		// Add possibility to overwrite properties from external file or comandline
		// How to handle optional params when they are send empty?
		// add logging framework an set log level to warn to remove the
		// org.apache.commons.beanutils.FluentPropertyBeanIntrospector info thing
		// exception Handling

		List<String> argsList = Arrays.asList(args);
		String logFileName = null;
		String existingModsecurityFileName = null;
		String modsecRuleFileName = "modsecRulesFile.conf";
		for (int i = 0; i < args.length; i++) {
			switch (args[i].toLowerCase()) {
			case "existingmodsecurityfile":
				existingModsecurityFileName = args[i + 1];
			case "logfile":
				logFileName = args[i + 1];
				break;
			case "outputfilename":
				modsecRuleFileName = args[i + 1];
				break;
			case "--help":
			case "-help":
			case "-h":
			case "--h":
				printHelpInfos();
				System.exit(42);
				break;
			}
		}

		if (StringUtils.isBlank(logFileName)) {
			System.out.println("logfile is mandatroy, set --help for more informations");
			System.exit(42);
		}
		final String executionDirectory = System.getProperty("user.dir");
		// path delimiter important when executed with linux?
		String logFilePath = executionDirectory + pathDelimiter + logFileName;
		File logFile = new File(logFilePath);
		if (!logFile.exists() && !logFile.isDirectory()) {
			System.out.println("Logfile: " + logFilePath + " can't be read. Check path and file permissions");
			System.exit(42);
		}
		System.out.println("Modsecurity log file to read: " + logFilePath);

		BufferedReader logFileReader = new BufferedReader(new FileReader(logFile));

		HashMap<String, Set<String>> dataMap = new LogFileParser().parse(logFileReader);

		List<UrlPart> urlPartList = new ArrayList<UrlPart>();

		if (StringUtils.isNotBlank(existingModsecurityFileName)) {
			String existingModsecurityFilePath = executionDirectory + pathDelimiter + existingModsecurityFileName;
			System.out.println("Parse existing modsecurity file: " + existingModsecurityFilePath);
			File existingModsecurityFile = new File(existingModsecurityFilePath);
			if (!existingModsecurityFile.exists() && !existingModsecurityFile.isDirectory()) {
				System.out.println("existing modsecurityfile: " + existingModsecurityFilePath
						+ " can't be read. Check path and file permissions");
				System.exit(42);
			}
			BufferedReader modsecReader = new BufferedReader(new FileReader(existingModsecurityFile));
			List<UrlPart> existingUrlPartList = new ModsecFileParser().parseFile(modsecReader);
			urlPartList = new UrlPartsCreator().parseRAWData(dataMap, existingUrlPartList);
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

		Path outputFile = Paths.get(executionDirectory + "\\" + modsecRuleFileName);
		Files.write(outputFile, outStream.toByteArray(), StandardOpenOption.CREATE);
		System.out.println("Write rules to file: " + outputFile.getFileName().toAbsolutePath());
	}

	private static void printHelpInfos() {
		System.out.println();
		System.out.println("Modsecurity learning mode parameters:");
		System.out.println("Only relativ paths to the execution directory are working at the moment");
		System.out.println();
		System.out.println("maditory parameters:");
		System.out.println("logfile -> file with modsecurity audit logs");
		System.out.println();
		System.out.println("optional parameters:");
		System.out.println("outputfilename -> name of outputfile defualt: modsecRulesFile.conf");
		System.out.println("existingmodsecurityfile -> file with existing rules that should be updated");

	}

	private static FileBasedConfiguration loadPropertiesFile() {
		String appConfigPath = Thread.currentThread().getContextClassLoader().getResource("app.properties").getPath();
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
