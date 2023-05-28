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
    protected int startPetrolService = 0;
    protected int stopPetrolService = 0;


    public ClientsAmbassador() {

    }

    private double convertTime(LogicalTime logicalTime) {
        // PORTICO SPECIFIC!!
        return ((DoubleTime) logicalTime).getTime();
    }

    private void log(String message) {
        System.out.println("FederateAmbassador: " + message);
    }

    public void synchronizationPointRegistrationFailed(String label) {
        log("Failed to register sync point: " + label);
    }

    public void synchronizationPointRegistrationSucceeded(String label) {
        log("Successfully registered sync point: " + label);
    }

    public void announceSynchronizationPoint(String label, byte[] tag) {
        log("Synchronization point announced: " + label);
        if (label.equals(GasStationAmbassador.READY_TO_RUN))
            this.isAnnounced = true;
    }

    public void federationSynchronized(String label) {
        log("Federation Synchronized: " + label);
        if (label.equals(GasStationAmbassador.READY_TO_RUN))
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

        if (interactionClass == startPetrolService) {
            try {
                builder.append("startPetrolService: ");
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time = EncodingHelpers.decodeDouble(theInteraction.getValue(1));
                builder.append("id=").append(clientId).append(", time=").append(time);
                for (Client cl : ClientsFederate.clients) {
                    if (cl.getId() == clientId) {
                        cl.startPetrolService = time;
                    }
                }

            } catch (ArrayIndexOutOfBounds e) {
                throw new RuntimeException(e);
            }
        } else if (interactionClass == stopPetrolService) {
            try {
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time = EncodingHelpers.decodeDouble(theInteraction.getValue(1));

                for (Client cl : ClientsFederate.clients) {
                    if (cl.getId() == clientId) {
                        cl.endPetrolService = time;
                    }
                }

            } catch (ArrayIndexOutOfBounds e) {
                throw new RuntimeException(e);
            }
        }
//        // print the handle
//        builder.append( " handle=" + interactionClass );
//        // print the tag
//        builder.append( ", tag=" + EncodingHelpers.decodeString(tag) );
//        // print the time (if we have it) we'll get null if we are just receiving
//        // a forwarded call from the other reflect callback above
//        if( theTime != null )
//        {
//            builder.append( ", time=" + convertTime(theTime) );
//        }
//
//        // print the parameer information
//        builder.append( ", parameterCount=" + theInteraction.size() );
//        builder.append( "\n" );
//        for( int i = 0; i < theInteraction.size(); i++ )
//        {
//            try
//            {
//                // print the parameter handle
//                builder.append( "\tparamHandle=" );
//                builder.append( theInteraction.getParameterHandle(i) );
//                // print the parameter value
//                builder.append( ", paramValue=" );
//                builder.append(
//                        EncodingHelpers.decodeString(theInteraction.getValue(i)) );
//                builder.append( "\n" );
//            }
//            catch( ArrayIndexOutOfBounds aioob )
//            {
//                // won't happen
//            }
//        }

        log(builder.toString());
    }

}
