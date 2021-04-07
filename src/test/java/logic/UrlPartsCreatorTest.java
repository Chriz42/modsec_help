package logic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsMapWithSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import logic.exceptions.UrlPartCreatorException;
import model.HTTPType;
import model.UrlPart;

class UrlPartsCreatorTest {

	// TODO:Sauber formatieren

	@ParameterizedTest
	@MethodSource
	void parseParameterStringTest(String line, List<String> paramNames, List<Set<String>> values) {
		UrlPartsCreator creator = new UrlPartsCreator();
		Map<String, Set<String>> paramMap = creator.createParameterMap(line);
		for (int i = 0; i < paramNames.size(); i++) {
			assertThat(paramMap.get(paramNames.get(0)), is(values.get(0)));
		}
	}

	private static Stream<Arguments> parseParameterStringTest() {
		return Stream.of(Arguments.of("param1=value1", Arrays.asList("param1"), Arrays.asList(Set.of("value1"))),
				Arguments.of("param1=value1&param1=value2", Arrays.asList("param1"),
						Arrays.asList(Set.of("value1", "value2"))),
				Arguments.of("param1=value1&param1=value1", Arrays.asList("param1"), Arrays.asList(Set.of("value1"))),
				Arguments.of("param1=value1&param2=value2", Arrays.asList("param1", "param2"),
						Arrays.asList(Set.of("value1"), Set.of("value2")),
						Arguments.of("param1=value1&param2=value2&param3=value3",
								Arrays.asList("param1", "param2", "param3"),
								Arrays.asList(Set.of("value1"), Set.of("value2"), Set.of("value3")))));
	}

	@Test
	void parseMultipleParameterStrings() {
		Set<String> stringList = Set.of("param1=value1&param2=value1", "param1=value1&param2=kekse", "kekse=super",
				"param1=value1&param2=value2", "param1=value1&param2=E68uhj", "param1=value1&pm2=E68uhj",
				"value1=12&param2=value2");
		UrlPartsCreator creator = new UrlPartsCreator();
		Map<String, Set<String>> paramMap = creator.createParameterMap(stringList);
		assertThat(paramMap.get("param1"), is(Set.of("value1")));
		assertThat(paramMap.get("param2"), is(Set.of("value1", "kekse", "value2", "E68uhj")));
		assertThat(paramMap.get("kekse"), is(Set.of("super")));
		assertThat(paramMap.get("pm2"), is(Set.of("E68uhj")));
		assertThat(paramMap.get("value1"), is(Set.of("12")));

	}

	@ParameterizedTest
	@MethodSource
	void urlAndTypTest(String line, List<HTTPType> typs, List<String> urls) throws UrlPartCreatorException {
		UrlPartsCreator creator = new UrlPartsCreator();
		UrlPart urlPart = creator.parseUrlandTyp(line);
		assertThat(urlPart.getUrlPartString(), is(equalTo(urls.get(0))));

		// little workaround to use parameterized tests
		if (typs.get(0) == null) {
			assertThat(urlPart.getHttpTyps(), is(IsCollectionWithSize.hasSize(0)));
		} else {
			assertThat(urlPart.getHttpTyps(), Matchers.hasItem(typs.get(0)));
		}

		Set<UrlPart> children = urlPart.getChildren();
		for (int i = 1; i < urls.size(); i++) {
			if (CollectionUtils.isNotEmpty(children)) {
				assertThat(children, is(IsCollectionWithSize.hasSize(1)));
				UrlPart child = children.iterator().next();
				assertThat(child.getUrlPartString(), is(equalTo(urls.get(i))));
				// little workaround to use parameterized tests
				if (typs.get(i) == null) {
					assertThat(child.getHttpTyps(), is(IsCollectionWithSize.hasSize(0)));
				} else {
					assertThat(child.getHttpTyps(), Matchers.hasItem(typs.get(i)));
				}
				children = child.getChildren();
			}

		}

	}

	private static Stream<Arguments> urlAndTypTest() {
		return Stream.of(Arguments.of("GET / HTTP/1.1", Arrays.asList(HTTPType.GET), Arrays.asList("/")),
				Arguments.of("POST eins/test.php HTTP/1.1", Arrays.asList(null, HTTPType.POST),
						Arrays.asList("eins", "test.php")),
				Arguments.of("POST test.php HTTP/1.1", Arrays.asList(HTTPType.POST), Arrays.asList("test.php")),
				Arguments.of("POST /eins/zwei/drei/test.php HTTP/1.1", Arrays.asList(null, null, null, HTTPType.POST),
						Arrays.asList("eins", "zwei", "drei", "test.php")));
	}

	@Test
	public void createUrlPartWithQueryString() throws UrlPartCreatorException {
		UrlPartsCreator creator = new UrlPartsCreator();
		UrlPart urlPart = creator.parseUrlandTyp("GET test?param1=test1&param2=test2 HTTP/1.1");
		assertThat(urlPart.getUrlPartString(), is(equalTo("test")));

		assertThat(urlPart.getUrlPartString(), is("test"));
		assertThat(urlPart.getHttpTyps(), Matchers.hasItem(HTTPType.GET));
		Map<String, Set<String>> urlPartParamMap = urlPart.getParamMap();
		assertThat(urlPartParamMap.get("param1"), is(Set.of("test1")));
		assertThat(urlPartParamMap.get("param2"), is(Set.of("test2")));
	}

	@Test
	public void createUrlPartWithEmptyQueryString() throws UrlPartCreatorException {
		UrlPartsCreator creator = new UrlPartsCreator();
		UrlPart urlPart = creator.parseUrlandTyp("GET test? HTTP/1.1");
		assertThat(urlPart.getUrlPartString(), is(equalTo("test")));

		assertThat(urlPart.getUrlPartString(), is("test"));
		assertThat(urlPart.getHttpTyps(), Matchers.hasItem(HTTPType.GET));
		Map<String, Set<String>> urlPartParamMap = urlPart.getParamMap();
		assertThat(urlPartParamMap, is(IsMapWithSize.anEmptyMap()));
	}

	@Test
	public void createUrlPartWithQueryStringFromPostErrorHandling() throws UrlPartCreatorException {

		UrlPartCreatorException exception = assertThrows(UrlPartCreatorException.class, () -> {
			UrlPartsCreator creator = new UrlPartsCreator();
			creator.parseUrlandTyp("POST test?param1=test1&param2=test2 HTTP/1.1");
		});
		String expectedMessage = "There shouldn't be a non GET request with querystring";
		String actualMessage = exception.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
	}

	@ParameterizedTest
	@MethodSource
	public void createUrlResourcePlaceholderUrlPart(String line, List<Set<HTTPType>> httpTyps, List<String> urls)
			throws UrlPartCreatorException {
		UrlPartsCreator creator = new UrlPartsCreator();
		UrlPart urlPart = creator.parseUrlandTyp(line);
		assertThat(urlPart.getHttpTyps(), is(httpTyps.get(0)));
		assertThat(urlPart.getUrlPartString(), is(urls.get(0)));
		assertThat(urlPart.getChildren(), is(IsCollectionWithSize.hasSize(1)));

		UrlPart child = urlPart.getChildren().iterator().next();

		assertThat(child.getHttpTyps(), is(httpTyps.get(1)));
		assertThat(child.getUrlPartString(), is(urls.get(1)));

	}

	private static Stream<Arguments> createUrlResourcePlaceholderUrlPart() {
		return Stream.of(
				Arguments.of("GET /js/11.m.da9195e4.chunk.js HTTP/1.1",
						Arrays.asList(SetUtils.emptySet(), Set.of(HTTPType.GET)), Arrays.asList("js", "*.js")),
				Arguments.of("GET /css/11.m.das.chunk.css HTTP/1.1",
						Arrays.asList(SetUtils.emptySet(), Set.of(HTTPType.GET)), Arrays.asList("css", "*.css")),
				Arguments.of("GET /resources/gsdgdsg.jpg HTTP/1.1",
						Arrays.asList(SetUtils.emptySet(), Set.of(HTTPType.GET)), Arrays.asList("resources", "*.*")));
	}

	@ParameterizedTest
	@MethodSource
	public void createUrlResourcePlaceholderUrlPartErrors(String line) {
		UrlPartCreatorException exception = assertThrows(UrlPartCreatorException.class, () -> {
			UrlPartsCreator creator = new UrlPartsCreator();
			creator.parseUrlandTyp(line);
		});
		String expectedMessage = "There shouldn't be a non GET request on static resources";
		String actualMessage = exception.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
	}

	private static Stream<Arguments> createUrlResourcePlaceholderUrlPartErrors() {
		return Stream.of(Arguments.of("POST /js/11.m.das.chunk.js HTTP/1.1"),
				Arguments.of("POST /css/11.m.das.chunk.css HTTP/1.1"), Arguments.of("POST /resources/dasg.png"));
	}

	@Test
	void parseRAWData() {
		UrlPartsCreator creator = new UrlPartsCreator();
		Map<String, Set<String>> parsedLines = new HashedMap<String, Set<String>>();
		parsedLines.put("POST /eins HTTP/1.1", Set.of("param1=value1&param2=value2", "param3=value3&param2=value22"));

		parsedLines.put("POST /eins/zwei HTTP/1.1",
				Set.of("param1=value1&param2=value2", "param3=value3&param2=value22"));

		parsedLines.put("GET /drei?param3=value3 HTTP/1.1", SetUtils.emptySet());
		List<UrlPart> urlPartsList = creator.parseRAWData(parsedLines);

		UrlPart eins = urlPartsList.stream().filter(urlPart -> urlPart.getUrlPartString().equals("eins")).findFirst()
				.get();

		assertThat(eins.getUrlPartString(), is("eins"));
		assertThat(eins.getHttpTyps(), Matchers.hasItem(HTTPType.POST));
		Map<String, Set<String>> einsParamMap = eins.getParamMap();
		assertThat(einsParamMap.get("param1"), is(Set.of("value1")));
		assertThat(einsParamMap.get("param2"), is(Set.of("value2", "value22")));
		assertThat(einsParamMap.get("param3"), is(Set.of("value3")));

		UrlPart zwei = eins.getChildren().stream().filter(urlPart -> urlPart.getUrlPartString().equals("zwei"))
				.findFirst().get();
		assertThat(zwei.getUrlPartString(), is("zwei"));
		assertThat(zwei.getHttpTyps(), Matchers.hasItem(HTTPType.POST));
		Map<String, Set<String>> zweiParamMap = zwei.getParamMap();
		assertThat(zweiParamMap.get("param1"), is(Set.of("value1")));
		assertThat(zweiParamMap.get("param2"), is(Set.of("value2", "value22")));
		assertThat(zweiParamMap.get("param3"), is(Set.of("value3")));

		UrlPart drei = urlPartsList.stream().filter(urlPart -> urlPart.getUrlPartString().equals("drei")).findFirst()
				.get();
		assertThat(drei.getUrlPartString(), is("drei"));
		assertThat(drei.getHttpTyps(), Matchers.hasItem(HTTPType.GET));
		Map<String, Set<String>> dreiParamMap = drei.getParamMap();
		assertThat(dreiParamMap.get("param3"), is(Set.of("value3")));
	}

}
