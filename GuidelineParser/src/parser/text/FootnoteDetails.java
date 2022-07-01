package parser.text;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.*;

import parser.config.ConfigProperty;

public class FootnoteDetails {

	private String footnoteText; //storing the footnote definitions and links associated.
	private Set<String> footnoteLinkKeys = null;
	
	public FootnoteDetails(String text) //taking the pagefootnotes hashmap
	{
		this.footnoteText = text;
		this.footnoteLinkKeys = new HashSet<String>();
		String footnoteRegex=ConfigProperty.getProperty("footnote.link.regex");
		Pattern pattern= Pattern.compile(footnoteRegex); // regex patterns
		Matcher m= pattern.matcher(footnoteText);
		while (m.find())
		{	
			String linkKey = footnoteText.substring(m.start(), m.end());
			footnoteLinkKeys.add(linkKey);	//adding in the set	
		}
	}
	
	public String getFootNoteText() 
	{
		return footnoteText;
	}
	
	public Set<String> getLinkKeys()
	{
		return footnoteLinkKeys;	
	}
}

