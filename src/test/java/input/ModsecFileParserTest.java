package input;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsMapWithSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import input.exceptions.FileParserException;
import logic.RegexCreator;
import model.HTTPType;
import model.UrlPart;

@ExtendWith(MockitoExtension.class)
public class ModsecFileParserTest {

	@Mock
	private RegexCreator regexCreatorMock;
	ModsecFileParser parser;

	@BeforeEach
	public void init() {
		parser = new ModsecFileParser();
		parser.regexCreator = regexCreatorMock;

	}

	@Test
	public void parseParamNameLineTest() {
		Map<String, Set<String>> paramMap = new HashMap<String, Set<String>>();
		parser.addParamNames(paramMap,
				"SecRule ARGS_NAMES !^(firstName|lastName)$ \"deny,id:31280209,msg:'The request contained the following unexpected Param: %{MATCHED_VAR_NAME}'\"");
		assertThat(paramMap, is(IsMapWithSize.aMapWithSize(2)));
		assertThat(paramMap, hasKey("firstName"));
		assertThat(paramMap, hasKey("lastName"));
	}

	@Test
	public void parseParamValueLineTest() throws FileParserException {
		when(regexCreatorMock.createStringFromRegex(anyString())).thenAnswer(i -> i.getArguments()[0]);
		Map<String, Set<String>> paramMap = new HashMap<String, Set<String>>();
		paramMap.put("firstName", new HashSet<String>());
		paramMap.put("lastName", new HashSet<String>());
		parser.addParamValue(paramMap,
				"SecRule ARGS:firstName !^[a-zA-Z]+$ \"deny,id:31280210,msg:'The Parameter %{MATCHED_VAR_NAME} contains invalid characters'\"");
		parser.addParamValue(paramMap,
				"SecRule ARGS:lastName !^[a-zA-Z]+$ \"deny,id:31280210,msg:'The Parameter %{MATCHED_VAR_NAME} contains invalid characters'\"");
		assertThat(paramMap, is(IsMapWithSize.aMapWithSize(2)));
		assertThat(paramMap.get("firstName"), is(Set.of("a-zA-Z")));
		assertThat(paramMap.get("lastName"), is(Set.of("a-zA-Z")));
	}

	@Test
	public void parseSingleHttpTypeTest() {

		UrlPart urlPart = new UrlPart("eins");
		urlPart.addChild(new UrlPart("zwei"));

		parser.addHttpTypeToLastChild(urlPart,
				"SecRule REQUEST_METHOD !(GET) \"deny,id:31280208,msg:'Request method not allowed'\"");

		assertThat(urlPart.getHttpTyps(), IsEmptyCollection.empty());
		UrlPart child = urlPart.getChildren().iterator().next();
		assertThat(child.getHttpTyps(), is(Matchers.hasItem(HTTPType.GET)));

	}

	@Test
	public void parseMultipleHttpTypeTest() {
		UrlPart urlPart = new UrlPart("eins");
		urlPart.addChild(new UrlPart("zwei"));

		parser.addHttpTypeToLastChild(urlPart,
				"SecRule REQUEST_METHOD !(GET|POST|PUT) \"deny,id:31280208,msg:'Request method not allowed'\"");

		assertThat(urlPart.getHttpTyps(), IsEmptyCollection.empty());
		UrlPart child = urlPart.getChildren().iterator().next();
		assertThat(child.getHttpTyps(), is(Matchers.hasItems(HTTPType.GET, HTTPType.POST, HTTPType.PUT)));
	}

	@Test
	public void createUrlPartsFromStringTest() {

		UrlPart urlPart = parser.createUrlParts("configuration/url");
		assertThat(urlPart.getUrlPartString(), is("configuration"));
		assertThat(urlPart.getHttpTyps(), IsEmptyCollection.empty());
		Map<String, Set<String>> urlPartParamMap = urlPart.getParamMap();
		assertThat(urlPartParamMap, is(IsMapWithSize.anEmptyMap()));

		Set<UrlPart> children = urlPart.getChildren();
		assertThat(children, is(IsCollectionWithSize.hasSize(1)));

		UrlPart firstChild = children.iterator().next();
		assertThat(firstChild.getUrlPartString(), is("url"));
		assertThat(firstChild.getHttpTyps(), IsEmptyCollection.empty());
	}

	@Test
	public void modsecFileTest() throws IOException, FileParserException {
		when(regexCreatorMock.createStringFromRegex(anyString())).thenAnswer(i -> i.getArguments()[0]);
		InputStream inputStream = ModsecFileParserTest.class.getResourceAsStream("modsec.conf");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		List<UrlPart> list = parser.parseFile(reader);
		assertThat(list, is(IsCollectionWithSize.hasSize(2)));

		UrlPart first = list.get(0);
		if (first.getUrlPartString().equals("test")) {
			testFirst(first);
			testSecond(list.get(1));
		} else {
			testFirst(list.get(1));
			testSecond(first);
		}

	}

	private void testFirst(UrlPart urlPart) {
		assertThat(urlPart.getUrlPartString(), is("test"));
		assertThat(urlPart.getHttpTyps(), IsEmptyCollection.empty());
		Map<String, Set<String>> urlPartParamMap = urlPart.getParamMap();
		assertThat(urlPartParamMap, is(IsMapWithSize.anEmptyMap()));

		Set<UrlPart> children = urlPart.getChildren();
		assertThat(children, is(IsCollectionWithSize.hasSize(1)));

		UrlPart firstChild = children.iterator().next();
		assertThat(firstChild.getUrlPartString(), is("accounts"));
		assertThat(firstChild.getHttpTyps(), IsEmptyCollection.empty());
		Map<String, Set<String>> firstChildParamMap = urlPart.getParamMap();
		assertThat(firstChildParamMap, is(IsMapWithSize.anEmptyMap()));

		Set<UrlPart> firstChildchildren = firstChild.getChildren();
		assertThat(firstChildchildren, is(IsCollectionWithSize.hasSize(1)));

		UrlPart secondChild = firstChildchildren.iterator().next();
		assertThat(secondChild.getUrlPartString(), is("[a-fA-F0-9\\-]+"));
		assertThat(secondChild.getHttpTyps(), is(Matchers.hasItem(HTTPType.GET)));
		Map<String, Set<String>> secondChildParamMap = secondChild.getParamMap();
		assertThat(secondChildParamMap, is(IsMapWithSize.aMapWithSize(2)));
		assertThat(secondChildParamMap.get("firstName"), is(Set.of("a-zA-Z")));
		assertThat(secondChildParamMap.get("lastName"), is(Set.of("a-zA-Z")));

	}

	private void testSecond(UrlPart urlPart) {
		assertThat(urlPart.getUrlPartString(), is("configuration"));
		assertThat(urlPart.getHttpTyps(), IsEmptyCollection.empty());
		Map<String, Set<String>> urlPartParamMap = urlPart.getParamMap();
		assertThat(urlPartParamMap, is(IsMapWithSize.anEmptyMap()));

		Set<UrlPart> children = urlPart.getChildren();
		assertThat(children, is(IsCollectionWithSize.hasSize(1)));

		UrlPart firstChild = children.iterator().next();
		assertThat(firstChild.getUrlPartString(), is("url"));
		assertThat(firstChild.getHttpTyps(), Matchers.hasItem(HTTPType.POST));
		Map<String, Set<String>> childParamMap = firstChild.getParamMap();
		assertThat(childParamMap, is(IsMapWithSize.anEmptyMap()));

	}

}
