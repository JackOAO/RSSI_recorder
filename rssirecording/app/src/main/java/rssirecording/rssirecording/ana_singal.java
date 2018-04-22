package rssirecording.rssirecording;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
//Log.i("Queue2", o_member.toString());
public class ana_singal {
    public String ana_singal_1(Queue q){
//        String[][] data_array = new String[10][2];
        List lq = new ArrayList<String>(q);
        List<List<String>> data_list = new ArrayList<>();
        for(int i = 0;i < q.size();i++){
            String tmpQ = ((List<String>) lq.get(i)).get(0);
//            Log.i("Queue2", ((List<String>)data_list.get(i)).get(0));
            if(data_list.isEmpty()) {
                data_list.add(new ArrayList<String>());
                ((List<String>) data_list.get(i)).add(tmpQ);
                ((List<String>) data_list.get(i)).add(((List<String>) lq.get(i)).get(1));
            }
//            String s = "0xd57bbb410xa89ef042";
//            Log.i("Queue2", String.valueOf(((List<String>)data_list.get(i)).get(0).indexOf(s)));
//            else
//                for (int j = 0, j)
//                        (((List<String>)data_list.get(i)).get(0).indexOf(tmpQ)==-1) {
//                data_list.add(new ArrayList<String>());
//                ((List<String>) data_list.get(i)).add(tmpQ);

//            Log.i("Queue2", data_list.toString());
//            for (int j = 0;j < data_list.size();j++){
//
//                if(((List<String>)data_list.get(j)).get(0).indexOf(tmpQ)==-1){
//                    data_list.add(new ArrayList<String>());
//                    ((List<String>)data_list.get(j)).add(tmpQ);
//                }
//            }
        }
        Log.i("Queue2", data_list.toString());
        return "";
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
