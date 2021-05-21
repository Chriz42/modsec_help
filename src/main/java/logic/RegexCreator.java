package logic;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class RegexCreator {

	private final String upperCase = "A-Z";
	private final String upperCasePlaceHolder = "upperCase";
	private final String lowerCase = "a-z";
	private final String lowerCaseupperCasePlaceHolder = "lowerCase";
	private final String numeric = "0-9";
	private final String numericupperCasePlaceHolder = "numeric";
	private final List<String> toEscapeChars = Arrays.asList("\\", ",", "^", "$", ".", "|", "?", "*", "+", "(", ")",
			"[", "]", "{", "}", "/");
	private final String escapeChar = "\\";

	public String getRegex(Set<String> values) {
		String regexString = StringUtils.EMPTY;

		for (String value : values) {
			regexString = checkString(regexString, value);
		}
		regexString = StringUtils.replace(regexString, upperCasePlaceHolder, upperCase);
		regexString = StringUtils.replace(regexString, lowerCaseupperCasePlaceHolder, lowerCase);
		regexString = StringUtils.replace(regexString, numericupperCasePlaceHolder, numeric);
		return regexString;
	}

	String checkString(String regex, String str) {
		String ch;
		for (int i = 0; i < str.length(); i++) {
			ch = Character.toString(str.charAt(i));
			if (StringUtils.isNumeric(ch)) {
				if (!regex.contains(numericupperCasePlaceHolder)) {
					regex = regex.concat(numericupperCasePlaceHolder);
				}
			} else if (StringUtils.isAllUpperCase(ch)) {
				if (!regex.contains(upperCasePlaceHolder)) {
					regex = regex.concat(upperCasePlaceHolder);
				}
			} else if (StringUtils.isAllLowerCase(ch)) {
				if (!regex.contains(lowerCaseupperCasePlaceHolder)) {
					regex = regex.concat(lowerCaseupperCasePlaceHolder);
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
}
