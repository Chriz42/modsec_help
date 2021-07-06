package logic;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import system.Main;

public class UrlPartsCreator {

	private static final List<String> resourcePlaceHolder;
	static {
		List<String> list = new ArrayList<String>();
		list.add("js");
		list.add("css");
		list.add("resources");
		list.add("images");
		list.add("fonts");
		list.add("styles");
		list.add("img");
		resourcePlaceHolder = Collections
				.unmodifiableList(Main.appProps.getList(String.class, "resourceUrlPlaceHolder", list));
	}

	public List<UrlPart> parseRAWData(Map<String, Set<String>> dataMap, List<UrlPart> urlPartsList) {
		for (Entry<String, Set<String>> entry : dataMap.entrySet()) {

			Map<String, Set<String>> paramMap = createParameterMap(entry.getValue());
			UrlPart urlPart;
			try {
				urlPart = parseUrlandTyp(entry.getKey());
				urlPart.addParamMapToLastChild(paramMap);
				if (urlPartsList.contains(urlPart)) {
					urlPartsList.get(urlPartsList.indexOf(urlPart)).merge(urlPart);
				} else {
					urlPartsList.add(urlPart);
				}
			} catch (UrlPartCreatorException e) {
				System.out.println("error occured scip this log entry. Message: " + e.getMessage());
			}
		}
		return urlPartsList;
	}

	public List<UrlPart> parseRAWData(Map<String, Set<String>> dataMap) {
		return parseRAWData(dataMap, new ArrayList<UrlPart>());

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
		// dependce on the use OS -> Linux is case sensitive windows not ->
		// best way would be to stick to the incomeing case

		if (urlPartString.contains("?")) {

			if (nextUrlPartString.length > 1) {
				// There is an URl inside the query Params -> use the full url
				return createUrlpartWithQueryString(httpTyp, url);
			} else {
				return createUrlpartWithQueryString(httpTyp, urlPartString);
			}
		}

		if (resourcePlaceHolder.contains(urlPartString) && !httpTyp.equals(HTTPType.GET)) {
			throw new UrlPartCreatorException("There shouldn't be a non GET request on static resources");
		} else if (resourcePlaceHolder.contains(urlPartString)) {
			String fileName = nextUrlPartString[1];
			String[] splittedString = fileName.split("\\.");
			UrlPart part = new UrlPart(urlPartString);
			part.addChild(createUrlParts(".+\\." + splittedString[splittedString.length - 1], httpTyp));
			return part;
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
			String urldecodedParams;
			try {
				urldecodedParams = URLDecoder.decode(urlAndQueryString[1], StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				System.out.println("Urldecoding error with string: " + urlAndQueryString[1]
						+ " and UTF-8 charset. Use raw value for params");
				urldecodedParams = urlAndQueryString[1];
			}
			Map<String, Set<String>> paramMap = createParameterMap(urldecodedParams);
			part.addParamMap(paramMap);
		}
		return part;
	}

}
