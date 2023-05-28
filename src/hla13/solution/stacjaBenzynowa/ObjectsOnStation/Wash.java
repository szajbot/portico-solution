package hla13.solution.stacjaBenzynowa.ObjectsOnStation;

import hla13.solution.BaseClient;
import hla13.solution.PetrolType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Wash extends ObjectOnStation {

    Random random;

    public Wash(String name, LinkedList<BaseClient> queue) {
        this.name = name;
        this.queue = queue;
        this.random = new Random();
    }

    public Double serviceTime() {
//        random double from 10 to 30
        return 10 + (30 - 10) * random.nextDouble();
    }
}
