package com.bobtoong.companyinforake;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Owner on 2018-04-05.
 */

public class DataManager {
    private static final String				TAG							= "CompanyInfo/DataManager";
    private static SQLiteDatabase tempdb = null;
    private static SQLiteDatabase CompanyInfoRakeDB = null;
    private static boolean initialized = false;
    private Context mContext = null;

    public DataManager(Context context){
        //super(context);
        this.mContext = context;
    }

    public boolean init_database(){
        if (initialized == true){
            Log.e(TAG, "init_database : data base is already initialized.");
            return true;
        }
        // 회사의 공시 정보를 가져다가 임시로 저장할 db file을 램에 생성
        File file = new File("", ":memory:"/*"contact.db"*/);
        if (file == null){
            Log.e(TAG, "PATH : " + file.toString() + " creaton error.");
            return false;
        }
        Log.i(TAG, "PATH : " + file.toString());
        try {
            tempdb = SQLiteDatabase.openOrCreateDatabase(file, null);
        } catch (SQLiteException e){
            e.printStackTrace();
            return false;
        }

        if (initCompanyDITable() == false){
            return false;
        }

        // 영구적으로 저장할 db file을 생성
        file = new File(mContext.getFilesDir().getAbsolutePath(), "CompanyInfoRake.db");
        if (file == null){
            Log.e(TAG, "PATH : " + file.toString() + " creaton error.");
            return false;
        }

        try{
            CompanyInfoRakeDB = SQLiteDatabase.openOrCreateDatabase(file, null);
        } catch (SQLiteException e){
            e.printStackTrace();
            return false;
        }

        if (initInterestedCompanyTable() == false){
            return false;
        }

        initialized = true;
        return true;
    }

    //  관심있는 Company관리 Table이 존재하는지 확인, 없으면 새로 만든다
    //  테이블을 새로 만드는 경우에는 초기 데이터도 입력한다.
    private boolean initInterestedCompanyTable(){
        if (CompanyInfoRakeDB == null){
            Log.e(TAG, "initInterestedCompanyTable is failed, database is not initialized!");
            return false;
        }

        String Query = "SELECT name FROM sqlite_master WHERE type='table' AND name ='INTERESTEDCOMPANY_T';";
        Cursor cursor = CompanyInfoRakeDB.rawQuery(Query , null);
        cursor.moveToFirst();
        if(cursor.getCount()>0){
            Log.e(TAG, "initInterestedCompanyTable is failed, INTERESTEDCOMPANY_T is already exist.");
            return true;
        }

        Query = "CREATE TABLE IF NOT EXISTS INTERESTEDCOMPANY_T (" + // To create Interested Company Table
                "COMPANY_ID " + "TEXT UNIQUE," +
                "COMPANY_NAME " + "TEXT" +
                ");";

        Log.i(TAG, Query);
        CompanyInfoRakeDB.execSQL(Query);

        // 최초 실행 시 국내 대표 회사들로 채워 넣음. 이후 User가 추가/삭제 하도록 함
        Query = "INSERT INTO INTERESTEDCOMPANY_T " + "(COMPANY_ID, COMPANY_NAME) VALUES ('066570', 'LG전자');";
        Log.i(TAG, Query);
        CompanyInfoRakeDB.execSQL(Query);
        Query = "INSERT INTO INTERESTEDCOMPANY_T " + "(COMPANY_ID, COMPANY_NAME) VALUES ('005930', '삼성전자');";
        Log.i(TAG, Query);
        CompanyInfoRakeDB.execSQL(Query);
        Query = "INSERT INTO INTERESTEDCOMPANY_T " + "(COMPANY_ID, COMPANY_NAME) VALUES ('005380', '현대 자동차');";
        Log.i(TAG, Query);
        CompanyInfoRakeDB.execSQL(Query);
        Query = "INSERT INTO INTERESTEDCOMPANY_T " + "(COMPANY_ID, COMPANY_NAME) VALUES ('051910', 'LG 화학');";
        Log.i(TAG, Query);
        CompanyInfoRakeDB.execSQL(Query);
        Query = "INSERT INTO INTERESTEDCOMPANY_T " + "(COMPANY_ID, COMPANY_NAME) VALUES ('017670', 'SK텔레콤');";
        Log.i(TAG, Query);
        CompanyInfoRakeDB.execSQL(Query);
        Query = "INSERT INTO INTERESTEDCOMPANY_T " + "(COMPANY_ID, COMPANY_NAME) VALUES ('030200', 'KT');";
        Log.i(TAG, Query);
        CompanyInfoRakeDB.execSQL(Query);
        return true;
    }

    public int getInterestedCompanyCount(){
        if (CompanyInfoRakeDB == null){
            Log.e(TAG, "getInterestedCompanyCount is failed, database is not initialized!");
            return -1;
        }

        int count = -1;
        String sqlQueryTbl = "SELECT * FROM INTERESTEDCOMPANY_T;";
        Cursor cursor = null ; // 쿼리 실행
        cursor = CompanyInfoRakeDB.rawQuery(sqlQueryTbl, null) ;

        count = cursor.getCount();
        cursor.close();
        return count;
    }

    public InterestedCompany getInterestedCompany(int idx){
        if (CompanyInfoRakeDB == null || idx < 0){
            Log.e(TAG, "getCompanyDI is failed, database is not initialized!");
            return null;
        }
        String sqlQueryTbl = "SELECT * FROM INTERESTEDCOMPANY_T;";
        Cursor cursor = null ; // 쿼리 실행
        cursor = CompanyInfoRakeDB.rawQuery(sqlQueryTbl, null) ;
        InterestedCompany ic = new InterestedCompany();

        if (idx >= cursor.getCount()){
            Log.e(TAG, "getCompanyDI is failed, queried index is bigger than count of table!");
            return null;
        }
        cursor.moveToPosition(idx);
        ic.CompanyID = cursor.getString(0);
        ic.CompanyName = cursor.getString(1);

        cursor.close();
        return ic;
    }

    public boolean insertInterestedCompany(InterestedCompany cmp){
        if (CompanyInfoRakeDB == null){
            Log.e(TAG, "insertInterestedCompany is failed, database is not initialized!");
            return false;
        }

        String  Query = "INSERT INTO INTERESTEDCOMPANY_T " + "(COMPANY_ID, COMPANY_NAME) VALUES ('" + cmp.CompanyID + "', '" + cmp.CompanyName + "');";
        Log.i(TAG, Query);
        CompanyInfoRakeDB.execSQL(Query);
        return true;
    }

    public boolean deleteInterestedCompany(String companyID){
        if (CompanyInfoRakeDB == null){
            Log.e(TAG, "deleteInterestedCompany is failed, database is not initialized!");
            return false;
        }

        String  Query = "DELETE FROM INTERESTEDCOMPANY_T WHERE COMPANY_ID = '" + companyID + "';";
        Log.i(TAG, Query);
        CompanyInfoRakeDB.execSQL(Query);
        return true;
    }

    //  CompanyDITable 존재하는지 확인, 없으면 새로 만든다
    private boolean initCompanyDITable(){
        if (tempdb == null){
            Log.e(TAG, "initCompanyDITable is failed, database is not initialized!");
            return false;
        }

        String Query = "SELECT name FROM sqlite_master WHERE type='table' AND name ='COMPANYDI_T';";
        Cursor cursor = tempdb.rawQuery(Query , null);
        cursor.moveToFirst();
        if(cursor.getCount()>0){
            Log.e(TAG, "initCompanyDITable is failed, COMPANYDI_T table is already existed!");
            return true;
        }

        Query = "CREATE TABLE IF NOT EXISTS COMPANYDI_T (" + // To create Company Disclosure Infomation Table
                "CRP_CLS " + "TEXT," +
                "CRP_NM " + "TEXT," +
                "CRP_CD " + "TEXT NOT NULL," +
                "RPT_NM " + "TEXT," +
                "RCP_NO " + "TEXT," +
                "FLR_NM " + "TEXT," +
                "RCP_DT " + "TEXT," +
                "RMK " + "TEXT" +
                ");";

        Log.i(TAG, Query);
        tempdb.execSQL(Query);
        return true;
    }

    public boolean InsertCompanyDI(CRP crp){
        if (tempdb == null){
            Log.e(TAG, "InsertCompanyDI is failed, database is not initialized!");
            return false;
        }
        String Query = "INSERT INTO COMPANYDI_T " + "(CRP_CLS, CRP_NM, CRP_CD, RPT_NM, RCP_NO, FLR_NM, RCP_DT, RMK) VALUES ('" +
                crp.crp_cls + "', '" + crp.crp_nm + "', '" +  crp.crp_cd + "', '" +   crp.rpt_nm  + "', '" +  crp.rcp_no + "', '" +  crp.flr_nm  + "', '" +  crp.rcp_dt + "', '" +  crp.rmk  + "');";

        Log.i(TAG, Query);
        try{
            tempdb.execSQL(Query);
        } catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }

    public boolean removeCompanyDI(CRP crp){
        if (tempdb == null){
            Log.e(TAG, "removeCompanyDI is failed, database is not initialized!");
            return false;
        }

        String Query = new String();
        if (crp == null){
            Query = "DELETE FROM COMPANYDI_T;";
        } else{
            Query = "DELETE FROM COMPANYDI_T " + "WHERE CRP_CLS = '" + crp.crp_cls + "';";

        }
        Log.i(TAG, Query);
        try{
            tempdb.execSQL(Query);
        } catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public int getCompanyDICount(){
        if (tempdb == null){
            Log.e(TAG, "getCompanyDICount is failed, database is not initialized!");
            return -1;
        }

        int count = -1;
        String sqlQueryTbl = "SELECT * FROM COMPANYDI_T;";
        Cursor cursor = null ; // 쿼리 실행
        cursor = tempdb.rawQuery(sqlQueryTbl, null) ;

        count = cursor.getCount();
        cursor.close();
        return count;
    }

    public CRP getCompanyDI(int idx){
        if (tempdb == null || idx < 0){
            Log.e(TAG, "getCompanyDI is failed, database is not initialized!");
            return null;
        }
        String sqlQueryTbl = "SELECT * FROM COMPANYDI_T;";
        Cursor cursor = null ; // 쿼리 실행
        cursor = tempdb.rawQuery(sqlQueryTbl, null) ;
        CRP crp = new CRP();

        if (idx >= cursor.getCount()){
            Log.e(TAG, "getCompanyDI is failed, queried index is bigger than count of table!");
            return null;
        }
        cursor.moveToPosition(idx);
        crp.crp_cls = cursor.getString(0);
        crp.crp_nm = cursor.getString(1);
        crp.crp_cd = cursor.getString(2);
        crp.rpt_nm = cursor.getString(3);
        crp.rcp_no = cursor.getString(4);
        crp.flr_nm = cursor.getString(5);
        crp.rcp_dt = cursor.getString(6);
        crp.rmk = cursor.getString(7);

        cursor.close();
        return crp;
    }
}


