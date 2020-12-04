package input;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class FileParserTest {

	@Test
	public void singleLogEntry() throws IOException {
		FileParser parser = new FileParser();

		InputStream inputStream = FileParserTest.class.getResourceAsStream("modsecurity.log");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		Map<String, Set<String>> parsedLines = parser.parse(reader);
		for (Entry<String, Set<String>> entry : parsedLines.entrySet()) {
			assertThat(entry.getKey(), is(oneOf("POST /test.php HTTP/1.1", "Post /test.php HTTP/1.1",
					"POST /jkngsdjgs HTTP/1.1", "POST /php HTTP/1.1", "POST /jkngsdjgs/php HTTP/1.1")));
			for (String value : entry.getValue()) {
				assertThat(value,
						is(oneOf("param1=value1&param2=value2", "param1=value1&param2=kekse", "kekse=super",
								"param1=value1&param2=value2", "param1=value1&param2=E68uhj",
								"param1=value1&pm2=E68uhj", "value1&param2=value2")));

			}
		}
	}
}
