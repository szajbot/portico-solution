package hla13.solution.stacjaBenzynowa;


import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import hla13.solution.BaseClient;
import hla13.solution.PetrolType;
import hla13.solution.stacjaBenzynowa.ObjectsOnStation.Distributor;
import hla13.solution.stacjaBenzynowa.ObjectsOnStation.Wash;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;

public class GasStationFederate {

    private static final String FEDERATION_NAME = "GasStationSimulateFederation";
    private static final String FEDERATE_NAME = "GasStationFederate";
    public static final String READY_TO_RUN = "ReadyToRun";

    private RTIambassador rtiamb;
    private GasStationAmbassador fedamb;
    private final double timeStep = 10.0;

    private final ArrayList<Distributor> onDistributors = new ArrayList<>();
    private final ArrayList<Distributor> benzineDistributors = new ArrayList<>();
    private final ArrayList<Wash> washes = new ArrayList<>();
    private final ArrayList<BaseClient> tempForWashQueue = new ArrayList<>();


    public void runFederate() throws Exception {

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

        fedamb = new GasStationAmbassador();
        rtiamb.joinFederationExecution(FEDERATE_NAME, FEDERATION_NAME, fedamb);
        log("Joined Federation as " + FEDERATE_NAME);

        rtiamb.registerFederationSynchronizationPoint(READY_TO_RUN, null);

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

        publishAndSubscribe();

        createStationObjects();

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeToAdvance);

            if (fedamb.receivedClients.size() > 0) {
                for (BaseClient receivedClient : fedamb.receivedClients) {

                    //log(receivedClient.explainYourself());
                    addClientToStation(receivedClient);
                    checkForEndedServiceTime(timeToAdvance);

                }
                fedamb.receivedClients.clear();
            }
            if (fedamb.grantedTime == timeToAdvance) {
                timeToAdvance += fedamb.federateLookahead;
                fedamb.federateTime = timeToAdvance;
            }

            rtiamb.tick();
        }

    }

    private void checkForEndedServiceTime(double timeToAdvance) {
//        loop for objects at station to check ended service on object
        checkEndedServiceForDistributor(onDistributors, timeToAdvance);
        checkEndedServiceForDistributor(benzineDistributors, timeToAdvance);

//        some clients can in one advance end distributor service and start wash service
        updateQueueForWash(washes);

        checkEndedServiceForWash(washes, timeToAdvance);
    }

    private void updateQueueForWash(ArrayList<Wash> washes) {
        sortTempForWashQueue();
        for (BaseClient client : tempForWashQueue) {
            client.setNumberInQue(washes.get(0).getQueue().size()+1);
            washes.get(0).getQueue().add(client);
        }
        tempForWashQueue.clear();
    }

    private void sortTempForWashQueue() {
        Comparator<BaseClient> comparator = Comparator.comparing(BaseClient::getLastServiceEndTime);
        tempForWashQueue.sort(comparator);
    }

    private void checkEndedServiceForWash(ArrayList<Wash> washes, double timeToAdvance) {
        for (Wash wash : washes) {
            if (!wash.getInUse() && !wash.getQueue().isEmpty()) {
                wash.setNextServiceTime(wash.serviceTime() + wash.getQueue().get(0).getLastServiceEndTime());
                serviceClientsSendInteractionsAndUpdateQueueInWash(wash, timeToAdvance);
            } else if (wash.getInUse() && !wash.getQueue().isEmpty()) {
                serviceClientsSendInteractionsAndUpdateQueueInWash(wash, timeToAdvance);
            } else {
//                log("no queue in: " + wash.getName());
            }
        }
    }

    private void checkEndedServiceForDistributor(ArrayList<Distributor> genericDistributors, double timeToAdvance) {
        for (Distributor distributor : genericDistributors) {
            if (!distributor.getInUse() && !distributor.getQueue().isEmpty()) {
                Float quantity = distributor.getQueue().get(0).getFuelQuantity();
                distributor.setNextServiceTime(distributor.serviceTime(quantity) + distributor.getQueue().get(0).getArriveTime());
                serviceClientsSendInteractionsAndUpdateQueueInDistributor(distributor, timeToAdvance);
            } else if (distributor.getInUse() && !distributor.getQueue().isEmpty()) {
                serviceClientsSendInteractionsAndUpdateQueueInDistributor(distributor, timeToAdvance);
            } else {
//                log("no queue in: " + distributor.getName());
            }
        }
    }

    private void serviceClientsSendInteractionsAndUpdateQueueInWash(Wash wash, double timeToAdvance) {
        while (wash.getNextServiceTime() < timeToAdvance && !wash.getQueue().isEmpty()) {
            BaseClient endedServiceClient = wash.getQueue().pop();
            //start mycia
            double startWashService = endedServiceClient.getLastServiceEndTime();
            endedServiceClient.setLastServiceEndTime(wash.getNextServiceTime());
            //koniec mycia
            double endWashService = endedServiceClient.getLastServiceEndTime();
            log(wash.getName() + " service ended in client nr: : " + endedServiceClient.getId() + ", at time: " + wash.getNextServiceTime());
            //send intercations for statistic
            try {
                sendInteractionService("WashService",endedServiceClient.getId(), endedServiceClient.getNumberInQue(), startWashService, endWashService);
                sendInteractionStartPayments(endedServiceClient.getId(),endWashService);
            } catch (RTIexception e) {
                throw new RuntimeException(e);
            }
            if (!wash.getQueue().isEmpty()) {
                wash.setNextServiceTime(wash.serviceTime() + wash.getNextServiceTime());
            }
        }
        if (!wash.getQueue().isEmpty()) {
            wash.setInUse(true);
        } else {
            wash.setInUse(false);
        }
    }

    private void sendClientToWashQueue(BaseClient endedServiceClient) {
        tempForWashQueue.add(endedServiceClient);
    }

    private void serviceClientsSendInteractionsAndUpdateQueueInDistributor(Distributor distributor, double timeToAdvance) {
        while (distributor.getNextServiceTime() < timeToAdvance && !distributor.getQueue().isEmpty()) {
            BaseClient endedServiceClient = distributor.getQueue().pop();
            double startPetrolService = endedServiceClient.getArriveTime();
            log(distributor.getName() + " service ended in client nr: : " + endedServiceClient.getId() + ", at time: " + distributor.getNextServiceTime());
            endedServiceClient.setLastServiceEndTime(distributor.getNextServiceTime());
            double endPetrolService = endedServiceClient.getLastServiceEndTime();
            if (endedServiceClient.isWashOption()) {
                sendClientToWashQueue(endedServiceClient);
            }
//                  TODO send to wash or cash !!!IMPORTANT SEND TO CASH
            try {
                sendInteractionService("PetrolService",endedServiceClient.getId(), endedServiceClient.getNumberInQue(), startPetrolService, endPetrolService);
                if(!endedServiceClient.isWashOption()){
                    sendInteractionStartPayments(endedServiceClient.getId(), endPetrolService);
                }
            } catch (RTIexception e) {
                throw new RuntimeException(e);
            }
            if (!distributor.getQueue().isEmpty()) {
                Float quantity = distributor.getQueue().get(0).getFuelQuantity();
                distributor.setNextServiceTime(distributor.serviceTime(quantity) + distributor.getNextServiceTime());
            }
        }
        if (!distributor.getQueue().isEmpty()) {
            distributor.setInUse(true);
        } else {
            distributor.setInUse(false);
        }
    }

    private void addClientToStation(BaseClient receivedClient) {
        switch (receivedClient.getPetrolType()) {
            case ON:
                onDistributors.get(0).getQueue().add(receivedClient);
                receivedClient.setArriveTime(fedamb.federateTime);
                receivedClient.setNumberInQue(onDistributors.get(0).getQueue().size());
                break;
            case BENZYNA:
                benzineDistributors.get(0).getQueue().add(receivedClient);
                receivedClient.setArriveTime(fedamb.federateTime);
                receivedClient.setNumberInQue(benzineDistributors.get(0).getQueue().size());
                break;
            default:
                log("Strange thing happen");
        }
    }

    private void createStationObjects() {
        onDistributors.add(new Distributor("Distributor nr 1", new LinkedList<>(), PetrolType.ON));
        benzineDistributors.add(new Distributor("Distributor nr 2", new LinkedList<>(), PetrolType.BENZYNA));
        washes.add(new Wash("Wash nr 1", new LinkedList<>()));
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

    private void advanceTime(double timeToAdvance) throws RTIexception {
        fedamb.isAdvancing = true;
        LogicalTime newTime = convertTime(timeToAdvance);
        rtiamb.timeAdvanceRequest(newTime);

        while (fedamb.isAdvancing) {
            rtiamb.tick();
        }
    }

    private void publishAndSubscribe() throws RTIexception {

        int classHandle = rtiamb.getObjectClassHandle("ObjectRoot.Client");

        int clientIdHandle = rtiamb.getAttributeHandle("clientId", classHandle);
        int petrolTypeHandle = rtiamb.getAttributeHandle("petrolType", classHandle);
        int fuelQuantityHandle = rtiamb.getAttributeHandle("fuelQuantity", classHandle);
        int washOptionHandle = rtiamb.getAttributeHandle("washOption", classHandle);

        AttributeHandleSet attributes = RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();

        attributes.add(clientIdHandle);
        attributes.add(petrolTypeHandle);
        attributes.add(fuelQuantityHandle);
        attributes.add(washOptionHandle);

        fedamb.newClientHandle = classHandle;

        rtiamb.subscribeObjectClassAttributes(classHandle, attributes);


        int interactionPetrolService = rtiamb.getInteractionClassHandle("InteractionRoot.PetrolService");
        rtiamb.publishInteractionClass(interactionPetrolService);

        int interactionWashService = rtiamb.getInteractionClassHandle("InteractionRoot.WashService");
        rtiamb.publishInteractionClass(interactionWashService);

        int interactionStartPayment = rtiamb.getInteractionClassHandle("InteractionRoot.StartPayment");
        rtiamb.publishInteractionClass(interactionStartPayment);
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

    private byte[] generateTag() {
        return ("" + System.currentTimeMillis()).getBytes();
    }

    private void log(String message) {
        System.out.println(FEDERATE_NAME + ": " + message);
    }

    private void sendInteractionService(String name, int clientId, int que, double startTime, double endTime) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        byte[] id = EncodingHelpers.encodeInt(clientId);
        byte[] startT = EncodingHelpers.encodeDouble(startTime);
        byte[] endT = EncodingHelpers.encodeDouble(endTime);
        byte[] queue = EncodingHelpers.encodeInt(que);

        int classHandle = rtiamb.getInteractionClassHandle("InteractionRoot."+name);
        int idHandle = rtiamb.getParameterHandle("clientId", classHandle);
        int startHandle = rtiamb.getParameterHandle("startTime", classHandle);
        int endHandle = rtiamb.getParameterHandle("endTime", classHandle);
        int queHandle = rtiamb.getParameterHandle("numberInPetrolQueue", classHandle);


        parameters.add(idHandle, id);
        parameters.add(startHandle, startT);
        parameters.add(endHandle, endT);
        parameters.add(queHandle, queue);


        rtiamb.sendInteraction(classHandle, parameters, generateTag());
    }

    private void sendInteractionStartPayments(int clientId, double startTime) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        byte[] id = EncodingHelpers.encodeInt(clientId);
        byte[] startT = EncodingHelpers.encodeDouble(startTime);

        int classHandle = rtiamb.getInteractionClassHandle("InteractionRoot.StartPayment");
        int idHandle = rtiamb.getParameterHandle("clientId", classHandle);
        int startHandle = rtiamb.getParameterHandle("startTime", classHandle);

        parameters.add(idHandle, id);
        parameters.add(startHandle, startT);

        rtiamb.sendInteraction(classHandle, parameters, generateTag());
    }

    public static void main(String[] args) {
        try {
            new GasStationFederate().runFederate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
