package hla13.solution.stacjaBenzynowa;

import hla13.solution.klienci.Client;


public class ReceivedClient {
    int id;
    Client.PetrolType petrolType;
    float fuelQuantity;
    boolean washOption;

    public ReceivedClient(int clientNumber, Client.PetrolType petrolType, float fuelQuantity, boolean washOption) {
        this.id = clientNumber;
        this.petrolType = petrolType;
        this.fuelQuantity = fuelQuantity;
        this.washOption = washOption;
    }

    public int getId() {
        return id;
    }

    public Client.PetrolType getPetrolType() {
        return petrolType;
    }

    public float getFuelQuantity() {
        return fuelQuantity;
    }

    public boolean isWashOption() {
        return washOption;
    }

    public String explainYourself() {
        return "Clientid:  " + this.id + " PetrolType:  " + this.petrolType + "  Fuel Quantity:   " + this.fuelQuantity + "  Wash option:   " + this.washOption;
    }

}
