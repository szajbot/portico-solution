package hla13.solution;

public class BaseClient {
    int id;
    PetrolType petrolType;
    float fuelQuantity;
    boolean washOption;
    double arriveTime;
    int numberInQue;
    double lastServiceEndTime;

    public BaseClient(int clientNumber, PetrolType petrolType, float fuelQuantity, boolean washOption, double arriveTime) {
        this.id = clientNumber;
        this.petrolType = petrolType;
        this.fuelQuantity = fuelQuantity;
        this.washOption = washOption;
        this.arriveTime = arriveTime;
    }

    public BaseClient(int clientNumber, PetrolType petrolType, float fuelQuantity, boolean washOption) {
        this.id = clientNumber;
        this.petrolType = petrolType;
        this.fuelQuantity = fuelQuantity;
        this.washOption = washOption;
    }

    public double getLastServiceEndTime() {
        return lastServiceEndTime;
    }

    public void setLastServiceEndTime(double lastServiceEndTime) {
        this.lastServiceEndTime = lastServiceEndTime;
    }

    public double getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(double arriveTime) {
        this.arriveTime = arriveTime;
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
    public int getNumberInQue(){return numberInQue;}
    public void setNumberInQue(int length){ this.numberInQue = length;}

    public boolean isWashOption() {
        return washOption;
    }

    public String explainYourself() {
        return "Clientid:  " + this.id + " PetrolType:  " + this.petrolType + "  Fuel Quantity:   " + this.fuelQuantity + "  Wash option:   " + this.washOption;
    }

}
