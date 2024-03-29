package logic;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.HTTPType;
import model.LocationMatch;
import system.Main;
import system.Properties;

public class Printer {

	// TODO: make actions configurable
	// TODO: add posibillity for anomalyscoring
	// TODO: addpossebillity to log matched value, with masking for sensitiv data

	private static final Logger logger = LoggerFactory.getLogger(Printer.class);

	final String locationMatchOpenString = "<LocationMatch \"^%s$\">";
	final String requestMethodeString = "\tSecRule REQUEST_METHOD !(%s) \"deny,id:%s,msg:'Request method not allowed'\"";
	final String unexpectedParamNameString = "\tSecRule ARGS_NAMES !^(%s)$ \"deny,id:%s,msg:'The request contained the following unexpected Param: %%{MATCHED_VAR_NAME}'\"";
	final String forbidQueryParamsString = "\tSecRule ARGS_GET_NAMES !^()$ \"deny,id:%s,msg:'The request contained the following unexpected Param: %%{MATCHED_VAR_NAME}'\"";
	final String containsInvalidCharsString = "\tSecRule ARGS:%s !^%s$ \"deny,id:%s,msg:'The Parameter %%{MATCHED_VAR_NAME} contains invalid characters'\"";
	final String requestAllowString = "\tSecAction \"allow,id:%s,msg:'Request passed',nolog,skip:31289999\"";
//	final String requestAllowString = "\tSecAction \"allow,id:%s,msg:'Request passed',nolog\"";
	final String locationMatchCloseString = "</LocationMatch>";

	final String unauthLocationCalledSecActionString = "\tSecAction \"deny,id:%s,msg:'Unauthorized location was called: %%{REQUEST_URI}'\"";

	final String defaultRuleUrlString = "/.*";

	boolean forbidUnknownBodyParams = Main.appProps.getBoolean(Properties.forbidUnknownBodyParams.name(), true);
	private int currentRuleId = Main.appProps.getInt(Properties.startRuleId.name(), 666666);

	public Printer() {

	}

	public Printer(int currentRuleId) {
		this.currentRuleId = currentRuleId;
	}

	public void printToStream(LocationMatch locationMatch, OutputStream outStream) {
		PrintWriter printWriter = new PrintWriter(outStream);
		printWriter.println();
		printWriter.println(String.format(locationMatchOpenString, locationMatch.getUrlString()));

		addHttpMethode(locationMatch.getHttpTyps(), printWriter);
		Set<HTTPType> httpTypes = locationMatch.getHttpTyps();
		// TODO: Check how delete and other HTTP Typs work and rework if needed
		if ((httpTypes.contains(HTTPType.POST) || httpTypes.contains(HTTPType.PUT)) && !forbidUnknownBodyParams) {
			logger.warn(
					" forbidUnknownBodyParams is set to false -> SecRules with HTTP body will accept every param with every content!");
			if (httpTypes.contains(HTTPType.GET)) {
				printWriter.println(String.format(forbidQueryParamsString, currentRuleId++));
			}
		} else {
			addExpectedParamNames(locationMatch.getParams(), printWriter);
			addExpectedParamValues(locationMatch.getParams(), printWriter);
		}
		printWriter.println(String.format(requestAllowString, currentRuleId++));

		printWriter.println(locationMatchCloseString);
		printWriter.close();

	}

	void addExpectedParamValues(HashMap<String, String> params, PrintWriter printWriter) {
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String value = entry.getValue();
			if (StringUtils.isNotEmpty(value)) {
				value = "[" + value + "]+";
			}
			String line = String.format(containsInvalidCharsString, entry.getKey(), value, currentRuleId++);
			printWriter.println(line);
		}

	}

	void addExpectedParamNames(HashMap<String, String> params, PrintWriter printWriter) {
		String paramNamesString = StringUtils.EMPTY;
		if (params.size() > 1) {
			paramNamesString = StringUtils.join(params.keySet(), "|");
		} else if (params.size() == 1) {
			paramNamesString = params.entrySet().iterator().next().getKey();
		}
		printWriter.println(String.format(unexpectedParamNameString, paramNamesString, currentRuleId++));
	}

	void addHttpMethode(Set<HTTPType> set, PrintWriter printWriter) {
		String httpTypesString = StringUtils.EMPTY;
		if (set.size() > 1) {
			Set<String> httpTypeNames = set.stream().map(value -> value.name()).collect(Collectors.toSet());
			httpTypesString = StringUtils.join(httpTypeNames, "|");
		} else {
			httpTypesString = set.iterator().next().name();
		}
		printWriter.println(String.format(requestMethodeString, httpTypesString, currentRuleId++));
	}

	public void printDefaultMatchToStream(ByteArrayOutputStream outStream) {
		PrintWriter printWriter = new PrintWriter(outStream);
		printWriter.println();
		printWriter.println(String.format(locationMatchOpenString, defaultRuleUrlString));
		printWriter.println(String.format(unauthLocationCalledSecActionString, currentRuleId++));

		printWriter.println(locationMatchCloseString);
		printWriter.close();
	}

}
