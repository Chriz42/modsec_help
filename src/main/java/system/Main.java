package system;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import input.LogFileParser;
import input.ModsecFileParser;
import input.exceptions.FileParserException;
import logic.LocationMatchCreator;
import logic.Printer;
import logic.UrlPartsCreator;
import model.LocationMatch;
import model.UrlPart;

public class Main {

	public static FileBasedConfiguration appProps = loadPropertiesFile("app.properties");

	private static final String pathDelimiter = System.getProperty("file.separator");

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

		// Add possibility to overwrite properties from external file
		// How to handle optional params when they are send empty?
		// exception Handling

		String logFileName = null;
		String existingModsecurityFileName = null;
		String modsecRuleFileName = "modsecRulesFile.conf";
		for (int i = 0; i < args.length; i++) {
			switch (args[i].toLowerCase()) {
			case "existingmodsecurityfile":
			case "existingModsecurityFile":
				existingModsecurityFileName = args[++i];
				break;
			case "logfile":
			case "logFile":
				logFileName = args[++i];
				break;
			case "outputfilename":
			case "outputFilename":
				modsecRuleFileName = args[++i];
				break;
			case "--help":
			case "-help":
			case "-h":
			case "--h":
				printHelpInfos();
				System.exit(42);
				break;
			default:
				setProperty(i, args);
				i++;
			}
		}

		if (StringUtils.isBlank(logFileName)) {
			logger.error("logfile is mandatroy, set --help for more informations");
			System.exit(42);
		}
		final String executionDirectory = System.getProperty("user.dir");
		String logFilePath = executionDirectory + pathDelimiter + logFileName;
		File logFile = new File(logFilePath);
		if (!logFile.exists() && !logFile.isDirectory()) {
			logger.error("Logfile: " + logFilePath + " can't be read. Check path and file permissions");
			System.exit(42);
		}
		logger.info("Modsecurity log file to read: " + logFilePath);
		try {
			process(existingModsecurityFileName, modsecRuleFileName, executionDirectory, logFile);
		} catch (IOException | FileParserException e) {
			logger.error("Catch critical exception: " + e.getMessage() + " Shutting down!");
			System.exit(42);
		} catch (Exception e) {
			logger.error("Catch unexpected critical exception: " + e.getMessage() + " Shutting down!");
			System.exit(42);
		}

	}

	private static void process(String existingModsecurityFileName, String modsecRuleFileName,
			final String executionDirectory, File logFile)
			throws FileNotFoundException, IOException, FileParserException {
		BufferedReader logFileReader = new BufferedReader(new FileReader(logFile));

		HashMap<String, Set<String>> dataMap = new LogFileParser().parse(logFileReader);

		List<UrlPart> urlPartList = new ArrayList<UrlPart>();

		if (StringUtils.isNotBlank(existingModsecurityFileName)) {
			String existingModsecurityFilePath = executionDirectory + pathDelimiter + existingModsecurityFileName;
			logger.info("Parse existing modsecurity file: " + existingModsecurityFilePath);
			File existingModsecurityFile = new File(existingModsecurityFilePath);
			if (!existingModsecurityFile.exists() && !existingModsecurityFile.isDirectory()) {
				logger.error("existing modsecurityfile: " + existingModsecurityFilePath
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
		if (appProps.getBoolean(Properties.denyAccessToUnknownUrl.name(), true)) {
			printer.printDefaultMatchToStream(outStream);
		}

		Path outputFile = Paths.get(executionDirectory + pathDelimiter + modsecRuleFileName);
		Files.write(outputFile, outStream.toByteArray(), StandardOpenOption.CREATE);
		logger.info("Write rules to file: " + outputFile.getFileName().toAbsolutePath());
	}

	private static void setProperty(int i, String[] args) {
		Optional<Properties> property = searchEnum(Properties.class, args[i]);
		property.ifPresentOrElse((prop) -> {
			String propName = prop.name();
			if (appProps.containsKey(propName)) {
				appProps.clearProperty(propName);
			}
			appProps.addProperty(propName, args[i + 1]);
			logger.info("Reading property: " + propName + " with value: " + args[i + 1]
					+ " from commandline overwriting existing value!");

		}, () -> {
			logger.error(args[i] + " is not a expected property! ");
			System.exit(42);
		});

	}

	private static void printHelpInfos() {
		logger.info("");
		logger.info("Modsecurity learning mode parameters:");
		logger.info("Only relativ paths to the execution directory are working at the moment");
		logger.info("");
		logger.info("maditory parameters:");
		logger.info("logfile -> file with modsecurity audit logs");
		logger.info("");
		logger.info("optional parameters:");
		logger.info("outputfilename -> name of outputfile defualt: modsecRulesFile.conf");
		logger.info("existingmodsecurityfile -> file with existing rules that should be updated");
		logger.info("");
		logger.info("It is possible to overwrite properties. Add propertyname value for details see readme");

	}

	private static FileBasedConfiguration loadPropertiesFile(String propFileName) {
		String appConfigPath = Thread.currentThread().getContextClassLoader().getResource(propFileName).getPath();
		Parameters params = new Parameters();
		File propertiesFile = new File(appConfigPath);

		FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
				PropertiesConfiguration.class)
						.configure(params.fileBased().setListDelimiterHandler(new DefaultListDelimiterHandler(','))
								.setFile(propertiesFile));
		try {
			return builder.getConfiguration();
		} catch (ConfigurationException e) {
			logger.warn(
					"Can't read configuration File: " + e.getMessage() + " will use defaults and provided properties");
		}
		return new PropertiesConfiguration();
	}

	public static <T extends Enum<?>> Optional<T> searchEnum(Class<T> enumeration, String search) {
		for (T each : enumeration.getEnumConstants()) {
			if (each.name().compareToIgnoreCase(search) == 0) {
				return Optional.of(each);
			}
		}
		return Optional.empty();
	}
}
