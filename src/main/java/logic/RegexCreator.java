package logic;

import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class RegexCreator {

	private final String upperCase = "A-Z";
	private final Pattern hasUpperCasePattern = Pattern.compile("^[A-Z]+$");
	private final String lowerCase = "a-z";
	private final Pattern hasLowerCasePattern = Pattern.compile("^[a-z]+$");
	private final String numeric = "0-9";
	private final Pattern hasNumericPattern = Pattern.compile("^[0-9]+$");

	public String getRegex(Set<String> values) {
		String regexString = StringUtils.EMPTY;
		for (String value : values) {
			regexString = hasUpperCaseChars(regexString, value);
		}
		return regexString;
	}

	String hasUpperCaseChars(String regex, String value) {
		if (!regex.contains(upperCase) && hasUpperCasePattern.matcher(value).matches()) {
			regex = regex.concat(upperCase);
		}
		return hasLowerCaseChars(regex, value);
	}

	String hasLowerCaseChars(String regex, String value) {
		if (!regex.contains(lowerCase) && hasLowerCasePattern.matcher(value).matches()) {
			regex = regex.concat(lowerCase);
		}
		return hasNumericChars(regex, value);
	}

	String hasNumericChars(String regex, String value) {
		if (!regex.contains(numeric) && hasNumericPattern.matcher(value).matches()) {
			regex = regex.concat(numeric);
		}
		return regex;
	}
}
