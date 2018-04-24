package Test;

import common.java.httpServer.booter;
import common.java.nlogger.nlogger;

public class TestWord {
    public static void main(String[] args) {
        booter booter = new booter();
        try {
            System.out.println("Words");
            System.setProperty("AppName", "Words");
            booter.start(1007);
        } catch (Exception e) {
            nlogger.logout(e);
        }
    }
}
