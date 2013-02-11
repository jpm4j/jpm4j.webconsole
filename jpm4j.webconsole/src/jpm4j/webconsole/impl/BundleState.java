package jpm4j.webconsole.impl;

import org.osgi.framework.*;

public class BundleState {
	public enum State {
		INSTALLED, RESOLVED, STARTING, ACTIVE, STOPPING, UNINSTALLED, UNKNOWN;

		static public State toString(int state) {
			switch (state) {
				case Bundle.ACTIVE :
					return ACTIVE;
				case Bundle.INSTALLED :
					return INSTALLED;
				case Bundle.RESOLVED :
					return RESOLVED;
				case Bundle.STARTING :
					return STARTING;
				case Bundle.STOPPING :
					return STOPPING;
				case Bundle.UNINSTALLED :
					return UNINSTALLED;

				default :
					return UNKNOWN;
			}
		}

	}

	public long		bundleId;
	public boolean	update;
	public boolean	install;
	public String	error;
	public String	bsn;
	public String	version;
	public State	state;
}
