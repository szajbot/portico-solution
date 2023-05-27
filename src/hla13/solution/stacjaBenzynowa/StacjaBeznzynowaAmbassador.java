package hla13.solution.stacjaBenzynowa;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import org.portico.impl.hla13.types.DoubleTime;

import java.util.ArrayList;


public class StacjaBeznzynowaAmbassador extends NullFederateAmbassador {

    //----------------------------------------------------------
    //                    STATIC VARIABLES
    //----------------------------------------------------------

    public static final String READY_TO_RUN = "ReadyToRun";

    //----------------------------------------------------------
    //                   INSTANCE VARIABLES
    //----------------------------------------------------------
    // these variables are accessible in the package
    protected double federateTime        = 0.0;
    protected double grantedTime         = 0.0;
    protected double federateLookahead   = 1.0;

    protected boolean isRegulating       = false;
    protected boolean isConstrained      = false;
    protected boolean isAdvancing        = false;

    protected boolean isAnnounced        = false;
    protected boolean isReadyToRun       = false;

    protected boolean running 			 = true;

    protected int newClientHandle = 0;
    protected int test2Handle = 0;
    protected int test3Handle = 0;

    protected ArrayList<ExternalEvent> externalEvents = new ArrayList<>();


    private double convertTime( LogicalTime logicalTime )
    {
        // PORTICO SPECIFIC!!
        return ((DoubleTime)logicalTime).getTime();
    }

    private void log( String message )
    {
        System.out.println( "FederateAmbassador: " + message );
    }

    public void synchronizationPointRegistrationFailed( String label )
    {
        log( "Failed to register sync point: " + label );
    }

    public void synchronizationPointRegistrationSucceeded( String label )
    {
        log( "Successfully registered sync point: " + label );
    }

    public void announceSynchronizationPoint( String label, byte[] tag )
    {
        log( "Synchronization point announced: " + label );
        if( label.equals(READY_TO_RUN) )
            this.isAnnounced = true;
    }

    public void federationSynchronized( String label )
    {
        log( "Federation Synchronized: " + label );
        if( label.equals(READY_TO_RUN) )
            this.isReadyToRun = true;
    }

    /**
     * The RTI has informed us that time regulation is now enabled.
     */
    public void timeRegulationEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isRegulating = true;
    }

    public void timeConstrainedEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isConstrained = true;
    }

    public void timeAdvanceGrant( LogicalTime theTime )
    {
        this.grantedTime = convertTime( theTime );
        this.isAdvancing = false;
    }


    public void receiveInteraction( int interactionClass,
                                    ReceivedInteraction theInteraction,
                                    byte[] tag )
    {
        // just pass it on to the other method for printing purposes
        // passing null as the time will let the other method know it
        // it from us, not from the RTI
        receiveInteraction(interactionClass, theInteraction, tag, null, null);
    }

    public void receiveInteraction(int interactionClass,
                                   ReceivedInteraction theInteraction,
                                   byte[] tag,
                                   LogicalTime theTime,
                                   EventRetractionHandle eventRetractionHandle ) {

//        TODO add more interactions then do proper log builder
        StringBuilder builder = new StringBuilder( "Interaction Received:" );
        log(builder.toString());

        if(interactionClass == newClientHandle) {
            try {
                log(String.valueOf(theInteraction.size()));
                Integer number = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                String message = EncodingHelpers.decodeString(theInteraction.getValue(1));
                double time = convertTime(theTime);
                ArrayList<String> messages = new ArrayList<>();
                ArrayList<Integer> numbers = new ArrayList<>();
                messages.add(message);
                numbers.add(number);
                externalEvents.add(new ExternalEvent(numbers, messages, ExternalEvent.EventType.NEW_CLIENT, time));
            } catch (ArrayIndexOutOfBounds ignored) {

            }
        }
    }

}
