package input;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FileParser {

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
			}
			if (line.contains("-C--")) {
				String paramLine = reader.readLine();
				if (dataMap.containsKey(urlLine) && !dataMap.get(urlLine).contains(paramLine)) {
					dataMap.get(urlLine).add(paramLine);
				}
			}
			if (line.contains("-F--")) {
				String returnCodeLine = reader.readLine();
				String[] returnCodeLIneParts = returnCodeLine.split(" ");
				// remove entries with 4** returncodes except 403
				if (returnCodeLIneParts[1].matches("^4\\d\\d$") && returnCodeLIneParts[1].matches("!^403$")) {
					dataMap.remove(urlLine);
				}
			}
		}

		return dataMap;
	}
}
