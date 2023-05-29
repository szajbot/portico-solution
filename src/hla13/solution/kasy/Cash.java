package hla13.solution.kasy;

import java.util.ArrayList;
import java.util.Random;

public class Cash {

    private ArrayList<Client> queue = new ArrayList<>();
    private double nextPaymentTime;
    Random random;
    Boolean isInUse = false;

    public Boolean getInUse() {
        return isInUse;
    }

    public void setInUse(Boolean inUse) {
        isInUse = inUse;
    }

    public ArrayList<Client> getQueue() {
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
        return 3 + 20 * random.nextDouble();
    }
}
