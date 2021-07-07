package logic;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class RegexCreator {

	private final String upperCase = "A-Z";
	private final String upperCasePlaceHolder = "AZ";
	private final String lowerCase = "a-z";
	private final String lowerCasePlaceHolder = "az";
	private final String numeric = "0-9";
	private final String numericPlaceHolder = "09";
	private final List<String> toEscapeChars = Arrays.asList(",", "^", "$", ".", "|", "?", "*", "+", "(", ")", "[", "]",
			"{", "}", "/", "\\");
	private final String escapeChar = "\\";

	public String getRegex(Set<String> values) {
		String regexString = StringUtils.EMPTY;

		for (String value : values) {
			regexString = createRegexFromString(regexString, value);
		}
		regexString = StringUtils.replace(regexString, upperCasePlaceHolder, upperCase);
		regexString = StringUtils.replace(regexString, lowerCasePlaceHolder, lowerCase);
		regexString = StringUtils.replace(regexString, numericPlaceHolder, numeric);
		return regexString;
	}

	String createRegexFromString(String regex, String str) {
		String ch;
		for (int i = 0; i < str.length(); i++) {
			ch = Character.toString(str.charAt(i));
			if (StringUtils.isNumeric(ch)) {
				if (!regex.contains(numericPlaceHolder)) {
					regex = regex.concat(numericPlaceHolder);
				}
			} else if (StringUtils.isAllUpperCase(ch)) {
				if (!regex.contains(upperCasePlaceHolder)) {
					regex = regex.concat(upperCasePlaceHolder);
				}
			} else if (StringUtils.isAllLowerCase(ch)) {
				if (!regex.contains(lowerCasePlaceHolder)) {
					regex = regex.concat(lowerCasePlaceHolder);
				}
			} else if (StringUtils.isAsciiPrintable(ch) && !regex.contains(ch)) {
				String specialCharString = ch;
				if (toEscapeChars.contains(ch)) {
					specialCharString = escapeChar.concat(specialCharString);
				}
				regex = regex.concat(specialCharString);
			}

			// TODO: add min max if they are usefull?
		}

		return regex;
	}

	public String createStringFromRegex(String regex) {
		String string = "";
		if (regex.contains(upperCase)) {
			string = string.concat(upperCasePlaceHolder);
			regex = regex.replace(upperCase, "");
		}
		if (regex.contains(lowerCase)) {
			string = string.concat(lowerCasePlaceHolder);
			regex = regex.replace(lowerCase, "");
		}
		if (regex.contains(numeric)) {
			string = string.concat(numericPlaceHolder);
			regex = regex.replace(numeric, "");
		}
		for (String escaped : toEscapeChars) {
			if (regex.contains("\\" + escaped)) {
				string = string.concat(escaped);
				regex = regex.replace("\\" + escaped, "");

			}
		}
		string = string.concat(regex);
		return string;
	}
}
