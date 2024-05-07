package fixtures;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.AbstractSyntaxTree;

import java.util.List;

public class Zafeiris {

    private static final AbstractSyntaxTree abstractSyntaxTree = new AbstractSyntaxTree();

    private static final String child = """
            package jade.imtp.leap.JICP;
            
            public class JICPSPeer extends JICPPeer {
            	protected static Logger myLogger = Logger.getMyLogger( JICPSPeer.class.getName() );
            	private SSLContext ctx = null;

                @Override
            	public TransportAddress activate(ICP.Listener l, String peerID, Profile p) throws ICPException {
            		if (myLogger.isLoggable(Logger.FINE)) {
            			myLogger.log(Logger.FINE, "About to activate JICP peer." );
            		}
            		ctx = SSLHelper.createContext(); // create context at activation time
                            setUseSSLAuth(SSLHelper.needAuth());
            		if (myLogger.isLoggable(Logger.FINE)) {
            			myLogger.log(Logger.FINE, "activate() context created ctx="+ctx );
            		}
            		TransportAddress ta = super.activate(l, peerID, p);
            		if (myLogger.isLoggable(Logger.INFO)) {
            			myLogger.log(Logger.INFO, "JICP Secure Peer activated. (auth="+getUseSSLAuth()+", ta="+ta+")");
            		}
            		return ta;
            	}

            } // end class""";

    private static final String parent = """
            package jade.imtp.leap.JICP;
            
            public class JICPPeer {
            	private static final int POOL_SIZE = 50;

            	private JICPClient   client;
            	private JICPServer   server;
            	private Ticker       ticker;

            	private String myID;

            	private int connectionTimeout = 0;

            	public static final String CONNECTION_TIMEOUT = "jade_imtp_leap_JICP_JICPPeer_connectiontimeout";
            	/**
            	 * Start listening for internal platform messages on the specified port
            	 */
            	public TransportAddress activate(ICP.Listener l, String peerID, Profile p) throws ICPException {
            		myID = peerID;

            		connectionTimeout = Integer.parseInt(p.getParameter(CONNECTION_TIMEOUT, "0"));

            		// Start the client
            		client = new JICPClient(getProtocol(), getConnectionFactory(), POOL_SIZE);

            		// Start the server listening for connections
            		server = new JICPServer(p, this, l, getConnectionFactory(), POOL_SIZE);
            		server.start();

            		// Start the Ticker
            		ticker = new Ticker(60000);
            		ticker.start();

            		// Creates the local transport address
            		TransportAddress localTA = getProtocol().buildAddress(server.getLocalHost(), String.valueOf(server.getLocalPort()), null, null);

            		return localTA;
            	}
            }""";

    public static List<JavaFile> createJavaFiles() {
        var javaFile1 = JavaFile.builder()
                .name("JICPPeer")
                .originalClass(parent)
                .build();
        var javaFile2 = JavaFile.builder()
                .name("JICPSPeer")
                .originalClass(child)
                .build();
        var javaFiles = List.of(javaFile1, javaFile2);

        abstractSyntaxTree.parseAll(javaFiles);
        return javaFiles;
    }
}
