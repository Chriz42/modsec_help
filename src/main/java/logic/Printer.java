package logic;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import model.LocationMatch;

public class Printer {

	// TODO: make actions configurable
	// TODO: add posibillity for anomalyscoring
	// TODO: addpossebillity to log matched value, with masking for sensitiv data
	// TODO: make startRuleID configureable

	final String locationMatchOpenString = "<LocationMatch \"^%s$\">";
	final String requestMethodeString = "\tSecRule REQUEST_METHOD !(%s) \"deny,id:%s,msg:'Request method not allowed'\"";
	final String unexpectedParamNameString = "\tSecRule ARGS_NAMES !^(%s)$ \"deny,id:%s,msg:'The request contained the following unexpected Param: %%{MATCHED_VAR_NAME}'\"";
	final String containsInvalidCharsString = "\tSecRule ARGS:%s !^[%s]+$ \"deny,id:%s,msg:'The Parameter %%{MATCHED_VAR_NAME} contains invalid characters'\"";
	final String requestAllowString = "\tSecAction \"allow,id:%s,msg:'Request passed',nolog\"";
	final String locationMatchCloseString = "</LocationMatch>";

	final String unauthLocationCalledSecActionString = "\tSecAction \"deny,id:%s,msg:'Unauthorized location was called: %%{REQUEST_URI}'\"";

	final String defaultRuleUrlString = "/.*";

	private int currentRuleId;

	public Printer(int currentRuleId) {
		this.currentRuleId = currentRuleId;
	}

	public void printToStream(LocationMatch locationMatch, OutputStream outStream) {
		PrintWriter printWriter = new PrintWriter(outStream);
		printWriter.println();
		printWriter.println(String.format(locationMatchOpenString, locationMatch.getUrlString()));

		addHttpMethode(locationMatch.getHttpTyps(), printWriter);
		addExpectedParamNames(locationMatch.getParams(), printWriter);
		addExpectedParamValues(locationMatch.getParams(), printWriter);
		printWriter.println(String.format(requestAllowString, currentRuleId++));

		printWriter.println(locationMatchCloseString);
		printWriter.close();

	}

	void addExpectedParamValues(HashMap<String, String> params, PrintWriter printWriter) {
		for (Map.Entry entry : params.entrySet()) {
			String line = String.format(containsInvalidCharsString, entry.getKey(), entry.getValue(), currentRuleId++);
			printWriter.println(line);
		}

	}

	void addExpectedParamNames(HashMap<String, String> params, PrintWriter printWriter) {
		String paramNamesString = StringUtils.EMPTY;
		if (params.size() > 1) {
			paramNamesString = StringUtils.join(params.keySet(), "|");
		} else {
			paramNamesString = params.entrySet().iterator().next().getKey();
		}
		printWriter.println(String.format(unexpectedParamNameString, paramNamesString, currentRuleId++));
	}

	void addHttpMethode(Set<String> httpTypes, PrintWriter printWriter) {
		String httpTypesString = StringUtils.EMPTY;
		if (httpTypes.size() > 1) {
			httpTypesString = StringUtils.join(httpTypes, "|");
		} else {
			httpTypesString = httpTypes.iterator().next();
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
