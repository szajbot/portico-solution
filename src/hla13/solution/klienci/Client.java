package hla13.solution.klienci;

import hla13.solution.BaseClient;
import hla13.solution.PetrolType;

public class Client extends BaseClient {

    int numberInPetrolQueue;
    int numberInWashingQueue;
    int numberInPaymentsQueue;
    int objectNumber;
    double arriveTime;
    double exitTime;
    double startPetrolService;
    double endPetrolService;
    double startWashing;
    double endWashing;
    double startPayments;
    double endPayments;


    public Client(int clientNumber, PetrolType petrolType, float fuelQuantity, boolean washOption) {
        super(clientNumber, petrolType, fuelQuantity, washOption);
    }
}
