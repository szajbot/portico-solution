package hla13.solution.stacjaBenzynowa;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla13.solution.PetrolType;
import hla13.solution.BaseClient;
import javafx.util.Pair;
import org.portico.impl.hla13.types.DoubleTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class GasStationAmbassador extends NullFederateAmbassador {

    //----------------------------------------------------------
    //                    STATIC VARIABLES
    //----------------------------------------------------------

    public static final String READY_TO_RUN = "ReadyToRun";

    //----------------------------------------------------------
    //                   INSTANCE VARIABLES
    //----------------------------------------------------------
    // these variables are accessible in the package
    protected double federateTime = 0.0;
    protected double grantedTime = 0.0;
    protected double federateLookahead = 1.0;

    protected boolean isRegulating = false;
    protected boolean isConstrained = false;
    protected boolean isAdvancing = false;

    protected boolean isAnnounced = false;
    protected boolean isReadyToRun = false;

    protected boolean running = true;

    protected int newClientHandle;
    protected ArrayList<Integer> newClientInstances = new ArrayList<>();

    protected ArrayList<BaseClient> receivedClients = new ArrayList<>();

    class PairComparator implements Comparator<Pair<Integer, byte[]>> {
        @Override
        public int compare(Pair<Integer, byte[]> pair1, Pair<Integer, byte[]> pair2) {
            Integer key1 = pair1.getKey();
            Integer key2 = pair2.getKey();
            return key1.compareTo(key2);
        }
    }

    private double convertTime(LogicalTime logicalTime) {
        // PORTICO SPECIFIC!!
        return ((DoubleTime) logicalTime).getTime();
    }

    private void log(String message) {
        System.out.println("GasStationAmbassador: " + message);
    }

    public void synchronizationPointRegistrationFailed(String label) {
        log("Failed to register sync point: " + label);
    }

    public void synchronizationPointRegistrationSucceeded(String label) {
        log("Successfully registered sync point: " + label);
    }

    public void announceSynchronizationPoint(String label, byte[] tag) {
        log("Synchronization point announced: " + label);
        if (label.equals(GasStationFederate.READY_TO_RUN))
            this.isAnnounced = true;
    }

    public void federationSynchronized(String label) {
        log("Federation Synchronized: " + label);
        if (label.equals(GasStationFederate.READY_TO_RUN))
            this.isReadyToRun = true;
    }

    public void discoverObjectInstance(int theObject,
                                       int theObjectClass,
                                       String objectName) {

        if (theObjectClass == newClientHandle) {
//            log("Received new object client");
            newClientInstances.add(theObject);
        } else {
            log("Sth strange happen");
        }

    }

    public void reflectAttributeValues(int theObject,
                                       ReflectedAttributes theAttributes,
                                       byte[] tag) {
        // just pass it on to the other method for printing purposes
        // passing null as the time will let the other method know it
        // it from us, not from the RTI
        reflectAttributeValues(theObject, theAttributes, tag, null, null);
    }

    public void reflectAttributeValues(int theObject,
                                       ReflectedAttributes theAttributes,
                                       byte[] tag,
                                       LogicalTime theTime,
                                       EventRetractionHandle retractionHandle) {


        if (newClientInstances.contains(theObject)) {
            try {
//                log("ReceivedClient");
                ArrayList<Pair<Integer, byte[]>> listOfPairs = new ArrayList<>();
                for (int i = 0; i < theAttributes.size(); i++) {
                    listOfPairs.add(new Pair<>(
                            theAttributes.getAttributeHandle(i),
                            theAttributes.getValue(i)
                            )
                    );
                }

                double time =  convertTime(theTime);

                PairComparator pairComparator = new PairComparator();
                Collections.sort(listOfPairs, pairComparator);

                BaseClient newClient = new BaseClient(
                        EncodingHelpers.decodeInt(listOfPairs.get(0).getValue()),
                        PetrolType.valueOf(EncodingHelpers.decodeString(listOfPairs.get(1).getValue())),
                        EncodingHelpers.decodeFloat(listOfPairs.get(2).getValue()),
                        EncodingHelpers.decodeBoolean(listOfPairs.get(3).getValue()),
                        time
                );

                receivedClients.add(newClient);
                log(newClient.explainYourself());
//                TODO if u wna to see what inside
//                for (Pair<Integer, byte[]> pair : listOfPairs) {
//                    System.out.println(pair.getKey() + ": " + pair.getValue());
//                }


            } catch (ArrayIndexOutOfBounds e) {
                throw new RuntimeException(e);
            }
        } else {
            log("Some strange thing happen");
        }
    }


    public void removeObjectInstance(int theObject,
                                     byte[] userSuppliedTag,
                                     LogicalTime theTime,
                                     EventRetractionHandle retractionHandle) {
//        log("Object Removed: handle=" + theObject);
    }

    /**
     * The RTI has informed us that time regulation is now enabled.
     */
    public void timeRegulationEnabled(LogicalTime theFederateTime) {
        this.federateTime = convertTime(theFederateTime);
        this.isRegulating = true;
    }

    public void timeConstrainedEnabled(LogicalTime theFederateTime) {
        this.federateTime = convertTime(theFederateTime);
        this.isConstrained = true;
    }

    public void timeAdvanceGrant(LogicalTime theTime) {
        this.grantedTime = convertTime(theTime);
        this.isAdvancing = false;
    }


    public void receiveInteraction(int interactionClass,
                                   ReceivedInteraction theInteraction,
                                   byte[] tag) {
        // just pass it on to the other method for printing purposes
        // passing null as the time will let the other method know it
        // it from us, not from the RTI
        receiveInteraction(interactionClass, theInteraction, tag, null, null);
    }

    public void receiveInteraction(int interactionClass,
                                   ReceivedInteraction theInteraction,
                                   byte[] tag,
                                   LogicalTime theTime,
                                   EventRetractionHandle eventRetractionHandle) {

        StringBuilder builder = new StringBuilder("Interaction Received:");
        builder.append("Cash ");
        running = false;
        builder.append("*** STOP SIM ***");
        log(builder.toString());
    }

}
