package logic;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class RegexCreator {

	private final String upperCase = "A-Z";
	private final String lowerCase = "a-z";
	private final String numeric = "0-9";

	public String getRegex(Set<String> values) {
		String regexString = StringUtils.EMPTY;
		int min = 0;
		int max = 0;

		for (String value : values) {

			regexString = checkString(regexString, value);
		}
		return regexString;
	}

	String checkString(String regex, String str) {
		char ch;
		for (int i = 0; i < str.length(); i++) {
			ch = str.charAt(i);
			if (Character.isDigit(ch) && !regex.contains(numeric)) {
				regex = regex.concat(numeric);
			} else if (Character.isUpperCase(ch) && !regex.contains(upperCase)) {
				regex = regex.concat(upperCase);
			} else if (Character.isLowerCase(ch) && !regex.contains(lowerCase)) {
				regex = regex.concat(lowerCase);
			}
			// TODO: add float and special caracters and min max if they are usefull?
		}
		return regex;
	}
}
