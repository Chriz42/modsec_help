package logic;

import java.util.ArrayList;
import java.util.List;

import model.UrlPart;

public class IDsFinder {

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
		 */
		
	// @formatter:on
	public List<String> findIDs(List<UrlPart> urlList) {
		List<UrlPart> listWithIDs = new ArrayList<UrlPart>();

		return null;
	}

}
