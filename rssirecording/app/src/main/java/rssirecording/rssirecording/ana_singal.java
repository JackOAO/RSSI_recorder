package rssirecording.rssirecording;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
//Log.i("Queue2", o_member.toString());
public class ana_singal {
    public String ana_singal_1(Queue q){
//        String[][] data_array = new String[10][2];
        List lq = new ArrayList<String>(q);
        List<String> data_list = new ArrayList<>();
        for (int i = 0;i < q.size();i++)
            if (data_list.indexOf(((List<String>) lq.get(i)).get(0)) == -1)
                data_list.add(((List<String>) lq.get(i)).get(0));
        float []ana_data = new float[data_list.size()];
        float findmaxrssi = -999;
        String find_max = "";
        for (int i=0; i<data_list.size(); i++) {
            int count = 0, count_rssi = 0;

            for (int j = 0; j < lq.size(); j++) {
//                Log.i("Queue", ((List<String>) lq.get(j)).get(0) +"\t"+ data_list.get(i));
                if ((((List<String>) lq.get(j)).get(0)).equals(data_list.get(i))) {
                    count_rssi += Integer.parseInt(((List<String>) lq.get(j)).get(1));
                    count++;
                }
            }
            ana_data[i] = count_rssi / count;
            if (ana_data[i] > findmaxrssi) {
                find_max = data_list.get(i);
                findmaxrssi = ana_data[i];
            }
//            Log.i("Queue2", data_list.get(i)+","+ana_data[i]+"\t");
//            Log.i("Queue3", find_max + "," + findmaxrssi + "\t");
        }

//        Log.i("Queue2", data_list.toString());
        return find_max;
    }
    public String ana_singal_2(Queue q){
        String[][] data_array = new String[10][2];
        List lq = new ArrayList<String>(q);
//        Log.i("Queue2", q.toString());
        for(int i = 0;i < q.size();i++){
            List tmpQ = (List<String>) lq.get(i);
            data_array[i] = new String[]{(String) tmpQ.get(0), (String) tmpQ.get(1)};
        }
        return "";
    }
}
