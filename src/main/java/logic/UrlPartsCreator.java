package logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import model.UrlPart;

public class UrlPartsCreator {

//	TODO:add check if the parent hasn't the HTTPmethode of the child -> error
//	TODO: Create from the tree urlList a list with only limbs without branches
	public List<UrlPart> parseRAWData(Map<String, Set<String>> dataMap) {
		List<UrlPart> urlList = new ArrayList<>();
		for (Entry<String, Set<String>> entry : dataMap.entrySet()) {

			Map<String, Set<String>> paramMap = createParameterMap(entry.getValue());
			UrlPart urlPart = parseUrlandTyp(entry.getKey());
			urlPart.addParamMapToLastChild(paramMap);
			if (urlList.contains(urlPart)) {
				urlList.get(urlList.indexOf(urlPart)).merge(urlPart);
			} else {
				urlList.add(urlPart);
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
		Arrays.stream(paramString.split("&")).map(s -> s.split("="))
				.forEach(pair -> addPairIfNotExist(paramMap, pair[0], pair[1]));
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

	UrlPart parseUrlandTyp(String urlLine) {
		String[] parts = urlLine.split(" ");
		String httpTyp = parts[0].toUpperCase();

		return createUrlParts(parts[1], httpTyp);
	}

	UrlPart createUrlParts(String url, String httpTyp) {
		if (url.startsWith("/")) {
			url = url.replaceFirst("/", "");
		}
		if (StringUtils.isBlank(url)) {
			return new UrlPart(url, httpTyp);
		}
		String[] nextUrlPartString = url.split("/", 2);
		String urlPartString = nextUrlPartString[0];
		// TODO: check if paths in modsec are case sensitive or not
		UrlPart part = new UrlPart(urlPartString, httpTyp);
		if (nextUrlPartString.length > 1 && StringUtils.isNotBlank(nextUrlPartString[1])) {
			part.addChild(createUrlParts(nextUrlPartString[1], httpTyp));
		}
		return part;
	}

//TODO:	I guess this will be to complicated to bring the deeper layers back- > better to splitt creating locationMAtches -> go throw the children and just copy evertyhing before
	List<UrlPart> removeBranches(UrlPart parent) {
		List<UrlPart> urlList = new ArrayList<>();
		if (parent.hasChildren()) {
			Set<UrlPart> children = parent.getChildren();
			parent.removeChildren();
			for (UrlPart urlPart : children) {
				UrlPart parent2 = (UrlPart) SerializationUtils.clone(parent);
				parent2.addChild(urlPart);
				urlList.add(parent2);

			}
		} else {
			urlList.add(parent);
		}
		return urlList;
	}
}
