package gateway;

import globals.Resources;
import globals.Vector2D;

import java.rmi.registry.LocateRegistry;


public class RSUApp
{

    public static void main(String[] args) {

        System.out.println("\n");

        // rsu's positions
        Vector2D pos_1 = new Vector2D(Resources.MAX_RSU_RANGE/2, 0);
        Vector2D pos_2 = new Vector2D(0, 0);
        Vector2D pos_3 = new Vector2D(Resources.MAX_RSU_RANGE, 0); // ja esta out-of-range de pos1

        // Constroi RSU's
        RSU rsu_1 = new RSU(Resources.RSU_NAME_1, pos_1);
        RSU rsu_2 = new RSU(Resources.RSU_NAME_2, pos_2);
        RSU rsu_3 = new RSU(Resources.RSU_NAME_3, pos_3);

        // Create registry if it doesn't exist
        try {
            LocateRegistry.createRegistry(Resources.REGISTRY_PORT);
        } catch(Exception e) {
            // registry is already created
        }

        try {
           
            RemoteRSUService rsu1_service = rsu_1.getRemoteRSUService();
            RemoteRSUService rsu2_service = rsu_2.getRemoteRSUService();
            RemoteRSUService rsu3_service = rsu_3.getRemoteRSUService();

          
            rsu_1.addRSU(rsu_2.getPosition(), rsu2_service);

           
            rsu_2.addRSU(rsu_1.getPosition(), rsu1_service);
            rsu_2.addRSU(rsu_3.getPosition(), rsu3_service);

         
            rsu_3.addRSU(rsu_2.getPosition(), rsu2_service);

            
            rsu1_service.publish();
            rsu2_service.publish();
            rsu3_service.publish();

            System.out.println(Resources.OK_MSG("Started: " + rsu_1));
            System.out.println(Resources.OK_MSG("Started: " + rsu_2));
            System.out.println(Resources.OK_MSG("Started: " + rsu_3));

            try {
                System.out.println("Press enter to kill the road side units...");
                System.in.read();
            } catch (java.io.IOException e) {
                System.out.println(Resources.ERROR_MSG("Unable to read from input. Exiting."));
            } finally {
                
                rsu1_service.unpublish();
                rsu2_service.unpublish();
                rsu3_service.unpublish();
            }

        } catch (Exception e) {
            System.err.println(Resources.ERROR_MSG("CA or VANET remote interfaces are not present in the RMI registry."));
        }

        System.out.println("\n");
        System.exit(0);

    }

}
