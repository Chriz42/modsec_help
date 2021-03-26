package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class LocationMatch implements Serializable {

	private String urlString = StringUtils.EMPTY;
	private HashMap<String, String> params = new HashMap<>();
	private Set<HTTPType> httpTyps = new HashSet<HTTPType>();

	public String getUrlString() {
		return this.urlString;
	}

	public void concatUrl(String urlPartString) {
		this.urlString = this.urlString + "/" + urlPartString;
	}

	public void addParam(String key, String regex) {
		this.params.put(key, regex);
	}

	public void addHttpTyps(Set<HTTPType> set) {
		this.httpTyps = set;
	}

	public Set<HTTPType> getHttpTyps() {
		return httpTyps;
	}

	public HashMap<String, String> getParams() {
		return params;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((urlString == null) ? 0 : urlString.hashCode());
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
		final LocationMatch other = (LocationMatch) object;

		if (!StringUtils.equals(this.urlString, other.getUrlString())) {
			return false;
		}
		return true;
	}
}
