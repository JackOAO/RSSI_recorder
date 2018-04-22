package rssirecording.rssirecording;

import android.util.Log;

import java.util.List;
import java.util.Queue;
//Log.i("Queue2", o_member.toString());
public class ana_singal {
    public String ana_singal(Queue q){
        String[][] data_array = new String[10][2];
        Log.i("Queue2", q.toString());
        for(int i = 0;i < q.size();i++){
            List tmpQ = (List<String>) q.poll();
            data_array[i] = new String[]{tmpQ.get(0).toString(), tmpQ.get(1).toString()};
//            Log.i("Queue2", "["+data_array[i][0]+","+data_array[i][1]+"]"+i);
        }
        return "";
    }
}
