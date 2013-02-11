package jpm4j.webconsole.impl;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.felix.webconsole.*;
import org.osgi.framework.*;
import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.lib.collections.*;
import aQute.lib.converter.*;
import aQute.lib.io.*;
import aQute.lib.json.*;
import aQute.library.remote.*;
import aQute.service.library.Library.Find;
import aQute.service.library.Library.Program;

/**
 * This Apache Felix Web Console Plugin uses JPM to get other plugins. It gets a
 * list of plugins from JPM. This list can then be shown with a checkbox. If
 * marked, the bundle is installed.
 */
@Component(provide = {
	Servlet.class
}, properties = {
	"felix.webconsole.label=" + WebconsoleRemotePlugins.PLUGIN_NAME
}, designate = WebconsoleRemotePlugins.Config.class, configurationPolicy = ConfigurationPolicy.optional)
public class WebconsoleRemotePlugins extends AbstractWebConsolePlugin {

	private static final String	APACHE_FELIX_WEBCONSOLE	= "apache.felix.webconsole";
	private static final String	X_JPM_PLUGIN			= "x-jpm-plugin";
	static final long			serialVersionUID		= 1L;
	static final String			PLUGIN_NAME				= "plugins";
	static final JSONCodec		codec					= new JSONCodec();

	LogService					log;
	BundleContext				context;
	List<BundleState>			state					= null;

	interface Config {
		String url();

		boolean debug();
	}

	Config			config;
	RemoteLibrary	library;

	@Activate
	void start(BundleContext context, Map<String,Object> config) throws Exception {
		this.context = context;
		this.config = Converter.cnv(Config.class, config);
	}

	@Override
	public String getLabel() {
		return "JPM4J";
	}

	@Override
	public String getTitle() {
		return "JPM4J";
	}

	@Override
	protected void renderContent(HttpServletRequest rq, HttpServletResponse rsp) throws ServletException, IOException {
		IO.copy(getClass().getResource("index.html").openStream(), rsp.getWriter());
	}

	/**
	 * Standard referring to statics. All resources should be in this package.
	 */
	public URL getResource(String resource) {
		if (resource.equals("/" + PLUGIN_NAME) || resource.equals("/" + PLUGIN_NAME + "/"))
			return null;

		resource = resource.replaceAll(PLUGIN_NAME + "/", "");
		if (resource.startsWith("/"))
			resource = resource.substring(1);

		URL url = getClass().getResource(resource);
		return url;
	}

	synchronized List<Program> getPrograms(String query) throws Exception {
		if (library == null) {
			String url = config.url();
			if (url != null && url.trim().length() == 0)
				url = null;

			library = new RemoteLibrary(url);
		}
		Find<Program> cursor = library.findProgram();
		if (query == null || query.isEmpty())
			cursor.capability(X_JPM_PLUGIN, X_JPM_PLUGIN, APACHE_FELIX_WEBCONSOLE);
		else
			cursor.query(query);

		cursor.where("last.hashes=osgi.identity.type=osgi.bundle");

		return new ExtList<Program>(cursor);
	}

	public void doPost(HttpServletRequest rq, HttpServletResponse rsp) {
		Response response = new Response();

		try {
			Command c = codec.dec().from(rq.getInputStream()).get(Command.class);
			try {
				switch (c.action) {
					case INSTALL : {
						Bundle b = context.installBundle(c.location);
						b.start();
						response.bundles = getBundleStates();
						break;
					}

					case UNINSTALL : {
						Bundle b = context.getBundle(c.location);
						b.uninstall();
						response.bundles = getBundleStates();
						break;
					}

					case FIRST :
						response.bundles = getBundleStates();

						// FALL THROUGH

					case REFRESH : {
						response.programs = getPrograms(c.query);
						break;
					}

					default :
						throw new RuntimeException("Unknown action " + c.action);
				}
			}
			catch (BundleException e) {
				response.error = e.getMessage();
				response.bundles = getBundleStates();
			}
			send(rq, rsp, response);
		}
		catch (Exception e) {
			log.log(LogService.LOG_ERROR, "Failed to execute command", e);
			throw new RuntimeException(e);
		}

	}

	private void send(HttpServletRequest rq, HttpServletResponse rsp, Object response) throws IOException, Exception {
		OutputStream out = rsp.getOutputStream();
		// String accept = rq.getHeader("Accept-Encoding");
		// if (accept != null && accept.contains("deflate")) {
		// out = new DeflaterOutputStream(out);
		// rsp.setHeader("Encoding", "deflate");
		// }
		rsp.setContentType("application/json;charset=UTF-8");
		codec.enc().to(out).put(response).close();
	}

	private List<BundleState> getBundleStates() {
		List<BundleState> states = new ArrayList<BundleState>();
		for (Bundle b : context.getBundles()) {
			BundleState state = new BundleState();
			state.bsn = b.getSymbolicName();
			if (b.getVersion() != null)
				state.version = b.getVersion().toString();
			state.bundleId = b.getBundleId();
			state.state = BundleState.State.toString(b.getState());
			states.add(state);
		}
		return states;
	}

	@Reference
	void setLog(LogService log) {
		this.log = log;
	}

}