package model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;

public class UrlPart {

	private String urlPartString;
	private Map<String, Set<String>> params;
	private Set<String> httpTyps = new HashSet<String>();
	private Set<UrlPart> children = new HashSet<UrlPart>();

	public UrlPart(String url, String httpTyp) {
		this.urlPartString = url;
		this.httpTyps.add(httpTyp.toUpperCase());
		this.params = new HashedMap<String, Set<String>>();
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

	public Set<String> getHttpTyps() {
		return this.httpTyps;
	}

	public Set<UrlPart> getChildren() {
		return children;
	}

	public Map<String, Set<String>> getParamMap() {
		return params;
	}

	public void addUrlTyps(Set<String> httpTyps) {
		httpTyps.addAll(httpTyps);
	}

	public void addParamMapToLastChild(Map<String, Set<String>> paramMap) {
		if (CollectionUtils.sizeIsEmpty(children)) {
			this.params = paramMap;
		} else {
			children.iterator().next().addParamMapToLastChild(paramMap);
		}
	}

	public void merge(UrlPart urlPart) {
		if (!equals(urlPart)) {
			return;
		}
		httpTyps.addAll(urlPart.getHttpTyps());
		mergeParamMap(urlPart.getParamMap());
		mergeChildren(urlPart.getChildren());

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

		if (!this.urlPartString.equals(other.getUrlPartString())) {
			return false;
		}
		return true;
	}
}
