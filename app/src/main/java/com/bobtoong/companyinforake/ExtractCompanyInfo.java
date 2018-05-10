package com.bobtoong.companyinforake;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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

/**
 * Created by Owner on 2018-04-04.
 */
class CRP{
        String crp_cls;
        String crp_nm;
        String crp_cd;
        String rpt_nm;
        String rcp_no;
        String flr_nm;
        String rcp_dt;
        String rmk;
}

class InterestedCompany {
    String CompanyID;
    String CompanyName;
}

public class ExtractCompanyInfo {
    private static final String				 TAG							= "CompanyInfo/PARSEXML";
    private static final String               API_KEY                     = "a848774d09901a8eeb2623d5531f63cb5cc895ee";
    public XmlPullParser                        parser = null;
//    ExtractCompanyInfo CompanyInfo = null;
    CRP crpRecord = new CRP();
    DataManager dm = null;
    Context mContext = null;

    ExtractCompanyInfo(Context context){
        mContext = context;
        try {
            parser = Xml.newPullParser() ;
        } catch (Exception e) {
            Log.e(TAG, "xml Parsing error : " + e.getMessage());
        }
        dm = new DataManager(mContext);
        dm.init_database();
    }

    public String fetchDiclosureDetail(String regNum){
        String fileName = "companyDIDetail.html";
        String url = "http://m.dart.fss.or.kr/html_mdart/MD1007.html?rcpNo="+regNum;
        File tempFile = DownLoadHtml(url, fileName);
        if (tempFile == null){
            return null;
        }

        return tempFile.getAbsolutePath();
    }

    public void fetchCL(String company_code){
        String url = "http://dart.fss.or.kr/api/search.xml?auth="+API_KEY+"&crp_cd="+company_code+"&start_dt=19990101&bsn_tp=A001&bsn_tp=A002&bsn_tp=A003";
        File xmlFile = DownLoadHtml(url, "ComponyInfo.xml");

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(xmlFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            dm.removeCompanyDI(null);
            parser.setInput(fis, null);
            ParseData();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File DownLoadHtml(String Addr, String tempFileName){
        StringBuilder html = new StringBuilder(2*1024);
        File file = null;
        FileWriter fw = null;
        try {
            file = File.createTempFile(tempFileName, null, mContext.getCacheDir());
            fw = new FileWriter(file);
            if (file.exists()){
                Log.i(TAG, file.getAbsolutePath() + " file is created.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            URL url = new URL(Addr);
            Log.i(TAG, "url is [" + url + "]");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            if (conn != null){
                conn.setConnectTimeout(10000);
                conn.setUseCaches(false);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    InputStreamReader inputStreamReader	= new InputStreamReader(conn.getInputStream());
                    BufferedReader br = new BufferedReader(inputStreamReader, 8*1024);

                    for (;;){
                        String line = br.readLine();
                        if (line == null)
                            break;
                        html.delete(0, html.length());
                        Log.i(TAG, line);
                        html.append(line+"\n");
                        fw.append(html);
                    }
                    br.close();
                    fw.close();
                }else {
                    InputStream is = conn.getErrorStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] byteBuffer = new byte[1024];
                    byte[] byteData = null;
                    int nLength = 0;
                    while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                        baos.write(byteBuffer, 0, nLength);
                    }
                    byteData = baos.toByteArray();
                    String response = new String(byteData);
                    Log.d(TAG, "response = " + response);
                }
                conn.disconnect();
            }
        }

        catch (Exception e){
            Log.d(TAG, "ERROR cause [" + e.getCause() + "], Message [" + e.getMessage() + "]");
            return null;
        }
        Log.i(TAG, "html [" + html.toString() + "]");

        return file;
    }

    public void ParseData() throws XmlPullParserException, IOException {
        final int STEP_NONE = 0;
        final int STEP_NO = 1;
        final int STEP_NAME = 2;
        final int STEP_RESULT = 3;
        final int STEP_ERRORCODE = 4;
        final int STEP_ERRORMSG = 5;
        final int STEP_PAGENO = 6;
        final int STEP_PAGESET = 7;
        final int STEP_TOTALCOUNT = 8;
        final int STEP_TOTALPAGE = 9;
        final int STEP_LIST = 10;
        final int STEP_CRPCLS = 11;
        final int STEP_CRPNM = 12;
        final int STEP_CRPCD = 13;
        final int STEP_RPTNM = 14;
        final int STEP_RCPNO = 15;
        final int STEP_FLRNM = 16;
        final int STEP_RCPDT = 17;
        final int STEP_RMK = 18;

        int step = STEP_NONE ;
        boolean invalidXML = false;
        int no = -1 ;
        int page_no = -1;
        int page_set = -1;
        int total_count = -1;
        int total_page = -1;
        String name = null ;
        int eventType = parser.getEventType() ;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
                // XML 데이터 시작
            } else if (eventType == XmlPullParser.START_TAG) {
                String startTag = parser.getName() ;
                if (startTag.equals("NO")) {
                    step = STEP_NO;
                } else if (startTag.equals("NAME")) {
                    step = STEP_NAME;
                } else if (startTag.equals("err_code")) {
                    step = STEP_ERRORCODE;
                } else if (startTag.equals("err_msg")) {
                    step = STEP_ERRORMSG;
                } else if (startTag.equals("page_no")) {
                    step = STEP_PAGENO;
                } else if (startTag.equals("page_set")) {
                    step = STEP_PAGESET;
                } else if (startTag.equals("total_count")) {
                    step = STEP_TOTALCOUNT;
                } else if (startTag.equals("total_page")) {
                    step = STEP_TOTALPAGE;
                } else if (startTag.equals("list")) {
                    step = STEP_LIST;
                } else if (startTag.equals("crp_cls")) {
                    step = STEP_CRPCLS;
                } else if (startTag.equals("crp_nm")) {
                    step = STEP_CRPNM;
                } else if (startTag.equals("crp_cd")) {
                    step = STEP_CRPCD;
                } else if (startTag.equals("rpt_nm")) {
                    step = STEP_RPTNM;
                } else if (startTag.equals("rcp_no")) {
                    step = STEP_RCPNO;
                } else if (startTag.equals("flr_nm")) {
                    step = STEP_FLRNM;
                } else if (startTag.equals("rcp_dt")) {
                    step = STEP_RCPDT;
                } else if (startTag.equals("rmk")) {
                    step = STEP_RMK;
                } else {
                    step = STEP_NONE;
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                String endTag = parser.getName() ;
                if ((endTag.equals("NO") && step != STEP_NO) ||
                        endTag.equals("NAME") && step != STEP_NAME)
                {
                    // TODO : error.
                } else if(endTag.equals("list")){
                    dm.InsertCompanyDI(crpRecord);
                    // add data to database
                }
                step = STEP_NONE ;
            } else if (eventType == XmlPullParser.TEXT) {
                String text = parser.getText() ;
                if (step == STEP_NO) {
                    no = Integer.parseInt(text) ;
                } else if (step == STEP_LIST) {

                } else if (step == STEP_ERRORCODE) {
                    if (text.equals("000") != true){
                        invalidXML = true;
                        // XML data is not valid!!
                    }
                } else if (step == STEP_ERRORMSG) {
                    //  show error message
                    if (invalidXML == true) {
                        Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
                        return;
                    }
                } else if (step == STEP_PAGENO) {
                    page_no  = Integer.parseInt(text);
                } else if (step == STEP_TOTALCOUNT) {
                    total_count = Integer.parseInt(text);
                } else if (step == STEP_TOTALPAGE) {
                    total_page = Integer.parseInt(text);
                } else if (step == STEP_CRPCLS) {
                    crpRecord.crp_cls = text;
                } else if (step == STEP_CRPNM) {
                    crpRecord.crp_nm = text;
                } else if (step == STEP_CRPCD) {
                    crpRecord.crp_cd = text;
                } else if (step == STEP_RPTNM) {
                    crpRecord.rpt_nm = text;
                } else if (step == STEP_RCPNO) {
                    crpRecord.rcp_no = text;
                } else if (step == STEP_FLRNM) {
                    crpRecord.flr_nm = text;
                } else if (step == STEP_RCPDT) {
                    crpRecord.rcp_dt = text;
                } else if (step == STEP_RMK) {
                    crpRecord.rmk = text;
                } else if (step == STEP_NAME) {
                    name = text ;
                }
            }
            eventType = parser.next();
        }

        if (no == -1 || name == null) {
            // ERROR : XML is invalid.
        }
    }
}
