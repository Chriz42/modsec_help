package logic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.Test;

import model.LocationMatch;
import model.UrlPart;

public class LocationMatchCreatorTest {

//	TODO: add negativ tests
	@Test
	public void createSingleLocationMatchTest() {
		UrlPart parent = new UrlPart("one");
		UrlPart child = new UrlPart("two");
		UrlPart newChild = new UrlPart("three");
		UrlPart newChild1 = new UrlPart("four");
		UrlPart newChild2 = new UrlPart("five", "Post");
		Map<String, Set<String>> paramMap = new HashedMap<>();
		paramMap.put("param1", Set.of("value1", "value2"));
		newChild2.addParamMap(paramMap);
		child.addChild(newChild);
		newChild.addChild(newChild1);
		newChild1.addChild(newChild2);
		parent.addChild(child);

		LocationMatchCreator creator = new LocationMatchCreator();
		Set<LocationMatch> matches = creator.createLocationMatch(parent);
		assertThat(matches, is(IsCollectionWithSize.hasSize(1)));
		LocationMatch locationMatch = matches.iterator().next();

		assertThat(locationMatch, is(notNullValue()));
		assertThat(locationMatch.getUrlString(), is("/one/two/three/four/five"));
		assertThat(locationMatch.getHttpTyps(), Matchers.hasItem("POST"));
		HashMap<String, String> params = locationMatch.getParams();
		assertThat(params.size(), is(1));
		assertTrue(params.get("param1").contains("a-z"));
		assertTrue(params.get("param1").contains("0-9"));
	}

	@Test
	public void createLocationMatchesFromUrlPartWithBranchesTest() {
		UrlPart parent = new UrlPart("one");
		UrlPart child = new UrlPart("two");
		UrlPart newChild = new UrlPart("three", "post");
		UrlPart newChild1 = new UrlPart("four");
		UrlPart newChild2 = new UrlPart("five", "Post");
		child.addChild(newChild);
		newChild1.addChild(newChild2);

		Map<String, Set<String>> paramMap = new HashedMap<>();
		paramMap.put("param1", Set.of("value1", "value2"));
		newChild2.addParamMap(paramMap);

		Map<String, Set<String>> paramMap2 = new HashedMap<>();
		paramMap2.put("param1", Set.of("value1", "value2"));
		newChild.addParamMap(paramMap2);

		parent.addChild(newChild1);
		parent.addChild(child);

		LocationMatchCreator creator = new LocationMatchCreator();
		Set<LocationMatch> matches = creator.createLocationMatch(parent);
		assertThat(matches, is(IsCollectionWithSize.hasSize(2)));

		for (LocationMatch locationMatch : matches) {
			assertThat(locationMatch, is(notNullValue()));

			assertThat(locationMatch.getUrlString(), is(oneOf("/one/two/three", "/one/four/five")));
			assertThat(locationMatch.getHttpTyps(), Matchers.hasItem("POST"));
			HashMap<String, String> params = locationMatch.getParams();
			assertThat(params.size(), is(1));
			assertThat(params.get("param1"), allOf(containsString("a-z"), containsString("0-9")));
		}

	}

	@Test
	public void createMultipleLocationMatchesFromSingleBranchTest() {
		UrlPart parent = new UrlPart("one", "pUT");
		UrlPart child = new UrlPart("two");
		UrlPart newChild = new UrlPart("three", "get");
		UrlPart newChild1 = new UrlPart("four");
		UrlPart newChild2 = new UrlPart("five", "Post");
		child.addChild(newChild);
		newChild.addChild(newChild1);
		newChild1.addChild(newChild2);
		parent.addChild(child);

		LocationMatchCreator creator = new LocationMatchCreator();
		Set<LocationMatch> matches = creator.createLocationMatch(parent);

		assertThat(matches, is(IsCollectionWithSize.hasSize(3)));
		for (LocationMatch locationMatch : matches) {
			assertThat(locationMatch.getUrlString(), is(oneOf("/one", "/one/two/three", "/one/two/three/four/five")));
			assertThat(locationMatch.getHttpTyps(), is(oneOf(Set.of("POST"), Set.of("GET"), Set.of("PUT"))));
		}
	}

	@Test
	public void createLocationMatchesFromListTest() {
		HashSet<UrlPart> parents = new HashSet<>();

		parents.add(new UrlPart("one", "pUT"));
		parents.add(new UrlPart("two", "post"));
		parents.add(new UrlPart("three", "get"));

		LocationMatchCreator creator = new LocationMatchCreator();
		Set<LocationMatch> matches = creator.createListOfLocationMatch(parents);
		assertThat(matches, is(IsCollectionWithSize.hasSize(3)));
		for (LocationMatch locationMatch : matches) {
			assertThat(locationMatch.getUrlString(), is(oneOf("/one", "/two", "/three")));
			assertThat(locationMatch.getHttpTyps(), is(oneOf(Set.of("POST"), Set.of("GET"), Set.of("PUT"))));
		}
	}

}
