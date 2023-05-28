package hla13.solution.klienci;

public class Klient {

    int id;
    PetrolType petrolType;
    float fuelQuantity;
    boolean washOption;

    public Klient(int clientNumber, PetrolType petrolType, float fuelQuantity, boolean washOption) {
        this.id = clientNumber;
        this.petrolType = petrolType;
        this.fuelQuantity = fuelQuantity;
        this.washOption = washOption;
    }

    public enum PetrolType {ON, BENZYNA}

    int numberInQueue;
    int objectNumber;
    int arriveTime;
    int exitTime;

}
