package hla13.solution.klienci;

import hla13.solution.BaseClient;
import hla13.solution.PetrolType;

public class Client extends BaseClient {

    int numberInQueue;
    int objectNumber;
    int arriveTime;
    int exitTime;

    public Client(int clientNumber, PetrolType petrolType, float fuelQuantity, boolean washOption) {
        super(clientNumber, petrolType, fuelQuantity, washOption);
    }
}
