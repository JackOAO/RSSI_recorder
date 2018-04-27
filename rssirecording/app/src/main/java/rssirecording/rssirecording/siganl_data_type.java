package rssirecording.rssirecording;

public class siganl_data_type {
    private int rssi;
    private String uuid = null;
    public siganl_data_type(String s, int i){
        rssi = i;
        uuid = s;
    }
    public void setvalue(String s){
        uuid = s;
    }
    public void setvalue(int i){
        rssi = i;
    }
    public String getUuid(){
        return uuid;
    }
    public int getrssi(){
        return rssi;
    }
}
