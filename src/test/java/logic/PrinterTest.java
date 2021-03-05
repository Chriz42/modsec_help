package logic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.LocationMatch;

public class PrinterTest {

//	TODO: add negativ tests
	Printer printer;
	ByteArrayOutputStream outStream;

	@BeforeEach
	public void init() {
		printer = new Printer(0);
		outStream = new ByteArrayOutputStream();
	}

	@Test
	public void createTextFromLocationMatch() {
		LocationMatch locationMatch = new LocationMatch();
		locationMatch.concatUrl("user");
		locationMatch.concatUrl("[0-9]+");
		locationMatch.concatUrl("data");
		locationMatch.addParam("name", "a-zA-Z");
		locationMatch.addParam("param2", "0-9\\,\\.");
		locationMatch.addHttpTyps(Set.of("POST", "GET"));

		printer.printToStream(locationMatch, outStream);
		String output = outStream.toString();

		String newLine = "\r\n";
		String openString = String.format(printer.locationMatchOpenString + "\r\n", "/user/[0-9]+/data");
		String httpTypeString = String.format(printer.requestMethodeString + "\r\n", "GET|POST", 0);
		String namesString = String.format(printer.unexpectedParamNameString + "\r\n", "name|param2", 1);
		String valuesString = String.format(printer.containsInvalidCharsString + "\r\n", "name", "a-zA-Z", 2)
				+ String.format(printer.containsInvalidCharsString + "\r\n", "param2", "0-9\\,\\.", 3);
		String allowString = String.format(printer.requestAllowString + "\r\n", 4);
		String closeString = printer.locationMatchCloseString + "\r\n";

		String printedString = newLine + openString + httpTypeString + namesString + valuesString + allowString
				+ closeString;
		assertThat(output, is(printedString));

	}

	@Test
	public void addExpectedParamValuesTest() {
		HashMap<String, String> paramMap = new HashMap<>();
		paramMap.put("one", "21");
		paramMap.put("two", "42");

		PrintWriter printWriter = new PrintWriter(outStream);
		printer.addExpectedParamValues(paramMap, printWriter);
		printWriter.close();
		String output = outStream.toString();
		String printedString = String.format(printer.containsInvalidCharsString + "\r\n", "one", "21", 0)
				+ String.format(printer.containsInvalidCharsString + "\r\n", "two", "42", 1);
		assertThat(output, is(printedString));

	}

	@Test
	public void addExpectedParamNamesTest() {
		HashMap<String, String> paramMap = new HashMap<>();
		paramMap.put("one", "21");
		paramMap.put("two", "42");

		PrintWriter printWriter = new PrintWriter(outStream);
		printer.addExpectedParamNames(paramMap, printWriter);
		printWriter.close();
		String output = outStream.toString();
		assertThat(output, is(String.format(printer.unexpectedParamNameString + "\r\n", "one|two", 0)));
	}

	@Test
	public void addExpectedHtttpTypesTest() {
		HashSet<String> httpTypes = new HashSet<>();
		httpTypes.add("POST");
		httpTypes.add("GET");

		PrintWriter printWriter = new PrintWriter(outStream);
		printer.addHttpMethode(httpTypes, printWriter);
		printWriter.close();
		String output = outStream.toString();
		assertThat(output, is(String.format(printer.requestMethodeString + "\r\n", "POST|GET", 0)));
	}

}
