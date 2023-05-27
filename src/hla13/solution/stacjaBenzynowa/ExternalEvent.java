package hla13.solution.stacjaBenzynowa;


import java.util.ArrayList;
import java.util.Comparator;

public class ExternalEvent {

    public enum EventType {NEW_CLIENT, TEST2, TEST3}

    private ArrayList<Integer> numbers;

    private ArrayList<String> messages;
    private EventType eventType;
    private Double time;

    public ExternalEvent(ArrayList<Integer> numbers,
                         ArrayList<String> messages,
                         EventType eventType,
                         Double time) {
        this.numbers = numbers;
        this.messages = messages;
        this.eventType = eventType;
        this.time = time;
    }

    public EventType getEventType() {
        return eventType;
    }

    public double getTime() {
        return time;
    }

    public ArrayList<Integer> getNumbers() {
        return numbers;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    static class ExternalEventComparator implements Comparator<ExternalEvent> {

        @Override
        public int compare(ExternalEvent o1, ExternalEvent o2) {
            return o1.time.compareTo(o2.time);
        }
    }

}
