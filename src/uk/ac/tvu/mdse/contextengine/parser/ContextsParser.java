package uk.ac.tvu.mdse.contextengine.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import uk.ac.tvu.mdse.contextengine.ContextEngine;
import android.content.Context;
import android.util.Log;

/*
 * Project: Cawefa_v2
 * Author: Anna Kocurova
 * Date: Dec 30, 2011
 */

public class ContextsParser {

	private static final String LOG_TAG = "ContextsParser";
	private boolean D = true;
	
	private static final String CONTEXT = "Context";	
	private static final String COMPOSITE_CONTEXT = "CompositeContext";	
	private static final String CONTEXT_NAME = "ContextName";
	private static final String CONTEXT_VALUE = "ContextValue";	
	private static final String APP_ID = "AppKey";
	private static final String CONTEXT_DEFINITION = "ContextDefinition";
	private static final String SPECIFIC_CVALUE = "SpecificContextValue";
	private static final String SPECIFIC_CNAME = "SpecificName";
	private static final String NUMERIC_VALUE1 = "NumericValue1";
	private static final String NUMERIC_VALUE2 = "NumericValue2";
	private static final String CONTEXT_RANGE = "Range";
	private static final String CONTEXT_RANGE_NAME = "RangeName";
	private static final String RANGE_MIN_VALUE = "Min";
	private static final String RANGE_MAX_VALUE = "Max";
	private static final String SPECIFIC_CONTEXT_VALUE = "SpeicificContextValue";
	private static final String NUMERIC_VALUE_1 = "NumericValue1";
	private static final String NUMERIC_VALUE_2 = "NumericValue2";
	private static final String CONTEXT_CHILD = "ChildContext";
	private static final String CONTEXT_CHILD_VALUE = "ChildValue";
	private static final String CONTEXT_PARENT_VALUE = "ParentValue";
	private static final String COMPOSITE_RULE = "Rule";
	private static final String CONTEXT_TYPE = "ContextType";
	
	public Context appContext;
		
	String contextName;
	String contextAssociation;
	String contextType;
	
	XmlPullParser xpp;
	ContextEngine contextEngine;

	private String currentElement;
	
	public boolean finishedReading;
	
	private String appid;
	private XMLSCV scv;
	private XMLRange range;
	
	ArrayList<String> ruleCondition;
	String ruleParentValue;
	int i;
	
	public boolean readXMLfile(Context c, String XMLsource, ContextEngine ce) throws XmlPullParserException, FileNotFoundException{
		
		Log.d(LOG_TAG, "readXMLfile" + XMLsource);
		
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		xpp = factory.newPullParser();
		
		this.appContext = c;
		this.contextEngine = ce;
		
		FileInputStream fis = new FileInputStream(XMLsource);
		
		try{
			//xpp = c.getResources().getXml(R.xml.desint_active_contexts);
			xpp.setInput(new InputStreamReader(fis));
			   xpp.next();
			   int eventType = xpp.getEventType();
			   while (eventType != XmlPullParser.END_DOCUMENT)
			   {
				   if(eventType == XmlPullParser.START_DOCUMENT)
				   {
					   if (D) Log.v(LOG_TAG, "Beginning XML Document");			     
				   }
				   else if(eventType == XmlPullParser.START_TAG)
				   {
					   currentElement = xpp.getName();
					   if (D) Log.v(LOG_TAG, "START_TAG query" +currentElement);
					   startElement(xpp.getName());							   
				   }
				   else if(eventType == XmlPullParser.END_TAG)
				   {
					   currentElement = xpp.getName();
					   if (D) Log.v(LOG_TAG, "END_TAG query" +currentElement);
					   endElement(xpp.getName());		
					   
				   }
				   else if(eventType == XmlPullParser.TEXT)
				   {
					   if (D) Log.v(LOG_TAG, "Text");
					   String text = xpp.getText();
					   if (!text.trim().equals(""))
						   addElementValue(xpp.getText());					   
				   }
				   eventType = xpp.next();
			   }
			   if (D) Log.v(LOG_TAG, "Finished XML Document");
			   finishedReading = true;
		}
		catch(Exception e){
			if (D) Log.e(LOG_TAG, "Error " + e.getLocalizedMessage());
			finishedReading = false;
		}		
		return finishedReading;
	}
	
	public void startElement(String elementName){
		
		if (elementName.equals(CONTEXT_DEFINITION)){
			appid = xpp.getAttributeValue(null, APP_ID);
			contextEngine.registerAppKey(appid);
		}
		
		if (elementName.equals(CONTEXT)){			
			
			contextType = xpp.getAttributeValue(null, CONTEXT_TYPE);
			contextName = xpp.getAttributeValue(null, CONTEXT_NAME);			
			
			if (contextType.equals("Default")){				
				contextEngine.newComponent(contextName);
			}		
//			else if (contextType.equals("Location")){
//				contextEngine.n.newComponent(contextName, "");
//			}
			else if (contextType.equals("UserPreference")){
				
				String preferenceType = "STRING";			
				contextEngine.newPreferenceComponent(contextName, preferenceType);
			}
			if (D) Log.e(LOG_TAG, "start CONTEXT "+ contextType);
		}	
		
		if (elementName.equals(COMPOSITE_CONTEXT)){
			contextName = xpp.getAttributeValue(null, CONTEXT_NAME);
			contextEngine.addComposite(contextName);
		}
		
		if (elementName.equals(COMPOSITE_RULE)){
			ruleParentValue = "";
			ruleCondition = new ArrayList<String>();
			i=0;
		}
		
	}
	
	public void endElement(String elementName){
		
		if(currentElement.equals(COMPOSITE_RULE)){
    		if (D) Log.d(LOG_TAG, "endElement COMPOSITE_RULE "+ruleCondition.size());
    		String[] conditions = new String[ruleCondition.size()];
    		int i= 0;
    		for(String s: ruleCondition){
    			conditions[i] = s; 
    			i++;
    		}
    		
    		contextEngine.newRule(contextName, conditions, ruleParentValue);
    	}
		
		if(elementName.equals(CONTEXT)){
    		contextEngine.componentDefined(contextName);
    		if (D) Log.d(LOG_TAG, "componentDefined");
		}
		
		if(elementName.equals(COMPOSITE_CONTEXT)){
    		contextEngine.compositeReady(contextName);
    		if (D) Log.d(LOG_TAG, "end composite ready ");
		}
	
    	if(elementName.equals(CONTEXT_DEFINITION))
    		Log.d("ContextEngine", "End of Contexts");
    		
	}
	
	public void addElementValue(String elementValue){
		
		
		if(currentElement.equals(CONTEXT_VALUE)){
			if (D) Log.d(LOG_TAG, "value CONTEXT_VALUE " + elementValue);
			contextEngine.newContextValue(contextName, elementValue);
		}
		
		if(currentElement.equals(CONTEXT_CHILD)){
			if (D) Log.d(LOG_TAG, "value CONTEXT_CHILD " + elementValue);
    		contextEngine.addToCompositeM(elementValue, contextName);
		}
		
    	if(currentElement.equals(CONTEXT_RANGE_NAME))
    		range.name = elementValue;
    	if(currentElement.equals(RANGE_MAX_VALUE))
    		range.max = elementValue;
    	if(currentElement.equals(RANGE_MIN_VALUE))
    		range.min = elementValue;
    	if(currentElement.equals(CONTEXT_RANGE))
    		contextEngine.newRange(contextName, range.min, range.max, range.name);
    	if(currentElement.equals(CONTEXT_TYPE))
    		contextEngine.newRange(contextName, range.min, range.max, range.name);
    	
    	if(currentElement.equals(CONTEXT_CHILD_VALUE)){
    		ruleCondition.add(elementValue);
    	}
    		
    	if(currentElement.equals(CONTEXT_PARENT_VALUE))
    		ruleParentValue = elementValue;
    	
    	
    	if(currentElement.equals(SPECIFIC_CVALUE))
    		contextEngine.newSpecificContextValue(contextName, scv.name, scv.value1, scv.value2);
    	if(currentElement.equals(SPECIFIC_CNAME))
    		scv.name = elementValue;
    	if(currentElement.equals(NUMERIC_VALUE1))
    		scv.value1 = elementValue;
    	if(currentElement.equals(NUMERIC_VALUE2))
    		scv.value2 = elementValue;		
	}
	
	static class XMLRange{
    	String name;
    	String min;
    	String max;
    }
    
    static class XMLSCV{
    	String name;
    	String value1;
    	String value2;
    }
}