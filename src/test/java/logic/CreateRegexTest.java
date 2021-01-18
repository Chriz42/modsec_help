package logic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CreateRegexTest {

	// TODO: Thoughts about negativ test...
	@ParameterizedTest
	@MethodSource
	public void getRegex(List<String> regex, Set<String> values) {
		RegexCreator regexCreator = new RegexCreator();
		String regexString = regexCreator.getRegex(values);
		for (String reg : regex) {
			assertTrue(regexString.contains(reg));
		}

	}

	private static Stream<Arguments> getRegex() {
		// @formatter:off
		return Stream.of(Arguments.of(Arrays.asList("a-z", "A-Z", "0-9"), Set.of("TT", "aa", "55")),
				Arguments.of(Arrays.asList("a-z", "A-Z"), Set.of("Kekse", "Sollten ", "super", "sein")),
				Arguments.of(Arrays.asList("0-9"), Set.of("12", "77")),
				Arguments.of(Arrays.asList("A-Z"), Set.of("TT")), Arguments.of(Arrays.asList(), Set.of()),
				Arguments.of(Arrays.asList("0-9", ",", "."), Set.of("55", "44.88", "44,33")), Arguments.of(
						Arrays.asList("0-9", "+", "|", ">", "!", "\\"), Set.of("133+5|>34|(!5|\\\\|0+4(|2!|\\\\/|3")));
		// @formatter:on
	}
}
