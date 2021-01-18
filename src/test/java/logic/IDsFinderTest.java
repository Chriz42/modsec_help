package logic;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import model.UrlPart;

public class IDsFinderTest {

// @formatter:off
	
	/*
	 * 	First with one number:
	 *    /User/12/view
	 *    /User/67/view
	 *    /User/9999999999/view
	 *    /User/12/edit
	 *    /User/67/edit
	 *    /User/9999999999/edit
	 *    
	 *    to:
	 *    /User/[0-9]+/view
	 *    /User/[0-9]+/edit
	 *    
	 * 	Second: With x numbers    
	 	TODO:Do it
	 */
	
// @formatter:on
	@Test
	public void singleIDUrlTest() {
		List<UrlPart> urlList = new ArrayList<UrlPart>();
		IDsFinder finder = new IDsFinder();
		List<String> regexedList = finder.findIDs(urlList);
	}

}
