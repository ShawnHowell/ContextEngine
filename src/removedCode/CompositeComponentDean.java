/*
 * Copyright (C) 2011 The Context Engine Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package removedCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import uk.ac.tvu.mdse.contextengine.Component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CompositeComponentDean extends Component implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -493221379660180723L;
	public static final String LOG_TAG = "Composite_Component";
	public static final boolean D = true;
	
	// Attributes
	public Hashtable<String, Boolean> positivecontexts;
	public Hashtable<String, Boolean> negativecontexts;
	public Hashtable<String, Boolean> eithercontexts;
	public Boolean eithercontextvalue;

	public CompositeComponentDean(String name, Context c) {
		super(name, c);
		positivecontexts = new Hashtable<String, Boolean>();
		negativecontexts = new Hashtable<String, Boolean>();
		eithercontexts = new Hashtable<String, Boolean>();
		eithercontextvalue = false;
		setupMonitor();

	}

	public CompositeComponentDean(String name, Context c, ArrayList<String> pc,
			ArrayList<String> nc) {
		super(name, c);
		positivecontexts = new Hashtable<String, Boolean>();
		setupMonitor();
		for (String cn : pc)
			registerComponent(cn, false);
		for (String cn : nc)
			registerComponent(cn, true);
	}

	private void setupMonitor() {
		// TODO Auto-generated method stub
		contextMonitor = new BroadcastReceiver() {

			@Override
			public void onReceive(Context c, Intent in) {
				// TODO Auto-generated method stub
				String context = in.getExtras().getString(CONTEXT_NAME);
				boolean value = in.getExtras().getBoolean(CONTEXT_VALUE);
				if (positivecontexts.containsKey(context)) {
					positivecontexts.put(context, value);
					checkContext();
				}

				if (negativecontexts.containsKey(context)) {
					negativecontexts.put(context, value);
					checkContext();
				}

				if (eithercontexts.containsKey(context)) {
					eithercontexts.put(context, value);
					checkContext();
				}
			}
		};
		context.registerReceiver(contextMonitor, filter);

	}

	public boolean registerComponent(String c, Boolean v) {
		if (v) {
			if (!positivecontexts.containsKey(c)) {
				positivecontexts.put(c, !v);
				return true;
			} else
				return false;
		} else {
			if (!negativecontexts.containsKey(c)) {
				negativecontexts.put(c, !v);
				return true;
			} else
				return false;
		}
	}

	public boolean registerComponent(ArrayList<String> contextList,
			Boolean value) {

		if (contextList.size() > 0) {
			eithercontextvalue = value;
			for (String c : contextList) {
				eithercontexts.put(c, !value);
			}
			return true;
		}
		return false;
	}

	public boolean unregisterComponent(String c) {

		if (positivecontexts.containsKey(c)) {
			positivecontexts.remove(c);
			return true;
		} else if (negativecontexts.containsKey(c)) {
			negativecontexts.remove(c);
			return true;
		} else if (eithercontexts.containsKey(c)) {
			eithercontexts.remove(c);
			return true;
		} else
			return false;
	}

	public boolean isComposite() {
		if (positivecontexts.size() > 1)
			return true;
		else
			return false;
	}

	public void checkContext() {
		if (checkPositives() && checkNegatives() && checkEithers()) {
			if (!contextValue) {
				sendNotification(true);
				contextValue = true;
			}
		} else if ((!checkPositives()) || (!checkNegatives())
				|| (!checkEithers())) {
			if (contextValue) {
				sendNotification(false);
				contextValue = false;
			}
		}
	}

	/*
	 * Because not all hashtables may contain a context, its important we don't
	 * get null exceptions, so we check if they have anything. If they don't,
	 * then just say its true so it gets ignored in the context checking, though
	 * it does have a context, check it and return if it suits or not.
	 */
	private Boolean checkPositives() {
		if (positivecontexts.size() > 0)
			return !positivecontexts.containsValue(false);
		else
			return true;
	}

	private Boolean checkNegatives() {
		if (negativecontexts.size() > 0)
			return !negativecontexts.containsValue(true);
		else
			return true;
	}

	private Boolean checkEithers() {
		if (eithercontexts.size() > 0)
			return eithercontexts.containsValue(eithercontextvalue);
		else
			return true;
	}

}