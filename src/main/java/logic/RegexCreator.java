package logic;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class RegexCreator {

	private final String upperCase = "A-Z";
	private final String lowerCase = "a-z";
	private final String numeric = "0-9";

	public String getRegex(Set<String> values) {
		String regexString = StringUtils.EMPTY;

		for (String value : values) {
			regexString = checkString(regexString, value);
		}
		return regexString;
	}

	String checkString(String regex, String str) {
		String ch;
		for (int i = 0; i < str.length(); i++) {
			ch = Character.toString(str.charAt(i));
			if (StringUtils.isNumeric(ch)) {
				if (!regex.contains(numeric)) {
					regex = regex.concat(numeric);
				}
			} else if (StringUtils.isAllUpperCase(ch)) {
				if (!regex.contains(upperCase)) {
					regex = regex.concat(upperCase);
				}
			} else if (StringUtils.isAllLowerCase(ch)) {
				if (!regex.contains(lowerCase)) {
					regex = regex.concat(lowerCase);
				}
			} else if (StringUtils.isAsciiPrintable(ch) && !regex.contains(ch)) {
//				TODO: add escaping for special chars
//				\ ^ $ . | ? * +  ( ) [ ] { }  -> escape with \
				regex = regex.concat(ch);
			}

			// TODO: add min max if they are usefull?
		}
		return regex;
	}
}
