package hla13.solution.klienci;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import hla13.solution.stacjaBenzynowa.GasStationAmbassador;
import org.portico.impl.hla13.types.DoubleTime;


public class ClientsAmbassador extends NullFederateAmbassador {

    protected double federateTime = 0.0;
    protected double federateLookahead = 1.0;

    protected boolean isRegulating = false;
    protected boolean isConstrained = false;
    protected boolean isAdvancing = false;
    protected boolean isAnnounced = false;

    protected boolean isReadyToRun = false;
    protected boolean running = true;

    protected int petrolService = 0;
    protected int washService = 0;
    protected int startPayment = 0;
    protected int endPayment = 0;


    public ClientsAmbassador() {

    }

    private double convertTime(LogicalTime logicalTime) {
        // PORTICO SPECIFIC!!
        return ((DoubleTime) logicalTime).getTime();
    }

    private void log(String message) {
        System.out.println("ClientAmbassador: " + message);
    }

    public void synchronizationPointRegistrationFailed(String label) {
        log("Failed to register sync point: " + label);
    }

    public void synchronizationPointRegistrationSucceeded(String label) {
        log("Successfully registered sync point: " + label);
    }

    public void announceSynchronizationPoint(String label, byte[] tag) {
        log("Synchronization point announced: " + label);
        if (label.equals(ClientsFederate.READY_TO_RUN))
            this.isAnnounced = true;
    }

    public void federationSynchronized(String label) {
        log("Federation Synchronized: " + label);
        if (label.equals(ClientsFederate.READY_TO_RUN))
            this.isReadyToRun = true;
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
        this.federateTime = convertTime(theTime);
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
        StringBuilder builder = new StringBuilder("Interaction Received from ");

        if (interactionClass == petrolService) {
            try {
                builder.append("PetrolService: ");
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double startTime = EncodingHelpers.decodeDouble(theInteraction.getValue(1));
                double endTime = EncodingHelpers.decodeDouble(theInteraction.getValue(2));
                int queue = EncodingHelpers.decodeInt(theInteraction.getValue(3));
                builder.append("id=")
                        .append(clientId)
                        .append(", Start=")
                        .append(startTime)
                        .append(", End=")
                        .append(endTime)
                        .append(", Que=")
                        .append(queue);
                for (Client cl : ClientsFederate.clients) {
                    if (cl.getId() == clientId) {
                        cl.startPetrolService = startTime;
                        cl.endPetrolService = endTime;
                        cl.numberInPetrolQueue = queue;
                    }
                }

            } catch (ArrayIndexOutOfBounds e) {
                throw new RuntimeException(e);
            }
        }else if (interactionClass == washService) {
            try {
                builder.append("WashService: ");
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double startTime = EncodingHelpers.decodeDouble(theInteraction.getValue(1));
                double endTime = EncodingHelpers.decodeDouble(theInteraction.getValue(2));
                int queue = EncodingHelpers.decodeInt(theInteraction.getValue(3));
                builder.append("id=")
                        .append(clientId)
                        .append(", Start=")
                        .append(startTime)
                        .append(", End=")
                        .append(endTime)
                        .append(", Que=")
                        .append(queue);
                for (Client cl : ClientsFederate.clients) {
                    if (cl.getId() == clientId) {
                        cl.startWashing = startTime;
                        cl.endWashing = endTime;
                        cl.numberInWashingQueue = queue;
                    }
                }

            } catch (ArrayIndexOutOfBounds e) {
                throw new RuntimeException(e);
            }
        }else if (interactionClass == startPayment) {
            try {
                builder.append("StartPayment: ");
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double startTime = EncodingHelpers.decodeDouble(theInteraction.getValue(1));
                builder.append("id=")
                        .append(clientId)
                        .append(", Start=")
                        .append(startTime);
                for (Client cl : ClientsFederate.clients) {
                    if (cl.getId() == clientId) {
                        cl.startPayment = startTime;
                    }
                }

            } catch (ArrayIndexOutOfBounds e) {
                throw new RuntimeException(e);
            }
        }else if (interactionClass == endPayment) {
            try {
                builder.append("EndPayment: ");
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double endTime = EncodingHelpers.decodeDouble(theInteraction.getValue(1));
                int queue = EncodingHelpers.decodeInt(theInteraction.getValue(2));
                builder.append("id=")
                        .append(clientId)
                        .append(", End=")
                        .append(endTime)
                        .append(", Que=")
                        .append(queue);
                for (Client cl : ClientsFederate.clients) {
                    if (cl.getId() == clientId) {
                        cl.endPayment = endTime;
                        cl.numberInPaymentQueue = queue;
                    }
                }

            } catch (ArrayIndexOutOfBounds e) {
                throw new RuntimeException(e);
            }
        }

            log(builder.toString());
        }

    }
