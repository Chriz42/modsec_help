package logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import logic.exceptions.UrlPartCreatorException;
import model.HTTPType;
import model.UrlPart;

public class UrlPartsCreator {

//	TODO: Create from the tree urlList a list with only limbs without branches
	public List<UrlPart> parseRAWData(Map<String, Set<String>> dataMap) {
		List<UrlPart> urlList = new ArrayList<>();
		for (Entry<String, Set<String>> entry : dataMap.entrySet()) {

			Map<String, Set<String>> paramMap = createParameterMap(entry.getValue());
			UrlPart urlPart;
			try {
				urlPart = parseUrlandTyp(entry.getKey());
				urlPart.addParamMapToLastChild(paramMap);
				if (urlList.contains(urlPart)) {
					urlList.get(urlList.indexOf(urlPart)).merge(urlPart);
				} else {
					urlList.add(urlPart);
				}
			} catch (UrlPartCreatorException e) {
				// TODO: Add logging
				System.out.println("error occured scip this log entry. Message: " + e.getMessage());
			}
		}

		return urlList;
	}

	Map<String, Set<String>> createParameterMap(String paramString) {
		return createParameterMap(new HashMap<String, Set<String>>(), paramString);
	}

	Map<String, Set<String>> createParameterMap(Set<String> paramStringList) {
		return createParameterMap(new HashMap<String, Set<String>>(), paramStringList);
	}

	Map<String, Set<String>> createParameterMap(HashMap<String, Set<String>> paramMap, Set<String> paramStringList) {
		paramStringList.stream().forEach(line -> createParameterMap(paramMap, line));
		return paramMap;
	}

	Map<String, Set<String>> createParameterMap(HashMap<String, Set<String>> paramMap, String paramString) {
		Arrays.stream(paramString.split("&")).map(s -> s.split("=")).forEach(pair -> {
			if (pair.length == 1) {
				addPairIfNotExist(paramMap, pair[0], StringUtils.EMPTY);
			} else {
				addPairIfNotExist(paramMap, pair[0], pair[1]);
			}
		});
		return paramMap;

	}

	void addPairIfNotExist(HashMap<String, Set<String>> paramMap, String key, String value) {
		if (paramMap.containsKey(key)) {
			paramMap.get(key).add(value);
		} else {
			Set<String> valueSet = new HashSet<>();
			valueSet.add(value);
			paramMap.put(key, valueSet);
		}
	}

	UrlPart parseUrlandTyp(String urlLine) throws UrlPartCreatorException {
		String[] parts = urlLine.split(" ");
		HTTPType httpTyp = HTTPType.valueOf(parts[0].toUpperCase());

		return createUrlParts(parts[1], httpTyp);
	}

	UrlPart createUrlParts(String url, HTTPType httpTyp) throws UrlPartCreatorException {
		if (url.startsWith("/")) {
			url = url.replaceFirst("/", "");
		}
		if (StringUtils.isBlank(url)) {
			return new UrlPart("/", httpTyp);
		}
		String[] nextUrlPartString = url.split("/", 2);
		String urlPartString = nextUrlPartString[0];
		// TODO: check if paths in modsec are case sensitive or not

		if (urlPartString.contains("?") && httpTyp.equals(HTTPType.GET)) {
			return createUrlpartWithQueryString(httpTyp, urlPartString);
		} else if (urlPartString.contains("?") && !httpTyp.equals(HTTPType.GET)) {
			throw new UrlPartCreatorException("There shouldn't be a non GET request with querystring");
		}
		UrlPart part = new UrlPart(urlPartString);
		if (nextUrlPartString.length > 1 && StringUtils.isNotBlank(nextUrlPartString[1])) {
			part.addChild(createUrlParts(nextUrlPartString[1], httpTyp));
		} else {
			part.addHttpTyp(httpTyp);
		}
		return part;
	}

	UrlPart createUrlpartWithQueryString(HTTPType httpTyp, String urlPartString) {
		String[] urlAndQueryString = urlPartString.split("\\?", 2);
		UrlPart part = new UrlPart(urlAndQueryString[0]);
		part.addHttpTyp(httpTyp);
		if (StringUtils.isNotBlank(urlAndQueryString[1])) {
			Map<String, Set<String>> paramMap = createParameterMap(urlAndQueryString[1]);
			part.addParamMap(paramMap);
		}
		return part;
	}

}
