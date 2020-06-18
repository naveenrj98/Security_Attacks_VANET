package security;

import globals.Resources;
import java.rmi.registry.LocateRegistry;

public class CA {
    public static void main( String[] args ) {
        System.out.println("\n");

        // Create registry if it doesn't exist
        try { LocateRegistry.createRegistry(Resources.REGISTRY_PORT); }
        catch(Exception e) { } // registry is already created

        RemoteCAService CA = null;
        try {
            // FIXME... this certificate has a different format...
            CA = new RemoteCAService(null/*(X509Certificate)Resources.readCertificateFile(Resources.CA_CERT)*/);
        } catch (Exception e) {
            System.out.println(Resources.ERROR_MSG(e.getMessage()));
            System.exit(1);
        }
        CA.publish();

        try {
            System.out.println("Press enter to kill the server...");
            System.in.read();
        } catch (java.io.IOException e) {
            System.out.println(Resources.ERROR_MSG("Unable to read from input. Exiting."));
        } finally {
            CA.unpublish();
            System.out.println("\n");
        }
    }
}
