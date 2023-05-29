package hla13.solution.kasy;

public class Client {

    private final int id;
    private final double timeArrive;
    private double paymentTime;
    private int numberInQueue;

    public Client(int id, double timeArrive){
        this.id = id;
        this.timeArrive = timeArrive;
    }

    public int getId(){return id;}
    public double getArriveTime(){return timeArrive;}
    public void setPaymentTime(double time){
        this.paymentTime = time;
    }
    public double getPaymentTime() {
        return paymentTime;
    }

    public void setNumberInQue(int index) {
        this.numberInQueue = index;
    }
    public int getNumberInQue() {
        return numberInQueue;
    }
    public String explainYourself() {
        return "Clientid:  " + this.id + " at time:  " + this.timeArrive;
    }
}
