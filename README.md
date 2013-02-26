jpm4j.webconsole
================

This is a demo project to show how JPM plugins can work. This project uses the following dependency:

    aQute.library.client;version=1.0.1

An OSGi capability is used to identify a JAR as a plugin for a give system. The capability 
in this case is:

    ns: x-jpm-plugin
	    x-jpm-plugin=NAME

In this case, an Apache Felix WebConsole plugin, the name chosen is 

    apache.felix.webconsole

So to mark a bundle as a Web Console plugin, the following header should be added to an OSGi
bundle's manifest:

    Provide-Capability: \
        x-jpm-plugin;x-jpm-plugin=apache.felix.webconsole

JPM will parse this header and make it searchable. So the application's side (in this case the
JPM4J WebConsole) we need to search for any bundles with this capability. The RemoteLibrary class
makes this almost trivial.

    RemoteLibrary lib = new RemoteLibrary(null); // default url
    for ( Program program : library.findProgram()
    	.capability(X_JPM_PLUGIN, X_JPM_PLUGIN, APACHE_FELIX_WEBCONSOLE) // our plugins
	) {
		// display
	}

A Program object is a data object and it contains lots of information about the Program. Its
bsn, name, description (in wiki.text), etc. The actual bundles are referenced in the revisions
field. A special revision is the last revision in the last field. Let Eclipse be your friend.

If a Program is selected, it needs to be installed. Though the revisions field contains information
about all revisions, in general you want to install the last version. You can do this as follows:

    context.installBundle( program.last.url.toString() );
    
If you want to find out if a bundle is already installed you can ask for the bundle with
`getBundle`:

    if ( context.getBundle( program.last.url.toString()) != null )
       // already installed
		
The interesting method is `WebconsoleRemotePlugins.getPrograms()`

 
    