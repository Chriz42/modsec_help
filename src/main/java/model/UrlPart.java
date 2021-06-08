package model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;

public class UrlPart {

	private String urlPartString;
	private Map<String, Set<String>> params = new HashMap<String, Set<String>>();
	private Set<HTTPType> httpTyps = new HashSet<HTTPType>();

	// TODO: make this regex configureable
	public static final String UUIDRegexStringForUrlpart = "[a-fA-F0-9\\-]+";
	public static final String HashRegexString = "[a-fA-F0-9]+";
	private Set<UrlPart> children = new HashSet<UrlPart>();

	public UrlPart(String url) {
		setUrlPart(url);
	}

	public UrlPart(String url, HTTPType httpTyp) {
		setUrlPart(url);
		this.params = new HashedMap<String, Set<String>>();
		this.httpTyps.add(httpTyp);
	}

	private void setUrlPart(String url) {
		if (StringUtils.isNumeric(url)) {
			this.urlPartString = "[0-9]+";
		} else if (isUUID(url)) {
			this.urlPartString = UUIDRegexStringForUrlpart;
		} else if (containsHashString(url)) {
			this.urlPartString = addHashRegexStringToUrl(url);

		} else {
			this.urlPartString = url;
		}
	}

	private String addHashRegexStringToUrl(String url) {
		String[] splitted = url.split("\\.");
		return Arrays.stream(splitted).map(string -> string.matches(HashRegexString) ? HashRegexString : string)
				.collect(Collectors.joining("\\."));
	}

	/*
	 * In frontend development it is common to set a hash inside the static resource
	 * filenames. Without a regex we have to generate new locationmatches for every
	 * new version e.g. runtime.e330f42ab6b744c4debd.js or
	 * nca-favicon.37cfa069b71f7ebb6c14.svg The methode with this regex should
	 * recognize this TODO: make this regex configureable
	 *
	 */
	private boolean containsHashString(String url) {
		return url.matches("^[a-zA-Z\\-]+\\.[a-fA-F0-9]+\\.[a-zA-Z0-9]+$");
	}

	// TODO: make this regex configureable
	private boolean isUUID(String url) {
		return url.matches("^[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}$");
	}

	public void addChild(UrlPart urlPart) {
		if (!children.contains(urlPart)) {
			children.add(urlPart);
		}
	}

	public void addParamMap(Map<String, Set<String>> params) {
		this.params = params;
	}

	public String getUrlPartString() {
		return this.urlPartString;
	}

	public Set<HTTPType> getHttpTyps() {
		return this.httpTyps;
	}

	public Set<UrlPart> getChildren() {
		return children;
	}

	public Map<String, Set<String>> getParamMap() {
		return params;
	}

	public void addHttpTyp(HTTPType httpTyp) {
		this.httpTyps.add(httpTyp);
	}

	public void addHttpTyps(Set<String> httpTyps) {
		httpTyps.addAll(httpTyps);
	}

	public void addParamMapToLastChild(Map<String, Set<String>> paramMap) {
		if (CollectionUtils.sizeIsEmpty(children)) {
			this.params.putAll(paramMap);
		} else {
			children.iterator().next().addParamMapToLastChild(paramMap);
		}
	}

	public void merge(UrlPart other) {
		if (!equals(other)) {
			return;
		}
		httpTyps.addAll(other.getHttpTyps());
		mergeParamMap(other.getParamMap());
		mergeChildren(other.getChildren());

	}

	private void mergeChildren(Set<UrlPart> children) {
		children.forEach(child -> {
			if (this.children.contains(child)) {
				for (UrlPart urlPart : this.children) {
					if (urlPart.equals(child)) {
						urlPart.merge(child);
					}
				}
			} else {
				this.children.add(child);
			}
		});
	}

	private void mergeParamMap(Map<String, Set<String>> paramMap) {
		for (Map.Entry<String, Set<String>> entry : paramMap.entrySet()) {
			if (this.params.containsKey(entry.getKey())) {
				HashSet<String> newSet = new HashSet<>(this.params.get(entry.getKey()));
				newSet.addAll(entry.getValue());
				this.params.put(entry.getKey(), newSet);
			} else {
				this.params.put(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((urlPartString == null) ? 0 : urlPartString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (object.getClass() != this.getClass()) {
			return false;
		}
		final UrlPart other = (UrlPart) object;

		if (!StringUtils.equals(this.urlPartString, other.getUrlPartString())) {
			return false;
		}
		return true;
	}

	public boolean hasChildren() {
		return this.children.size() > 0;
	}

	public void removeChildren() {
		this.children = new HashSet<UrlPart>();
	}
}
