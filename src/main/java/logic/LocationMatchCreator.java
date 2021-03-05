package logic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;

import model.LocationMatch;
import model.UrlPart;

public class LocationMatchCreator {

	private final RegexCreator regexCreator = new RegexCreator();

	public Set<LocationMatch> createLocationMatch(UrlPart parent) {
		Set<LocationMatch> matches = new HashSet<>();
		LocationMatch locationMatch = new LocationMatch();
		return addParametersFromUrlParts(parent, locationMatch, matches);
	}

	public Set<LocationMatch> createListOfLocationMatch(Collection<UrlPart> parents) {
		Set<LocationMatch> matches = new HashSet<>();
		parents.parallelStream().forEach(parent -> addParametersFromUrlParts(parent, new LocationMatch(), matches));
		return matches;
	}

	Set<LocationMatch> addParametersFromUrlParts(UrlPart urlPart, LocationMatch locationMatch,
			Set<LocationMatch> matches) {

		locationMatch.concatUrl(urlPart.getUrlPartString());
		urlPart.getParamMap().entrySet().forEach(entry -> {
			locationMatch.addParam(entry.getKey(), regexCreator.getRegex(entry.getValue()));
		});
		locationMatch.addHttpTyps(urlPart.getHttpTyps());
		Set<UrlPart> children = urlPart.getChildren();
		if (CollectionUtils.isEmpty(children)) {
			matches.add(locationMatch);
			return matches;
		}
		if (children.size() > 1) {
			for (UrlPart child : children) {
				LocationMatch locationMatchClone = (LocationMatch) SerializationUtils.clone(locationMatch);
				addParametersFromUrlParts(child, locationMatchClone, matches);
			}
		}
		UrlPart child = children.iterator().next();
		if (CollectionUtils.isNotEmpty(locationMatch.getHttpTyps())) {
			LocationMatch locationMatchClone = (LocationMatch) SerializationUtils.clone(locationMatch);
			matches.add(locationMatchClone);
		}
		return addParametersFromUrlParts(child, locationMatch, matches);

	}

}