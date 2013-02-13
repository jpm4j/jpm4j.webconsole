package jpm4j.webconsole.impl;

public class Command {
	enum Action {
		INSTALL, UNINSTALL, REFRESH, FIRST, UPDATE;
	}

	public Action	action;
	public String	location;	// for INSTALL/UNINSTALL
	public String	query;		// for REFRESH
	public long	bundleId;		// for UPDATE
}
