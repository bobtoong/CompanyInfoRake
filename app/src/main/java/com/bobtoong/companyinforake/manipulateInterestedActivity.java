package com.bobtoong.companyinforake;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class manipulateInterestedActivity extends AppCompatActivity {

    DataManager dm = null;
    CustomChoiceListViewAdapter listAdapter = null; // ArrayAdapter<String> listAdapter = null;
    ListView companyListView = null;
    TextView companyCode = null;
    TextView companyName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manipulate_interested);

        companyCode = (TextView)findViewById(R.id.maniInterestedCompanyCode);
        companyName = (TextView)findViewById(R.id.maniInterestedCompanyName);
        companyListView = (ListView)findViewById(R.id.maniInterestedCompanyList);
        dm = new DataManager(this);
        dm.init_database();

        InterestedCompany ic = new InterestedCompany();
        ArrayList<String> ical = new ArrayList<>();
        listAdapter = new CustomChoiceListViewAdapter();//ArrayAdapter(this, android.R.layout.simple_list_item_1, ical);

        int icCount = dm.getInterestedCompanyCount();
        for (int i = 0; i < icCount; i++){
            ic = dm.getInterestedCompany(i);
            listAdapter.addItem(ic.CompanyID , ic.CompanyName);
//            ical.add(ic.CompanyID + ", " + ic.CompanyName);
        }

        companyListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        companyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InterestedCompany ic = new InterestedCompany();
                ic = dm.getInterestedCompany(position);
                companyCode.setText(ic.CompanyID);
                companyName.setText(ic.CompanyName);
            }
        });

        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        // button click 처리
        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        Button CompanyInfoInsert = (Button) findViewById(R.id.CompanyInfoInsert);
        CompanyInfoInsert.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                InterestedCompany ic = new InterestedCompany();
                ic.CompanyID = companyCode.getText().toString();
                ic.CompanyName = companyName.getText().toString();

                if (dm.insertInterestedCompany(ic) == false){
                    return;
                }
                listAdapter.addItem(ic.CompanyID , ic.CompanyName);
            }
        });

        Button CompanyInfoMani = (Button) findViewById(R.id.CompanyInfoMani);
        CompanyInfoMani.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        Button CompanyInfoDelete = (Button) findViewById(R.id.CompanyInfoDelete);
        CompanyInfoDelete.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                SparseBooleanArray checkedItems = companyListView.getCheckedItemPositions();
                int count = listAdapter.getCount() ;

                for (int i = count-1; i >= 0; i--) {
                    if (checkedItems.get(i)) {
                        String companyCode = listAdapter.deleteItem(i);
                        dm.deleteInterestedCompany(companyCode);
                    }
                }

                // 모든 선택 상태 초기화.
                companyListView.clearChoices() ;
                listAdapter.notifyDataSetChanged();
                companyCode.setText("");
                companyName.setText("");
            }
        });
    }
}
