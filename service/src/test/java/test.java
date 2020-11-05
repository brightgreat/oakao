package test.java;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static main.java.com.service.OaKaoq.*;

public class test {
    public static void main(String[] args) throws Exception {
//        System.out.println(readFileContent("./service/src/main/file/uAndp.txt"));
//
//        String users = readFileContent("./service/src/main/file/uAndp.txt");
//        call(users);
        String loginUrl = "http://oa.ittx.com.cn/api/hrm/login/checkLogin"; //?loginid=hlwang@ittx.com.cn&userpassword=ec53c91144781b29d864d6a16a13e506_random_
        String sessionId = callHttp("http://oa.ittx.com.cn/api/hrm/login/checkLogin?loginid=hlwang@ittx.com.cn&userpassword=Ttx1234", "", true);
        callHttpDk("http://oa.ittx.com.cn/api/hrm/kq/attendanceButton/punchButton?address=上海市普陀区东方国际元中大厦A栋&longitude=121.418124&latitude=31.240195&locationshowaddress=1&locationid=25",sessionId,false);
    }

    public static String callHttpDk(String callURL, String sessionId, boolean isLogin) throws Exception {
        String result = "";
//        String sessionId = "";
        URL u0 = new URL(callURL);
        HttpURLConnection conn = (HttpURLConnection) u0.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setRequestProperty("Content-Language", "en-US");
        conn.setRequestProperty("address", "上海市普陀区东方国际元中大厦A栋");
        conn.setRequestProperty("longitude", "121.418124");
        conn.setRequestProperty("latitude", "31.240195");
        conn.setRequestProperty("locationshowaddress", "1");
        conn.setRequestProperty("locationid", "25");

        if (!isLogin) {
            conn.setRequestProperty("Cookie", sessionId);
        }
//        conn.setRequestProperty("Cookie","experimentation_subject_id=IjYwOGFkZjM5LTNlZDQtNGY5MS05MzAxLWI1MDdmNjc0ZjJmNiI%3D--01211de8bf1d54bde5c7c903f8490fcb79331245; languageidweaver=7; loginuuids=324; __randcode__=18e77b64-c48e-4320-b053-dd1eb283f33c; loginidweaver=hlwang%40ittx.com.cn; ecology_JSessionid=aaafL2O-cn4sH4rKC9Pjx; JSESSIONID=aaafL2O-cn4sH4rKC9Pjx; ecology_JSessionId=aaafL2O-cn4sH4rKC9Pjx");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        // 捕获sessionId
        String key = null;
        for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; i++) {
            if (key.equalsIgnoreCase("set-cookie")) {
                sessionId = conn.getHeaderField(key);
                sessionId = sessionId.substring(0, sessionId.indexOf(";"));
                break;
            }
        }

//        if (SESSION_ID != null && !"".equals(SESSION_ID)) {
//            // 已经记录sessionId则放入session中
//            conn.setRequestProperty("Cookie", SESSION_ID);
//        } else if ((SESSION_ID == null || "".equals(SESSION_ID)) && conn != null) {
//            // 捕获sessionId
//            String key = null;
//            for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; i++) {
//                if (key.equalsIgnoreCase("set-cookie")) {
//                    SESSION_ID = conn.getHeaderField(key);
//                    SESSION_ID = SESSION_ID.substring(0, SESSION_ID.indexOf(";"));
//                    break;
//                }
//            }
//        }
        // 自动捕获网页编码，并按其编码方式读取网页内容
        String charset = getChareset(conn.getContentType());
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        result = buffer.toString();

        conn.disconnect();
        System.out.println(result);
//        return result;
        //如果登录成功，这里就把session返回出去
        if (isLogin && result.toLowerCase().contains("成功")) {
            return sessionId;
        } else {
            return result;
        }

    }
}
