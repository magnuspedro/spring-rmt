package fixtures;

import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.detection.refactor.dataExtractions.ast.AbstractSyntaxTree;
import br.com.magnus.detection.refactor.dataExtractions.ast.AstHandler;
import br.com.magnus.detection.refactor.methods.zaiferisVE.ZafeirisEtAl2016Candidate;
import com.github.javaparser.ast.CompilationUnit;

import java.util.List;

public class Zafeiris {

    private static final AbstractSyntaxTree abstractSyntaxTree = new AbstractSyntaxTree();

    public static final String CHILD = """
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

    public static final String PARENT = """
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
            }
""";

    public final static String REFACTORED_CHILD = """
package jade.imtp.leap.JICP;

public class JICPSPeer extends JICPPeer {

    protected static Logger myLogger = Logger.getMyLogger(JICPSPeer.class.getName());

    private SSLContext ctx = null;

    protected void beforeActivate(ICP.Listener l, String peerID, Profile p) throws ICPException {
        if (myLogger.isLoggable(Logger.FINE)) {
            myLogger.log(Logger.FINE, "About to activate JICP peer.");
        }
        // create context at activation time
        ctx = SSLHelper.createContext();
        setUseSSLAuth(SSLHelper.needAuth());
        if (myLogger.isLoggable(Logger.FINE)) {
            myLogger.log(Logger.FINE, "activate() context created ctx=" + ctx);
        }
    }

    protected TransportAddress afterActivate(ICP.Listener l, String peerID, Profile p, TransportAddress ta) throws ICPException {
        if (myLogger.isLoggable(Logger.INFO)) {
            myLogger.log(Logger.INFO, "JICP Secure Peer activated. (auth=" + getUseSSLAuth() + ", ta=" + ta + ")");
        }
        return ta;
    }
}
// end class
""";

    public final static String REFACTORED_PARENT = """
package jade.imtp.leap.JICP;

public class JICPPeer {

    private static final int POOL_SIZE = 50;

    private JICPClient client;

    private JICPServer server;

    private Ticker ticker;

    private String myID;

    private int connectionTimeout = 0;

    public static final String CONNECTION_TIMEOUT = "jade_imtp_leap_JICP_JICPPeer_connectiontimeout";

    /**
     * Start listening for internal platform messages on the specified port
     */
    public final TransportAddress activate(ICP.Listener l, String peerID, Profile p) throws ICPException {
        beforeActivate(l, peerID, p);
        TransportAddress superReturnVar = doActivate(l, peerID, p);
        return afterActivate(l, peerID, p, superReturnVar);
    }

    private TransportAddress doActivate(ICP.Listener l, String peerID, Profile p) throws ICPException {
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

    protected void beforeActivate(ICP.Listener l, String peerID, Profile p) throws ICPException {
    }

    protected TransportAddress afterActivate(ICP.Listener l, String peerID, Profile p, TransportAddress ta) throws ICPException {
        return ta;
    }
}
""";

    private static CompilationUnit getParentCu() {
        return (CompilationUnit) AbstractSyntaxTree.parseSingle(PARENT);
    }

    private static CompilationUnit getChildCu() {
        return (CompilationUnit) AbstractSyntaxTree.parseSingle(CHILD);
    }

    public static List<JavaFile> createJavaFiles() {
        var javaFile1 = JavaFile.builder()
                .name("JICPPeer")
                .originalClass(PARENT)
                .parsed(getParentCu())
                .build();
        var javaFile2 = JavaFile.builder()
                .name("JICPSPeer")
                .originalClass(CHILD)
                .parsed(getChildCu())
                .build();

        return List.of(javaFile1, javaFile2);
    }

    public static ZafeirisEtAl2016Candidate getCandidate() {
        var cu = (CompilationUnit) AbstractSyntaxTree.parseSingle(CHILD);
        var clazz = AstHandler.getClassOrInterfaceDeclaration(cu).orElse(null);
        var overriddenMethod = AstHandler.getMethods(cu).getFirst();
        var overridingMethod = AstHandler.getMethods((CompilationUnit) AbstractSyntaxTree.parseSingle(PARENT)).getFirst();
        var superCall = AstHandler.getSuperCalls(overriddenMethod).getFirst();
        var file = JavaFile.builder()
                .name("JICPSPeer")
                .originalClass(CHILD)
                .parsed(cu)
                .build();

        return ZafeirisEtAl2016Candidate.builder()
                .file(file)
                .compilationUnit(file.getCompilationUnit())
                .classDcl(clazz)
                .overriddenMethod(overriddenMethod)
                .overridingMethod(overridingMethod)
                .superCall(superCall)
                .build();
    }
}
