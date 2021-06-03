package model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.collections4.map.HashedMap;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsMapWithSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UrlPartTest {

	@Test
	public void mergeTest() {
		UrlPart parent = new UrlPart("eins", HTTPType.POST);
		UrlPart child = new UrlPart("zwei", HTTPType.POST);
		Map<String, Set<String>> paramMap = new HashedMap<>();
		paramMap.put("param1", Set.of("value1", "value2"));
		child.addParamMap(paramMap);

		parent.addChild(child);

		UrlPart newParent = new UrlPart("eins", HTTPType.POST);
		UrlPart newChild = new UrlPart("zwei", HTTPType.POST);
		newParent.addChild(newChild);
		Map<String, Set<String>> paramMap2 = new HashedMap<>();
		paramMap2.put("param1", Set.of("value2", "value3"));
		newChild.addParamMap(paramMap2);

		parent.merge(newParent);

		assertThat(parent.getUrlPartString(), is("eins"));
		assertThat(parent.getHttpTyps(), is(Set.of(HTTPType.POST)));
		Set<UrlPart> children = parent.getChildren();
		assertThat(children, is(IsCollectionWithSize.hasSize(1)));

		UrlPart nextChild = children.iterator().next();
		assertThat(nextChild.getUrlPartString(), is("zwei"));
		assertThat(nextChild.getHttpTyps(), is(Set.of(HTTPType.POST)));
		Map<String, Set<String>> params = child.getParamMap();
		assertThat(params, is(IsMapWithSize.aMapWithSize(1)));
		assertThat(params.get("param1"), hasItems("value1", "value2", "value3"));
	}

	@Test
	public void mergeNumericUrlParts() {
		UrlPart parent = new UrlPart("User", HTTPType.POST);
		UrlPart child = new UrlPart("12", HTTPType.POST);
		Map<String, Set<String>> paramMap = new HashedMap<>();
		paramMap.put("param1", Set.of("value1", "value2"));
		child.addParamMap(paramMap);

		parent.addChild(child);

		UrlPart newParent = new UrlPart("User", HTTPType.POST);
		UrlPart newChild = new UrlPart("33", HTTPType.POST);
		newParent.addChild(newChild);
		Map<String, Set<String>> paramMap2 = new HashedMap<>();
		paramMap2.put("param1", Set.of("value2", "value3"));
		newChild.addParamMap(paramMap2);

		parent.merge(newParent);

		assertThat(parent.getUrlPartString(), is("User"));
		assertThat(parent.getHttpTyps(), is(Set.of(HTTPType.POST)));
		Set<UrlPart> children = parent.getChildren();
		assertThat(children, is(IsCollectionWithSize.hasSize(1)));

		UrlPart nextChild = children.iterator().next();
		assertThat(nextChild.getUrlPartString(), is("[0-9]+"));
		assertThat(nextChild.getHttpTyps(), is(Set.of(HTTPType.POST)));
		Map<String, Set<String>> params = child.getParamMap();
		assertThat(params, is(IsMapWithSize.aMapWithSize(1)));
		assertThat(params.get("param1"), hasItems("value1", "value2", "value3"));
	}

	@Test
	public void mergeDifferentHTTPTypsTest() {
		UrlPart parent = new UrlPart("User", HTTPType.POST);
		UrlPart child = new UrlPart("12", HTTPType.POST);
		Map<String, Set<String>> paramMap = new HashedMap<>();
		paramMap.put("param1", Set.of("value1", "value2"));
		child.addParamMap(paramMap);

		parent.addChild(child);

		UrlPart newParent = new UrlPart("User", HTTPType.GET);
		UrlPart newChild = new UrlPart("33", HTTPType.GET);
		newParent.addChild(newChild);
		Map<String, Set<String>> paramMap2 = new HashedMap<>();
		paramMap2.put("param1", Set.of("value2", "value3"));
		newChild.addParamMap(paramMap2);

		parent.merge(newParent);

		assertThat(parent.getUrlPartString(), is("User"));
		assertThat(parent.getHttpTyps(), containsInAnyOrder(HTTPType.POST, HTTPType.GET));
		Set<UrlPart> children = parent.getChildren();
		assertThat(children, is(IsCollectionWithSize.hasSize(1)));

		UrlPart nextChild = children.iterator().next();
		assertThat(nextChild.getUrlPartString(), is("[0-9]+"));
		assertThat(nextChild.getHttpTyps(), containsInAnyOrder(HTTPType.POST, HTTPType.GET));
		Map<String, Set<String>> params = child.getParamMap();
		assertThat(params, is(IsMapWithSize.aMapWithSize(1)));

		assertThat(params.get("param1"), hasItems("value1", "value2", "value3"));
	}

	@Test
	void addParamMaptoLastChildTest() {
		Set<String> valueSet = Set.of("value1", "value2");
		Map<String, Set<String>> paramMap = new HashedMap<>();
		paramMap.put("Key", valueSet);
		UrlPart parent = new UrlPart("parent", HTTPType.POST);
		UrlPart child = new UrlPart("child", HTTPType.POST);
		parent.addChild(child);
		parent.addParamMapToLastChild(paramMap);
		assertThat(child.getParamMap(), is(IsMapWithSize.aMapWithSize(1)));
	}

	@ParameterizedTest
	@MethodSource
	void differentUrlPartRegexTest(String rawUrlPart, String urlpartString) {
		UrlPart urlPart = new UrlPart(rawUrlPart);
		assertThat(urlPart.getUrlPartString(), is(urlpartString));

	}

	private static Stream<Arguments> differentUrlPartRegexTest() {
		return Stream.of(Arguments.of("simpleString", "simpleString"), Arguments.of("5678", "[0-9]+"),
				Arguments.of("4a1e3fb1-ea81-4eee-a691-b31c059fbe0c", UrlPart.UUIDRegexStringForUrlpart),
				// removed one letter from uuid
				Arguments.of("4a1e3fb1-ea81-4eee-a691-b31c059fb0c", "4a1e3fb1-ea81-4eee-a691-b31c059fb0c"),
				// add a U inside UUID (only Hex a-f is permitted
				// TODO is this to strict? UUIds are difiend like this but for the real world...
				Arguments.of("4U1e3fb1-ea81-4eee-a691-b31c059fbe0c", "4U1e3fb1-ea81-4eee-a691-b31c059fbe0c"));
	}
}
