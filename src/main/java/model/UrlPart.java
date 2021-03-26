package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;

public class UrlPart {

	private String urlPartString;
	private Map<String, Set<String>> params = new HashMap<String, Set<String>>();
	private Set<HTTPType> httpTyps = new HashSet<HTTPType>();

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
		} else {
			this.urlPartString = url;
		}
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
			this.params = paramMap;
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
