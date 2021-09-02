package input;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import system.Main;
import system.Properties;

public class LogFileParser {

	private String ALLOWED_4XX_HTTPSTATUSCODES = Main.appProps.getString(Properties.allowed400HttpStatusCodes.name(),
			"403");

	public HashMap<String, Set<String>> parse(BufferedReader reader) throws IOException {
		// TODO: Is POST boddy wrtten to -I--?Saw something inside auditlog config
		HashMap<String, Set<String>> dataMap = new HashMap<>();
		String line;
		String urlLine = "";
		while ((line = reader.readLine()) != null) {
			if (line.contains("-B--")) {
				urlLine = reader.readLine();
				dataMap.putIfAbsent(urlLine, new HashSet<>());
				continue;
			} else if (line.contains("-C--")) {
				String paramLine = reader.readLine();
				if (dataMap.containsKey(urlLine) && !dataMap.get(urlLine).contains(paramLine)) {
					dataMap.get(urlLine).add(paramLine);
				}
			} else if (line.contains("-F--")) {
				String returnCodeLine = reader.readLine();
				String[] returnCodeLIneParts = returnCodeLine.split(" ");
				// TODO: Should 500 are removed too?
				// remove entries with 4** returncodes except 403 ->allowed400HttpStatusCodes
				// inside app.properties
				if (returnCodeLIneParts[1].matches("^4\\d\\d$")
						&& !returnCodeLIneParts[1].matches("^(" + ALLOWED_4XX_HTTPSTATUSCODES + ")$")) {
					dataMap.remove(urlLine);
				}
			}
		}

		return dataMap;
	}
}
