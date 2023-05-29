package hla13.solution.stacjaBenzynowa.ObjectsOnStation;

import hla13.solution.BaseClient;

import java.util.ArrayList;
import java.util.LinkedList;

public class ObjectOnStation {

    String name;
    LinkedList<BaseClient> queue;
    double nextServiceTime;
    Boolean isInUse = false;

    public Boolean getInUse() {
        return isInUse;
    }

    public void setInUse(Boolean inUse) {
        isInUse = inUse;
    }

    public double getNextServiceTime() {return nextServiceTime;}

    public void setNextServiceTime(double nextServiceTime) {
        this.nextServiceTime = nextServiceTime;
    }


    public String getName() {
        return name;
    }

    public LinkedList<BaseClient> getQueue() {
        return queue;
    }
}
