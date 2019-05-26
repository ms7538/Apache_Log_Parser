package com.poloapps.apache_log_parser;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //private static final String TAG = "InspAppsCD";
    ArrayList<HashMap<String, String>> seqList;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();

        // get Access Log info in a new thread
        new Thread(new Runnable() {
            @SuppressWarnings("ConstantConditions")
            public void run() {
                List<LogEntry> AccessList = new ArrayList<>();
                try {
                    // Create a URL for the desired page
                    String urlStr = "https://dev.inspiringapps.com/Files/IAChallenge/";
                    urlStr += "30E02AAA-B947-4D4B-8FB6-9C57C43872A9/Apache.log";
                    URL url = new URL(urlStr);
                    // Read all the text returned by the server
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    String str;
                    int counter = 0;
                    while ((str = in.readLine()) != null) {
                        String user = str.substring(0,9);
                        int last_pg_index = str.indexOf("HTTP");
                        String page = str.substring(48, last_pg_index - 1);
                        LogEntry logEntry = new LogEntry(user, page, counter);
                        AccessList.add(logEntry);
                        counter++;
                    }
                    in.close();
                } catch (MalformedURLException e) {
                    Toast.makeText(getApplicationContext(), "Malformed URL Exception",
                            Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Input/Output Exception",
                            Toast.LENGTH_LONG).show();
                }

                // sort Access List by user IP
                Collections.sort(AccessList, new Comparator<LogEntry>() {
                    public int compare(LogEntry u1, LogEntry u2) {
                        return u1.getUserIP().compareTo(u2.getUserIP());
                    }
                });

                //initialize new hash for access sequence key val pairs
                HashMap<String, Integer> seqMap = new HashMap<>();

                for(int i = 0; i < AccessList.size() - 2; i++){
                    if(AccessList.get(i).getUserIP().equals(AccessList.get(i+1).getUserIP()) &&
                            AccessList.get(i).getUserIP().equals(AccessList.get(i+2).getUserIP()))
                    {
                        String keySeq = AccessList.get(i).getPage() + "->"
                                + AccessList.get(i+1).getPage() + "->"
                                + AccessList.get(i+2).getPage();
                        if(seqMap.containsKey(keySeq)){
                            int seqVal = seqMap.get(keySeq) + 1;
                            seqMap.put(keySeq,seqVal);
                        }else {
                            seqMap.put(keySeq, 1);
                        }
                    }
                    //Log.i(TAG, (AccessList.get(i).toString()));
                }
                Map<String, Integer> sortedMap = sortByValue(seqMap);
                seqList = new ArrayList<>();
                final ListView lv = findViewById(R.id.alp_listView);

                for (Map.Entry<String,Integer> entry : sortedMap.entrySet()) {
                    String key = entry.getKey();
                    Integer value = entry.getValue();

                    HashMap<String, String> item = new HashMap<>();
                    item.put("key", key);
                    item.put("value", Integer.toString(value));
                    seqList.add(item);
                    //Log.i(TAG, key + " " + value);
                }
                String[] from = {"key","value"};
                int[] to = {R.id.seq_string, R.id.seq_value};
                final ListAdapter listAdapter = new SimpleAdapter(getApplicationContext(), seqList,
                        R.layout.apl_list_item, from, to) {};
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lv.setAdapter(listAdapter);
                        dialog.dismiss();
                    }
                });

            }
        }).start();

    }
    public class LogEntry {
        String userIP;
        String page;
        int id;
        LogEntry(String userIP,String page, int id){
            this.userIP = userIP;
            this.page = page;
            this.id = id;
        }
        String getUserIP(){ return userIP; }
        String getPage(){ return page; }
    }

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hashMap)
    {
        // Create a list from elements of HashMap
        List<HashMap.Entry<String, Integer> > list = new LinkedList<>(hashMap.entrySet());

        // Sort the list
        Collections.sort(list, Collections. reverseOrder(
                new Comparator<Map.Entry<String, Integer> >(){
            public int compare(Map.Entry<String, Integer> obj1,
                               Map.Entry<String, Integer> obj2)
            {
                return (obj1.getValue()).compareTo(obj2.getValue());
            }
        }));

        // put data from sorted list to hash map
        HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}


