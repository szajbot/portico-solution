package hla13.solution.klienci;


import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import hla13.solution.PetrolType;
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
    public static final int ITERATIONS = 100;
    public static final String READY_TO_RUN = "ReadyToRun";
    private RTIambassador rtiamb;
    private ClientsAmbassador fedamb;


    public static final ArrayList<Client> clients = new ArrayList<>();
    private double timeStep = 10.0;

    public void runFederate() throws RTIexception, InterruptedException {

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
        log("Joined Federation as " + FEDERATE_NAME);

        rtiamb.registerFederationSynchronizationPoint(READY_TO_RUN, null);
        //rtiamb.registerFederationSynchronizationPoint(READY_TO_END, null);

        while (!fedamb.isAnnounced) {
            rtiamb.tick();
        }

        waitForUser();

        rtiamb.synchronizationPointAchieved(READY_TO_RUN);
        log("Achieved sync point: " + READY_TO_RUN + ", waiting for federation...");
        while (!fedamb.isReadyToRun) {
            rtiamb.tick();
        }

        enableTimePolicy();
        log("Time Policy Enabled");

        publishClient();
        log("Published Client");

        subscribeInteractions();
        log("Subscribe Gas Station");


//        while (fedamb.running) {
//
//            advanceTime(randomTime());
//            Thread.sleep(1000);
//            int clientHandle = registerClient();
//            clients.add(createClient(clientHandle, fedamb.federateTime + fedamb.federateLookahead));
//            rtiamb.tick();
//
//        }
 //       TODO this is for test use case
        sendInteractionIterations(ITERATIONS);
        for (int i = 0; i < ITERATIONS; i++) {
            advanceTime(randomTime());
            //Thread.sleep(1000);
            int clientHandle = registerClient();
            clients.add(createClient(clientHandle, fedamb.federateTime + fedamb.federateLookahead));
            rtiamb.tick();
        }

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeToAdvance);

            if (fedamb.grantedTime == timeToAdvance) {
                timeToAdvance += fedamb.federateLookahead;
                fedamb.federateTime = timeToAdvance;
            }
            rtiamb.tick();
        }

        for (Client client : clients){
            log(client.explainYourself());
            log(client.printAttributes());
        }

        for (Client client : clients) {
            deleteObject(client.getId());
            log("Deleted Object, handle=" + client.getId());
        }


        rtiamb.resignFederationExecution(ResignAction.NO_ACTION);
        log("Resigned from Federation");

        try {
            rtiamb.destroyFederationExecution(FEDERATE_NAME);
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

    private Client createClient(int clientHandle, double timeStep) {

        Random rd = new Random();
        PetrolType[] values = PetrolType.values();
        PetrolType petrolType = values[rd.nextInt(2)];
        float fuelQuantity = rd.nextFloat() + rd.nextInt(95) + 5;
        boolean washOption = rd.nextBoolean();

        try {
            Client client = new Client(clientHandle, petrolType, fuelQuantity, washOption);

            SuppliedAttributes attributes =
                    RtiFactoryFactory.getRtiFactory().createSuppliedAttributes();

            byte[] id = EncodingHelpers.encodeInt(clientHandle);
            byte[] fuel = EncodingHelpers.encodeString(String.valueOf(petrolType));
            byte[] quantity = EncodingHelpers.encodeFloat(fuelQuantity);
            byte[] wash = EncodingHelpers.encodeBoolean(washOption);

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
            LogicalTime time = convertTime(timeStep);
            rtiamb.updateAttributeValues(clientHandle, attributes, "tag".getBytes(), time);
            log("Registered Client, handle=" + clientHandle + ", petrolT=" + petrolType + ", fuelQ=" + fuelQuantity + ", washO=" + washOption);
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


    private void publishClient() throws RTIexception {

        int clientHandle = rtiamb.getObjectClassHandle("ObjectRoot.Client");

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

        int interactionIterations = rtiamb.getInteractionClassHandle("InteractionRoot.Iterations");
        rtiamb.publishInteractionClass(interactionIterations);
    }

    private void subscribeInteractions() throws RTIexception {

        int petrolService = rtiamb.getInteractionClassHandle("InteractionRoot.PetrolService");
        fedamb.petrolService = petrolService;
        rtiamb.subscribeInteractionClass(petrolService);

        int washService = rtiamb.getInteractionClassHandle("InteractionRoot.WashService");
        fedamb.washService = washService;
        rtiamb.subscribeInteractionClass(washService);

        int startPayment = rtiamb.getInteractionClassHandle("InteractionRoot.StartPayment");
        fedamb.startPayment = startPayment;
        rtiamb.subscribeInteractionClass(startPayment);

        int endPayment = rtiamb.getInteractionClassHandle("InteractionRoot.EndPayment");
        fedamb.endPayment = endPayment;
        rtiamb.subscribeInteractionClass(endPayment);

        int stopSim = rtiamb.getInteractionClassHandle("InteractionRoot.StopSim");
        fedamb.stopSim = stopSim;
        rtiamb.subscribeInteractionClass(stopSim);

    }
    private void sendInteractionIterations(int iter)throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        byte[] i = EncodingHelpers.encodeInt(iter);


        int classHandle = rtiamb.getInteractionClassHandle("InteractionRoot.Iterations");
        int iHandle = rtiamb.getParameterHandle("iter", classHandle);

        parameters.add(iHandle, i);

        rtiamb.sendInteraction(classHandle, parameters, generateTag());
    }

    private void advanceTime(double timestep) throws RTIexception {
        log("Federate time: " + fedamb.federateTime);
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
        return r.nextInt(50) + r.nextDouble();
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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
