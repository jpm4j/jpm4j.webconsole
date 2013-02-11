package jpm4j.webconsole.impl;

import java.util.*;

import aQute.service.library.Library.Program;

public class Response {
	public String				error;
	public String				repo;
	public List<BundleState>	bundles;
	public List<Program>		programs;
}
