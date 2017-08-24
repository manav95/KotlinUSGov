package com.example.manavdutta1.myapplication;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by manavdutta1 on 8/23/17.
 */

public class ListSpendingActivity extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // We'll define a custom screen layout here (the one shown above), but
        // typically, you could just use the standard ListActivity layout.
        setContentView(R.layout.activity_list_spending);

        List<Map<String, String>> awardMap = new ArrayList<>();
        ArrayList<Award> awards = getIntent().getParcelableArrayListExtra("AWARDLIST");
        String type = getIntent().getExtras().getString("TYPE");
        awardMap = constructMap(awards);
        String[] args = {"Category", "Type Description", "Description", "Created Date", "Recipient Name", "Total Obligation"};
        int[] viewArgs = {R.id.category, R.id.typeDesc, R.id.desc, R.id.createdDt, R.id.recipient, R.id.totalObligation};

        ListAdapter adapter = new SimpleAdapter(this, awardMap, R.layout.row_layout, args, viewArgs);

        setListAdapter(adapter);
    }

    private List<Map<String,String>> constructMap(ArrayList<Award> awards) {
          List<Map<String, String>> maps = new ArrayList<>();
          for (Award award: awards) {
              Map<String, String > map = new HashMap();
              map.put("Category", award.getCategory());
              map.put("Type Description", award.getTypeDescription());
              if (award.getDescription().length() > 25) {
                  award.setDescription(award.getDescription().substring(0, 24));
              }
              map.put("Description", award.getDescription());
              map.put("Created Date", award.getCreatedDt().toString());
              map.put("Recipient Name", award.getRecipient());
              map.put("Total Obligation", "$" + award.getTotalObligation().toString());
              maps.add(map);
          }
          return maps;
    }

}
