package hla13.solution;

public class BaseClient {
    int id;
    PetrolType petrolType;
    float fuelQuantity;
    boolean washOption;

    double time;

    public BaseClient(int clientNumber, PetrolType petrolType, float fuelQuantity, boolean washOption, double time) {
        this.id = clientNumber;
        this.petrolType = petrolType;
        this.fuelQuantity = fuelQuantity;
        this.washOption = washOption;
        this.time = time;
    }

    public BaseClient(int clientNumber, PetrolType petrolType, float fuelQuantity, boolean washOption) {
        this.id = clientNumber;
        this.petrolType = petrolType;
        this.fuelQuantity = fuelQuantity;
        this.washOption = washOption;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public PetrolType getPetrolType() {
        return petrolType;
    }

    public float getFuelQuantity() {
        return fuelQuantity;
    }

    public boolean isWashOption() {
        return washOption;
    }

    public String explainYourself() {
        return "Clientid:  " + this.id + " PetrolType:  " + this.petrolType + "  Fuel Quantity:   " + this.fuelQuantity + "  Wash option:   " + this.washOption + " at time:  " + this.time;
    }

}
