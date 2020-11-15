package test.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static main.java.com.service.OaKaoq.callNingMeng;

public class testPub {
    public static void main(String[] args) throws Exception {
//        System.out.println(doGet("https://leaguehd.com/attendance.php"));
//        System.out.println(callNingMeng());

        Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DATE);
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int mi = cal.get(Calendar.MINUTE);
        int s = cal.get(Calendar.SECOND);
        //判断当前时间是否是周四
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Date date;
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = new Date();
            cal.setTime(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //一周的第几天
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        System.out.println("今天是" + weekDays[w]);
        boolean oo =!weekDays[w].equals("星期日") && !weekDays[w].equals("星期六");
        System.out.println(oo);
    }

    public static String doGet(String httpurl)throws Exception {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;// 返回结果字符串
        try {
            // 创建远程url连接对象
            URL url = new URL(httpurl);
            // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(60000);
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            connection.setRequestProperty("c_secure_uid", "MzQxMjQ%3D");
            connection.setRequestProperty("c_secure_pass", "c1fec63c3f96279e98469fef5bd33dd6");
            connection.setRequestProperty("c_secure_ssl", "eWVhaA%3D%3D");
            connection.setRequestProperty("c_secure_tracker_ssl", "eWVhaA%3D%3D");
            connection.setRequestProperty("c_secure_login", "bm9wZQ%3D%3D");
            connection.setRequestProperty("__cfduid", "d122666a97857d8a4c0ed78f6e2de39181599147650");
            connection.setRequestProperty("Cookie", "c_secure_uid=MzQxMjQ%3D; c_secure_pass=c1fec63c3f96279e98469fef5bd33dd6; c_secure_ssl=eWVhaA%3D%3D; c_secure_tracker_ssl=eWVhaA%3D%3D; c_secure_login=bm9wZQ%3D%3D; __cfduid=d122666a97857d8a4c0ed78f6e2de39181599147650");
            // 发送请求
            connection.connect();
            // 通过connection连接，获取输入流
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                // 封装输入流is，并指定字符集
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                // 存放数据
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            connection.disconnect();// 关闭远程连接
        }

        return result;
    }
}
