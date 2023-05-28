package hla13.solution.stacjaBenzynowa.ObjectsOnStation;

import hla13.solution.BaseClient;

import java.util.ArrayList;
import java.util.LinkedList;

public class ObjectOnStation {

    String name;
    LinkedList<BaseClient> queue;

    public Boolean getInUse() {
        return isInUse;
    }

    public void setInUse(Boolean inUse) {
        isInUse = inUse;
    }

    Boolean isInUse = false;



    public double getNextServiceTime() {
        return nextServiceTime;
    }

    public void setNextServiceTime(double nextServiceTime) {
        this.nextServiceTime = nextServiceTime;
    }

    double nextServiceTime;

    public String getName() {
        return name;
    }

    public LinkedList<BaseClient> getQueue() {
        return queue;
    }
}
