package hla13.solution.klienci;

import hla13.solution.BaseClient;
import hla13.solution.PetrolType;

public class Client extends BaseClient {

    int numberInPetrolQueue;
    int numberInWashingQueue;
    int numberInPaymentQueue;
    double startPetrolService; //arrive
    double endPetrolService;
    double startWashing = 0;
    double endWashing = 0;
    double startPayment;
    double endPayment; //exit


    public Client(int clientNumber, PetrolType petrolType, float fuelQuantity, boolean washOption) {
        super(clientNumber, petrolType, fuelQuantity, washOption);
    }
}
