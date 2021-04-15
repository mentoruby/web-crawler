package crawler;

public class PageLoadException extends Exception {

    private static final long serialVersionUID = 7742976138812034180L;
    
    private int responseCode;
    
    
    public PageLoadException(int responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }


    public int getResponseCode() {
        return responseCode;
    }


    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
    
    
}
