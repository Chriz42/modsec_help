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

	@ParameterizedTest
	@MethodSource
	public void getRegex(List<String> regex, Set values) {
		RegexCreator regexCreator = new RegexCreator();
		String regexString = regexCreator.getRegex(values);
		for (String reg : regex) {
			assertTrue(regexString.contains(reg));
		}

	}

	private static Stream<Arguments> getRegex() {
		return Stream.of(Arguments.of(Arrays.asList("a-z", "A-Z", "0-9"), Set.of("TT", "aa", "55")));
	}

}
