package hla13.solution.kasy;


import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Comparator;

public class CashFederate {

    private static final String FEDERATION_NAME = "GasStationSimulateFederation";
    private static final String FEDERATE_NAME = "CashFederate";
    public static final String READY_TO_RUN = "ReadyToRun";
    public static int ITERATIONS = 100;

    private RTIambassador rtiamb;
    private CashAmbassador fedamb;
    private final double timeStep = 10.0;
    private double counter = 0;
    private final Cash cash = new Cash();


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

        fedamb = new CashAmbassador();
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


        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeToAdvance);

            if (fedamb.receivedClients.size() > 0) {
                for (Client receivedClient : fedamb.receivedClients) {

                    //log(receivedClient.explainYourself());
                    addClientToQueue(receivedClient);
                    checkEndedServiceForPayment(timeToAdvance);
                }
                fedamb.receivedClients.clear();

            }
            if (!cash.getQueue().isEmpty()){
                checkEndedServiceForPayment(timeToAdvance);

            }
            if (fedamb.grantedTime == timeToAdvance) {
                timeToAdvance += fedamb.federateLookahead;
                fedamb.federateTime = timeToAdvance;

            }
            if (counter == ITERATIONS){
                sendInteractionStopSim();
                fedamb.running = false;
            }

            rtiamb.tick();
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
    private void checkEndedServiceForPayment(double timeToAdvance) {
        if (!cash.getInUse() && !cash.getQueue().isEmpty()) {
            cash.setNextPaymentTime(cash.serviceTime() + cash.getQueue().get(0).getArriveTime());
            serviceClientsSendInteractionsAndUpdateQueueInCash(cash, timeToAdvance);
        } else if (cash.getInUse() && !cash.getQueue().isEmpty()) {
            serviceClientsSendInteractionsAndUpdateQueueInCash(cash, timeToAdvance);
        } else {
                log("ERROR in payment queue");
        }
    }

    private void serviceClientsSendInteractionsAndUpdateQueueInCash(Cash cash, double timeToAdvance) {
        while (cash.getNextPaymentTime() < timeToAdvance && !cash.getQueue().isEmpty()) {
            Client endedPaymentClient = cash.getQueue().pop();
            log("Payment service ended in client nr: : " + endedPaymentClient.getId() + ", at time: " + cash.getNextPaymentTime());
            endedPaymentClient.setPaymentTime(cash.getNextPaymentTime());
            double endPetrolService = endedPaymentClient.getPaymentTime();

            try {
                sendInteractionEndPayment(endedPaymentClient.getId(), endPetrolService, endedPaymentClient.getNumberInQue());
                counter++;

            } catch (RTIexception e) {
                throw new RuntimeException(e);
            }
            if (!cash.getQueue().isEmpty()) {
                cash.setNextPaymentTime(cash.serviceTime() + cash.getNextPaymentTime());
            }
        }
        if (!cash.getQueue().isEmpty()) {
            cash.setInUse(true);
        } else {
            cash.setInUse(false);
        }
    }

    public void addClientToQueue(Client receivedClient) {
        if(cash.getQueue().isEmpty()){
            cash.addToQueue(receivedClient);
        }else{
            cash.addToQueue(receivedClient);
            cash.getQueue().sort(Comparator.comparingDouble(Client::getArriveTime));
            receivedClient.setNumberInQue(cash.getQueue().indexOf(receivedClient)+1);
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

    private void advanceTime(double timeToAdvance) throws RTIexception {
        fedamb.isAdvancing = true;
        LogicalTime newTime = convertTime(timeToAdvance);
        rtiamb.timeAdvanceRequest(newTime);

        while (fedamb.isAdvancing) {
            rtiamb.tick();
        }
    }

    private void publishAndSubscribe() throws RTIexception {

        int startPayment = rtiamb.getInteractionClassHandle("InteractionRoot.StartPayment");
        fedamb.startPayment = startPayment;
        rtiamb.subscribeInteractionClass(startPayment);

        int iters = rtiamb.getInteractionClassHandle("InteractionRoot.Iterations");
        fedamb.iters = iters;
        rtiamb.subscribeInteractionClass(iters);

        int endPayment = rtiamb.getInteractionClassHandle("InteractionRoot.EndPayment");
        rtiamb.publishInteractionClass(endPayment);

        int stopSim = rtiamb.getInteractionClassHandle("InteractionRoot.StopSim");
        rtiamb.publishInteractionClass(stopSim);
    }

    private void enableTimePolicy() throws RTIexception {
        LogicalTime currentTime = convertTime(fedamb.federateTime);
        LogicalTimeInterval lookahead = convertInterval(fedamb.federateLookahead);

        this.rtiamb.enableTimeRegulation(currentTime, lookahead);

        while (!fedamb.isRegulating) {
            rtiamb.tick();
        }

        this.rtiamb.enableTimeConstrained();

        while (!fedamb.isConstrained) {
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

    private void sendInteractionEndPayment(int clientId, double endTime, int que) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        byte[] id = EncodingHelpers.encodeInt(clientId);
        byte[] endT = EncodingHelpers.encodeDouble(endTime);
        byte[] queue = EncodingHelpers.encodeInt(que);

        int classHandle = rtiamb.getInteractionClassHandle("InteractionRoot.EndPayment");
        int idHandle = rtiamb.getParameterHandle("clientId", classHandle);
        int endHandle = rtiamb.getParameterHandle("endTime", classHandle);
        int queHandle = rtiamb.getParameterHandle("numberInPetrolQueue", classHandle);

        parameters.add(idHandle, id);
        parameters.add(endHandle, endT);
        parameters.add(queHandle, queue);

        rtiamb.sendInteraction(classHandle, parameters, generateTag());
    }
    private void sendInteractionStopSim() throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        int classHandle = rtiamb.getInteractionClassHandle("InteractionRoot.StopSim");

        rtiamb.sendInteraction(classHandle, parameters, generateTag());
    }


    public static void main(String[] args) {
        try {
            new CashFederate().runFederate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
