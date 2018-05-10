package com.bobtoong.companyinforake;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

public class MainActivity extends AppCompatActivity {

    private static final String				TAG							= "CompanyInfo/Main";
    ExtractCompanyInfo CompanyInfo = null;
    DataManager dm = null;
    List<String> companyDI = null;
    ArrayAdapter<String> listAdapter = null;
    ListView companyDIListView = null;

    class SpnOnSelectedListener implements Spinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            // 선택된 회사의 정보 얻어 옮
            InterestedCompany ic = dm.getInterestedCompany(position);
            // 선택된 회사의 정보를 바탕으로 보고서 리스트 얻어 옮
            //  async로 동작할줄 알았더니 synchronous하게 동작하는듯...
            CompanyInfo.fetchCL(ic.CompanyID);
            companyDI.clear();

            // SSSSSSSS 여기서 DI Temp Database에 있는 값을 listview와 연결된 arraylist에 넣어 줌
            int companyDICount = dm.getCompanyDICount();
            for (int i = 0; i < companyDICount; i++){
                CRP crp = dm.getCompanyDI(i);
                companyDI.add(crp.crp_nm + ", " + crp.rpt_nm + ", " +  crp.rcp_dt);
            }
            listAdapter.notifyDataSetChanged();
            companyDIListView.setAdapter(listAdapter);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 이걸 추가해야 network이 정상 동작 하는데... Strict mode랑 무슨 관계가 있는 것일까? 2018.04.03
        // To keep this example simple, we allow network access
        // in the user interface thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        CompanyInfo = new ExtractCompanyInfo(this);
        dm = new DataManager(this);
        dm.init_database();
        companyDI = new ArrayList<>();

        InterestedCompany ic = new InterestedCompany();
        ArrayList<String> ical = new ArrayList<>();

        int icCount = dm.getInterestedCompanyCount();
        for (int i = 0; i < icCount; i++){
            ic = dm.getInterestedCompany(i);
            ical.add(ic.CompanyID + ", " + ic.CompanyName);
        }

        onCreate에서 data load하던 것을 onResume으로 옮길 것
        Spinner Main_spinner = (Spinner)findViewById(R.id.CompanyInfoSP);
        ArrayAdapter<String> sAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ical);
        sAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, companyDI);
        companyDIListView = (ListView)findViewById(R.id.CompanyDI);

        // 스피너에 사용자가 관심있는 회사 리스트를 넣어놓고, 선택하면 해당 회사의 보고서 목록을 실시간 조회해서 리스트로 보여 줌
        Main_spinner.setAdapter(sAdapter);
        SpnOnSelectedListener onSelectedListener = new SpnOnSelectedListener();
        Main_spinner.setOnItemSelectedListener(onSelectedListener);
/*
        Main_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 선택된 회사의 정보 얻어 옮
                InterestedCompany ic = dm.getInterestedCompany(position);
                // 선택된 회사의 정보를 바탕으로 보고서 리스트 얻어 옮
                //  async로 동작할줄 알았더니 synchronous하게 동작하는듯...
                CompanyInfo.fetchCL(ic.CompanyID);
                companyDI.clear();

                // SSSSSSSS 여기서 DI Temp Database에 있는 값을 listview와 연결된 arraylist에 넣어 줌
                int companyDICount = dm.getCompanyDICount();
                for (int i = 0; i < companyDICount; i++){
                    CRP crp = dm.getCompanyDI(i);
                    companyDI.add(crp.crp_nm + ", " + crp.rpt_nm + ", " +  crp.rcp_dt);
                }
                listAdapter.notifyDataSetChanged();
                companyDIListView.setAdapter(listAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
*/
        Button CompanyInfoManiBtn = (Button) findViewById(R.id.CompanyInfoManiBtn) ;
        CompanyInfoManiBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent  intent;
                intent = new Intent(MainActivity.this, manipulateInterestedActivity.class);
//                intent.putExtra("companyDIDetail", url);
                startActivity(intent);
            }
        });

        companyDIListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 위의 SSSSSSSS에서 추가한 list와 sync가 맞아야 함. 틀려지는 경우가 생기지 않도록 주의를 기울일 것
                CRP crp = dm.getCompanyDI(position);
//                String DIfile = CompanyInfo.fetchDiclosureDetail(crp.rcp_no);
                String url = "http://m.dart.fss.or.kr/html_mdart/MD1007.html?rcpNo=" + crp.rcp_no;
                Intent  intent;
                intent = new Intent(MainActivity.this, companyDIDetailActivity.class);
                intent.putExtra("companyDIDetail", url);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Spinner Main_spinner = (Spinner)findViewById(R.id.CompanyInfoSP);

        SpnOnSelectedListener onSelectedListener = new SpnOnSelectedListener();
        Main_spinner.setOnItemSelectedListener(onSelectedListener);
    }

}
