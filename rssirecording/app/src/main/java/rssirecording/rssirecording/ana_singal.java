package rssirecording.rssirecording;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
//Log.i("Queue2", o_member.toString());
public class ana_singal {
    private Queue<siganl_data_type> weight_queue = new LinkedList<>();
    public String ana_singal_1(Queue q){
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
            for (int j = 0; j < lq.size(); j++)
                if ((((List<String>) lq.get(j)).get(0)).equals(data_list.get(i))) {
                    count_rssi += Integer.parseInt(((List<String>) lq.get(j)).get(1));
                    count++;
                }
            ana_data[i] = count_rssi / count;
            if (ana_data[i] > findmaxrssi) {
                find_max = data_list.get(i);
                findmaxrssi = ana_data[i];
            }
        }
        return find_max;
    }
    public String ana_singal_2(Queue q,int w){
//      10筆data一組計算權限，兩次最高才轉換
        List lq = new ArrayList<String>(q);
        List<String> data_list = new ArrayList<>();
        for (int i = 0;i < q.size();i++)
            if (data_list.indexOf(((List<String>) lq.get(i)).get(0)) == -1)
                data_list.add(((List<String>) lq.get(i)).get(0));
        List<Integer> weight_list = new ArrayList<>();
        for (int i = 0; i<w; i++)
            weight_list.add((int) Math.pow(2,i));
        Collections.reverse(weight_list);
        String find_max = "";
        int findmaxrssi = -999;
        List<Float> ana_data = new ArrayList<>();
        for (int i=0; i<data_list.size(); i++) {
            int count = 0, count_rssi = 0;
            for (int j = 0; j < lq.size(); j++)
                if ((((List<String>) lq.get(j)).get(0)).equals(data_list.get(i))) {
                    count_rssi += Integer.parseInt(((List<String>) lq.get(j)).get(1));
                    count++;
                }
            ana_data.add((float)(count_rssi/count));
            if (ana_data.get(i) > findmaxrssi){
                find_max = data_list.get(i);
                findmaxrssi = Math.round(ana_data.get(i));
            }
        }
        siganl_data_type sdt = new siganl_data_type(find_max,findmaxrssi);
        weight_queue.add(sdt);
        if (weight_queue.size()>w) {
            weight_queue.poll();
        }
        List<siganl_data_type> get_weight_data= new ArrayList<>(weight_queue);
        List<siganl_data_type> count_data_weight= new ArrayList<>();
        for (int i = 0;i < get_weight_data.size();i++) {
            if (count_data_weight.indexOf(get_weight_data.get(i)) == -1) {
                count_data_weight.add(new siganl_data_type(get_weight_data.get(i).getUuid()
                        ,(get_weight_data.get(i).getrssi(0))*weight_list.get(i)));
            }
            else {
                count_data_weight.get(
                        count_data_weight.indexOf(
                                get_weight_data.get(i))).
                        setvalue((get_weight_data.get(i).getrssi(0))*weight_list.get(i));
            }
        }
        for (int i = 0; i < count_data_weight.size(); i++){
            Log.i("SL12",count_data_weight.get(i).getUuid()+
                    "\t" +count_data_weight.get(i).countavg());
            if (findmaxrssi < count_data_weight.get(i).countavg())
                find_max = count_data_weight.get(i).getUuid();
        }
        return find_max;
    }
}
