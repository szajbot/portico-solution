package hla13.solution.klienci;

public class Klient {

    int id;

    public Klient(PetrolType petrolType, int clientNumber) {
        this.petrolType = petrolType;
        this.id = clientNumber;
    }

    public enum PetrolType {ON, BENZYNA}

    PetrolType petrolType;
    int numberInQueue;
    int objectNumber;
    int arriveTime;
    int exitTime;

}
