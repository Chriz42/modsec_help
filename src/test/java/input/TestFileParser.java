package input;

import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class TestFileParser {

	@Test
	public void singleLogEntry() throws IOException {
		FileParser parser = new FileParser();

		InputStream inputStream = TestFileParser.class.getResourceAsStream("modsecurity.log");

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		// Mockito.mock(BufferedReader.class);
		when(reader.readLine()).thenReturn("first line").thenReturn("second line");

		Map<String, String> parsedLines = parser.parse(reader);
	}
}
