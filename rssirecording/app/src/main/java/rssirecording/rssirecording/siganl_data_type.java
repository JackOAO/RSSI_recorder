package rssirecording.rssirecording;

import java.util.ArrayList;
import java.util.List;

public class siganl_data_type {
    private List<Integer> rssi = new ArrayList<>();
    private String uuid = null;
    public siganl_data_type(String s, int i){
        rssi.add(i);
        uuid = s;
    }
    public void setvalue(String s){
        uuid = s;
    }
    public void setvalue(int i){
        rssi.add(i);
    }
    public String getUuid(){
        return uuid;
    }
    public int getrssi(int i){
        return rssi.get(i);
    }
    public String getrssilist(){
        return rssi.toString();
    }
    public int getrssi(){
        return rssi.get(0);
    }
    public int countsum(){
        int count=0;
        for (int i:rssi){
            count += i;
        }
        return count;
    }
    public float countavg(){
        int count=0,num=0;
        for (int i:rssi){
           count += i;
           num ++;
        }
        return (float)(count/num);
    }
}
