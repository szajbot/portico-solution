package hla13.solution.klienci;

import hla13.solution.BaseClient;
import hla13.solution.PetrolType;
import javafx.util.Builder;

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

    public String printAttributes(){
        StringBuilder result = new StringBuilder();

        result.append("WashQue=").append(this.numberInWashingQueue)
                .append(", PetrolQue=").append(this.numberInPetrolQueue)
                .append(", PaymentQue=").append(this.numberInPaymentQueue)
                .append(", StartTime=").append(this.startPetrolService)
                .append(", EndPetrol=").append(this.endPetrolService);
        if (startWashing!=0){
            result.append(", StartWash=").append(this.startWashing)
                    .append(", EndWash=").append(this.endWashing);
        }
        result.append(", StartPayment=").append(this.startPayment)
                .append(", EndPayment=").append(this.endPayment);

        return result.toString();
    }
}
