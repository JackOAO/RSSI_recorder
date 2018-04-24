package rssirecording.rssirecording;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
//Log.i("Queue2", o_member.toString());
public class ana_singal {
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
        List<Float> ana_data = new ArrayList<>();
        float findmaxrssi = -999;
        List<Integer> weight_list = new ArrayList<>();
        for (int i = 0; i<w; i++)
            weight_list.add((int) Math.pow(2,i));
        Collections.reverse(weight_list);
        Queue<String> weight_queue = new LinkedList<>();
        String find_max = "";
        for (int i=0; i<data_list.size(); i++) {
            int count = 0, count_rssi = 0;
            for (int j = 0; j < lq.size(); j++)
                if ((((List<String>) lq.get(j)).get(0)).equals(data_list.get(i))) {
                    count_rssi += Integer.parseInt(((List<String>) lq.get(j)).get(1));
                    count++;
                }
            ana_data.add((float) (count_rssi / count));
            if (ana_data.get(i) > findmaxrssi) {
                find_max = data_list.get(i);
                findmaxrssi = ana_data.get(i);
                ana_data.clear();
            }
        }
        weight_queue.add(find_max);
        if (weight_queue.size()>w) weight_queue.poll();



        return find_max;
    }
}
