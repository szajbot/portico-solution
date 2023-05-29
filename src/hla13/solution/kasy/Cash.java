package hla13.solution.kasy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Cash {

    private LinkedList<Client> queue = new LinkedList<>();
    private double nextPaymentTime;
    Random random = new Random();
    Boolean isInUse = false;

    public Cash(){}

    public Boolean getInUse() {
        return isInUse;
    }

    public void setInUse(Boolean inUse) {
        isInUse = inUse;
    }

    public LinkedList<Client> getQueue() {
        return queue;
    }

    public void addToQueue(Client client) {
        this.queue.add(client);
    }

    public double getNextPaymentTime() {
        return nextPaymentTime;
    }
    public void setNextPaymentTime(double time) {
        this.nextPaymentTime = time;
    }

    public Double serviceTime() {
        return 20 * random.nextDouble() + random.nextInt(20);
    }
}
