package hla13.solution.stacjaBenzynowa.ObjectsOnStation;

import hla13.solution.BaseClient;
import hla13.solution.PetrolType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Distributor extends ObjectOnStation {

    Random random;
    PetrolType petrolType;

    public Distributor(String name, LinkedList<BaseClient> queue, PetrolType petrolType) {
        this.name = name;
        this.queue = queue;
        this.petrolType = petrolType;
        this.random = new Random();
    }

    public Double serviceTime(Float quantity) {
//        random double from 1 to 10 + quantity
        return 50 + (10 - 1) * random.nextDouble() + quantity;
    }
}
