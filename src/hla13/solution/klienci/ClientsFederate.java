package hla13.solution.klienci;


import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Random;

public class ClientsFederate {

    private static final String FEDERATION_NAME = "GasStationSimulateFederation";
    private static final String FEDERATE_NAME = "ClientsFederate";
    public static final int ITERATIONS = 20;
    public static final String READY_TO_RUN = "ReadyToRun";
    private RTIambassador rtiamb;
    private ClientsAmbassador fedamb;
    private final double timeStep = 10.0;


    private ArrayList<Client> clients = new ArrayList<>();
    private int clientId = 0;
    private int clientHlaHandle;

    public void runFederate() throws RTIexception {

        rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();

        try {
            File fom = new File("solution.fed");
            rtiamb.createFederationExecution(FEDERATION_NAME, fom.toURI().toURL());
            log("Created Federation");
        } catch (FederationExecutionAlreadyExists exists) {
            log("Didn't create federation, it already existed");
        } catch (MalformedURLException urle) {
            log("Exception processing fom: " + urle.getMessage());
            urle.printStackTrace();
            return;
        }

        fedamb = new ClientsAmbassador();
        rtiamb.joinFederationExecution(FEDERATE_NAME, FEDERATION_NAME, fedamb);
        log("Joined Federation as" + FEDERATE_NAME);

        rtiamb.registerFederationSynchronizationPoint(READY_TO_RUN, null);

        while (fedamb.isAnnounced == false) {
            rtiamb.tick();
        }

        waitForUser();

        rtiamb.synchronizationPointAchieved(READY_TO_RUN);
        log("Achieved sync point: " + READY_TO_RUN + ", waiting for federation...");
        while (fedamb.isReadyToRun == false) {
            rtiamb.tick();
        }

        enableTimePolicy();
        log("Time Policy Enabled");

        publishClient();
        log("Published and Subscribed");


//        while (fedamb.running) {
//            advanceTime(randomTime());
//            sendInteraction(fedamb.federateTime + fedamb.federateLookahead);
//            rtiamb.tick();
//        }
//        TODO this is for test use case
        for (int i = 0; i < ITERATIONS; i++) {
            // register new clients
            int clentHandle = registerClient();
            clients.add(createClient(clentHandle));


            // 9.3 request a time advance and wait until we get it
            advanceTime(1.0);
            log("Time Advanced to " + fedamb.federateTime);
        }

        for (Client client : clients) {
            deleteObject(client.id);
            log("Deleted Object, handle=" + client.id);
        }


        rtiamb.resignFederationExecution(ResignAction.NO_ACTION);
        log("Resigned from Federation");

        try {
            rtiamb.destroyFederationExecution("ExampleFederation");
            log("Destroyed Federation");
        } catch (FederationExecutionDoesNotExist dne) {
            log("No need to destroy federation, it doesn't exist");
        } catch (FederatesCurrentlyJoined fcj) {
            log("Didn't destroy federation, federates still joined");
        }

    }

    private void waitForUser() {
        log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (Exception e) {
            log("Error while waiting for user input: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void enableTimePolicy() throws RTIexception {
        LogicalTime currentTime = convertTime(fedamb.federateTime);
        LogicalTimeInterval lookahead = convertInterval(fedamb.federateLookahead);

        this.rtiamb.enableTimeRegulation(currentTime, lookahead);

        while (fedamb.isRegulating == false) {
            rtiamb.tick();
        }

        this.rtiamb.enableTimeConstrained();

        while (fedamb.isConstrained == false) {
            rtiamb.tick();
        }
    }

    private int registerClient() throws RTIexception {

        int clientHandle = rtiamb.getObjectClassHandle("ObjectRoot.Client");
        return rtiamb.registerObjectInstance(clientHandle);
    }

    private Client createClient(int clientHandle) {

        Random rd = new Random();
        Client.PetrolType[] values = Client.PetrolType.values();
        Client.PetrolType petrolType = values[rd.nextInt(2)];
        float fuelQuantity = rd.nextFloat() + rd.nextInt(95) + 5;
        boolean washOption = rd.nextBoolean();

        try {
            Client client = new Client(clientHandle, petrolType, fuelQuantity, washOption);

            SuppliedAttributes attributes =
                    RtiFactoryFactory.getRtiFactory().createSuppliedAttributes();

            byte[] id = EncodingHelpers.encodeString("clientId:" + clientHandle);
            byte[] fuel = EncodingHelpers.encodeString("petrolType:" + petrolType);
            byte[] quantity = EncodingHelpers.encodeString("fuelQuantity:" + fuelQuantity);
            byte[] wash = EncodingHelpers.encodeString("washOption:" + washOption);

            int classClient = rtiamb.getObjectClass(clientHandle);
            int idHandle = rtiamb.getAttributeHandle("clientId", classClient);
            int fuelHandle = rtiamb.getAttributeHandle("petrolType", classClient);
            int quantityHandle = rtiamb.getAttributeHandle("fuelQuantity", classClient);
            int washHandle = rtiamb.getAttributeHandle("washOption", classClient);

            // put the values into the collection
            attributes.add(idHandle, id);
            attributes.add(fuelHandle, fuel);
            attributes.add(quantityHandle, quantity);
            attributes.add(washHandle, wash);

            //////////////////////////
            // do the actual update //
            //////////////////////////
            rtiamb.updateAttributeValues(clientHandle, attributes, generateTag());
            log("Registered Client, handle=" + clientHandle);
            return client;
        } catch (Exception e) {
            log("Client not created!");
            return null;
        }
    }

    private byte[] generateTag() {
        return ("" + System.currentTimeMillis()).getBytes();
    }

    private void deleteObject(int handle) throws RTIexception {
        rtiamb.deleteObjectInstance(handle, generateTag());
    }

    private void sendInteraction(double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        Random random = new Random();
        Client.PetrolType petrolType;
        if (random.nextInt(2) == 0) {
            petrolType = Client.PetrolType.ON;
        } else {
            petrolType = Client.PetrolType.BENZYNA;
        }

        byte[] petrolTypeByte = EncodingHelpers.encodeString(petrolType.name());
        byte[] clientIdByte = EncodingHelpers.encodeInt(++clientId);

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.NewClient");
        int clientIdHandle = rtiamb.getParameterHandle("clientId", interactionHandle);
        int petrolTypeHandle = rtiamb.getParameterHandle("petrolType", interactionHandle);

        parameters.add(clientIdHandle, clientIdByte);
        parameters.add(petrolTypeHandle, petrolTypeByte);

//        klienci.add(new Klient(petrolType, clientNumber));

        LogicalTime time = convertTime(timeStep);
        rtiamb.sendInteraction(interactionHandle, parameters, "tag".getBytes(), time);
    }

    private void publishClient() throws RTIexception {

        int clientHandle = rtiamb.getObjectClassHandle("ObjectRoot.Client");
//        this.clientHlaHandle = rtiamb.registerObjectInstance(clientHandle);

        int idHandle = rtiamb.getAttributeHandle("clientId", clientHandle);
        int fuelHandle = rtiamb.getAttributeHandle("petrolType", clientHandle);
        int quantityHandle = rtiamb.getAttributeHandle("fuelQuantity", clientHandle);
        int washHandle = rtiamb.getAttributeHandle("washOption", clientHandle);

        // package the information into a handle set
        AttributeHandleSet attributes =
                RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add(idHandle);
        attributes.add(fuelHandle);
        attributes.add(quantityHandle);
        attributes.add(washHandle);

        // do the actual publication
        rtiamb.publishObjectClass(clientHandle, attributes);
    }

    private void advanceTime(double timestep) throws RTIexception {
        log("requesting time advance for: " + timestep);
        // request the advance
        fedamb.isAdvancing = true;
        LogicalTime newTime = convertTime(fedamb.federateTime + timestep);
        rtiamb.timeAdvanceRequest(newTime);
        while (fedamb.isAdvancing) {
            rtiamb.tick();
        }
    }

    private double randomTime() {
        Random r = new Random();
        return 1 + (9 * r.nextDouble());
    }

    private LogicalTime convertTime(double time) {
        // PORTICO SPECIFIC!!
        return new DoubleTime(time);
    }

    /**
     * Same as for {@link #convertTime(double)}
     */
    private LogicalTimeInterval convertInterval(double time) {
        // PORTICO SPECIFIC!!
        return new DoubleTimeInterval(time);
    }

    private void log(String message) {
        System.out.println(FEDERATE_NAME + "   : " + message);
    }

    public static void main(String[] args) {
        try {
            new ClientsFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }

}
