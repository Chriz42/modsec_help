package input;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import input.exceptions.FileParserException;
import logic.RegexCreator;
import model.HTTPType;
import model.UrlPart;

public class ModsecFileParser {

	RegexCreator regexCreator = new RegexCreator();

	public List<UrlPart> parseFile(BufferedReader reader) throws IOException, FileParserException {
		List<UrlPart> urlList = new ArrayList<>();
		String line;
		UrlPart tmp = null;
		Map<String, Set<String>> paramMap = new HashMap<String, Set<String>>();
		while ((line = reader.readLine()) != null) {
			// <LocationMatch "^/configuration/url$">
			if (line.startsWith("<LocationMatch")) {
				if (tmp != null) {
					throw new FileParserException(
							"Locationmatch start tag was reached without closing the one before!");
				}
				String[] parts = line.split(" ");
				String url = parts[1].substring(3, parts[1].length() - 3);
				tmp = createUrlParts(url);
			} else if (line.contains("SecRule REQUEST_METHOD")) {
				// SecRule REQUEST_METHOD !(POST) "deny,id:31280226,msg:'Request method not
				// allowed'"
				addHttpTypeToLastChild(tmp, line);
			} else if (line.contains("SecRule ARGS_NAMES") || line.contains("SecRule ARGS_GET_NAMES")
					|| line.contains("SecRule ARGS_POST_NAMES")) {
				// SecRule ARGS_NAMES !^(firstName|lastName)$ "deny,id:31280209,msg:'The request
				// contained the following unexpected Param: %{MATCHED_VAR_NAME}'"
				addParamNames(paramMap, line);
			} else if (line.contains("SecRule ARGS")) {
				// SecRule ARGS:firstName !^[a-zA-Z]+$ "deny,id:31280210,msg:'The Parameter
				// %{MATCHED_VAR_NAME} contains invalid characters'"
				addParamValue(paramMap, line);
			} else if (line.startsWith("</LocationMatch>")) {
				// </LocationMatch>
				if (tmp != null) {
					tmp.addParamMapToLastChild(paramMap);
					urlList.add(tmp);
				} else {
					throw new FileParserException("End of locationmatch was reached without a stating tag");
				}
				paramMap = new HashMap<String, Set<String>>();
				tmp = null;

			}
		}
		return urlList;
	}

	void addParamValue(Map<String, Set<String>> paramMap, String line) throws FileParserException {
		String[] parts = line.split(":", 2);
		String[] nextParts = parts[1].split(" ", 3);
		if (paramMap.containsKey(nextParts[0])) {
			String rawValue = nextParts[1].substring(3, nextParts[1].length() - 3);
			Set<String> paramValues = paramMap.get(nextParts[0]);
			paramValues.add(regexCreator.createStringFromRegex(rawValue));
			paramMap.put(nextParts[0], paramValues);
		} else {
			throw new FileParserException("Error while parsing modsecurity rules from file. No ParamName for "
					+ nextParts[0] + "inside parsed data");
		}
	}

	void addParamNames(Map<String, Set<String>> paramMap, String line) {
		String[] parts = line.split("!", 2);
		String[] nextParts = parts[1].split(" ", 2);
		String rawValue = nextParts[0].substring(2, nextParts[0].length() - 2);
		if (StringUtils.isBlank(rawValue)) {
			return;
		}
		if (rawValue.contains("|")) {
			Arrays.stream(rawValue.split("\\|")).forEach(string -> paramMap.put(string, new HashSet<String>()));
		} else {
			paramMap.put(rawValue, new HashSet<String>());
		}
	}

	void addHttpTypeToLastChild(UrlPart tmp, String line) {
		String[] parts = line.split("!", 2);
		String[] nextParts = parts[1].split(" ", 2);
		String rawTyp = nextParts[0].substring(1, nextParts[0].length() - 1);
		Set<HTTPType> httpTyps = new HashSet<>();
		if (rawTyp.contains("|")) {
			httpTyps = Arrays.stream(rawTyp.split("\\|")).map(string -> HTTPType.valueOf(string))
					.collect(Collectors.toSet());
		} else {
			httpTyps.add(HTTPType.valueOf(rawTyp));
		}
		addHttpTyp(tmp, httpTyps);
	}

	private void addHttpTyp(UrlPart tmp, Set<HTTPType> httpTyps) {
		if (tmp.hasChildren()) {
			addHttpTyp(tmp.getChildren().iterator().next(), httpTyps);
		} else {
			tmp.addHttpTyps(httpTyps);
		}

	}

	UrlPart createUrlParts(String url) {

		if (StringUtils.isBlank(url)) {
			return new UrlPart("/");
		}
		String[] nextUrlPartString = url.split("/", 2);
		UrlPart part = new UrlPart(nextUrlPartString[0]);

		if (nextUrlPartString.length > 1 && StringUtils.isNotBlank(nextUrlPartString[1])) {
			part.addChild(createUrlParts(nextUrlPartString[1]));
		}
		return part;
	}
}
