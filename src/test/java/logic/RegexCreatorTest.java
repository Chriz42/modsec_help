package logic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class RegexCreatorTest {

	// TODO: Thoughts about negativ test...
	@ParameterizedTest
	@MethodSource
	public void getRegex(List<SimpleEntry<String, Integer>> regex, Set<String> values) {
		RegexCreator regexCreator = new RegexCreator();
		String regexString = regexCreator.getRegex(values);
		for (Entry<String, Integer> reg : regex) {
			int count = StringUtils.countMatches(regexString, reg.getKey());
			assertThat(count, is(reg.getValue()));
		}

	}

	private static Stream<Arguments> getRegex() {
		// @formatter:off
		return Stream.of(Arguments.of(Arrays.asList(createSimpleEntry("a-z"), createSimpleEntry("A-Z"), createSimpleEntry("0-9")), Set.of("TT", "aa", "55")),
				Arguments.of(Arrays.asList(createSimpleEntry("a-z"), createSimpleEntry("A-Z")), Set.of("Kekse", "Sollten ", "super", "sein")),
				Arguments.of(Arrays.asList(createSimpleEntry("0-9")), Set.of("12", "77")),
				Arguments.of(Arrays.asList(createSimpleEntry("A-Z")), Set.of("TT")), 
				Arguments.of(Arrays.asList(createSimpleEntry(StringUtils.EMPTY,0)), Set.of()),
				Arguments.of(Arrays.asList(createSimpleEntry("0-9"), createSimpleEntry("\\,"), createSimpleEntry("\\.")), Set.of("55", "44.88", "44,33")), 
				Arguments.of(Arrays.asList(createSimpleEntry("0-9"), createSimpleEntry("+"), createSimpleEntry("|"), createSimpleEntry(">"), createSimpleEntry("!"), createSimpleEntry("\\",3)), Set.of("133+5|>34|(!5|\\\\|0+4(|2!|\\\\/|3")),
				Arguments.of(Arrays.asList(createSimpleEntry("a-z"),createSimpleEntry("\"")), Set.of("\"test\"")),
				Arguments.of(Arrays.asList(createSimpleEntry("a-z"),createSimpleEntry("\\",2)), Set.of("\\test\\")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\\\")), Set.of("\\")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\,")), Set.of(",")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\^")), Set.of("^")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\$")), Set.of("$")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\.")), Set.of(".")),
				Arguments.of(Arrays.asList(createSimpleEntry( "\\|")), Set.of("|")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\?")), Set.of("?")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\*")), Set.of("*")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\+")), Set.of("+")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\(")), Set.of("(")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\)")), Set.of(")")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\[")), Set.of("[")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\]")), Set.of("]")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\{")), Set.of("{")),
				Arguments.of(Arrays.asList(createSimpleEntry("\\}")), Set.of("}")),
				Arguments.of(Arrays.asList(createSimpleEntry("a-z"),createSimpleEntry(":"),createSimpleEntry("!"),createSimpleEntry("^"),createSimpleEntry("["),createSimpleEntry("]"),createSimpleEntry("+"),createSimpleEntry("$")), Set.of("!^[[:print:]]+$")),
				Arguments.of(Arrays.asList(createSimpleEntry("a-z"),createSimpleEntry("A-Z"),createSimpleEntry("-",3)),Set.of("de-DE","en-EN"))
				
				);
		// @formatter:on
	}

	private static SimpleEntry<String, Integer> createSimpleEntry(String key) {
		return new SimpleEntry<String, Integer>(key, 1);
	}

	private static SimpleEntry<String, Integer> createSimpleEntry(String key, int value) {
		return new SimpleEntry<String, Integer>(key, value);
	}
}
