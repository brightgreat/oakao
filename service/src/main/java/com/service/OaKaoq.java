package main.java.com.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;


import com.alibaba.fastjson.*;
import sun.misc.BASE64Decoder;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class OaKaoq {
    private static String SESSION_ID = "";
    private static long timeInterval = 1000;
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat dh = new SimpleDateFormat("HH");

    public static void main(String[] args) throws Exception {

        long sleep = 1200;// millils  每20分钟触发一次
        long lopnum = 0;
        int hour = 9; //默认值9 表示9点打卡
        Map<String, String> stringToMap = null;
        String failResu;
        String ningMengResult = null;
        String newD = "";
        System.out.println("我启动了，开始工作:" + df.format(new Date()));
        System.out.println("version：1.3");
        try {
            while (true) {
                //无限循环，采用进程阻塞方式，达到计划定时处理，low
                lopnum += 1;
                System.out.println("我进入了循环这是第" + lopnum + "次:" + df.format(new Date()));

                //进程睡眠
                Thread.sleep(sleep * timeInterval);
                //获取当前时分秒
                Calendar cal = Calendar.getInstance();
                int y = cal.get(Calendar.YEAR);
                int m = cal.get(Calendar.MONTH);
                int d = cal.get(Calendar.DATE);
                int h = cal.get(Calendar.HOUR_OF_DAY);
                int mi = cal.get(Calendar.MINUTE);
                int s = cal.get(Calendar.SECOND);
                //todo 打卡想办法只在工作日打卡，可以在文件上记录法定节假日，排除法定节假日的打卡日期
                //如果启动时间是在9点之后，那么9点就不需要打卡了，把打卡时间改到19点
                if (h > 9 && h < 19) {
                    hour = 19;
                }
                System.out.println("预定时间：" + hour + ",睡眠时间：" + sleep);
                System.out.println("时间到了:" + df.format(new Date()) + "，我即将对比时间是否满足打卡,预定时间hour：" + hour + "h:" + h);

                //只有当时间等于9点和19点的时候，才去触发打卡，其他时间不进行打卡
                if (h == hour) {
                    String initDate = df.format(new Date());
                    System.out.println("打卡时间到，开始打卡：" + initDate);

                    Map<String, String> resule = call();
                    //表示到点进入了打卡程序，不需要发邮件了
                    if (!resule.toString().toLowerCase().contains("成功")) {
                        //                    如果失败了 得到失败的那一个用户 就5分钟后再去打卡一次
                        for (int i = 0; i < 3; i++) {
                            Thread.sleep(300 * timeInterval);
                            Map<String, String> failResuss = failCall(resule);
                            if (failResuss.toString().toLowerCase().contains("成功")) {
                                System.out.println("补卡成功：" + df.format(new Date()));
                                //如果打卡成功,结束这个循环
                                break;
                            } else {
                                //打卡依然失败，那么就把新反馈出来的失败账号，重新轮询打卡
                                resule = failResuss;
                            }
                        }
                        //这里可以写一个lop进行指定循环次数，间隔时间的打卡
                    } else {
                        System.out.println("全部打卡成功，打卡时间是" + df.format(new Date()));
                        if (hour == 9) {
                            //柠檬打卡，每天打一次
                            ningMengResult = callNingMeng();
                            System.out.println("柠檬打卡结束，打卡时间是" + df.format(new Date()));
                            //如果全部打卡成功，并且是早上9点打卡，那么这里将变量更改为19点
                            hour = 19;
                            //全部打卡成功之后，给sleep时间增加100s,以便达到打卡时间随机
                            if (sleep > 2000) { //如果定时时间已经达到40分钟以上这里重置为20分钟
                                sleep = 1200;
                            } else {
                                //给定时睡眠时间随机加一个100之间得随机数
                                sleep += (int) (100 * Math.random() + 1);
                            }
                            System.out.println("全部打卡成功，现在的sleep时间为：" + sleep);
                        } else {
                            hour = 9;
                        }
                        //计算下次打卡时间
                        newD = timePastTenSecond(initDate, (int) sleep, hour);
                        System.out.println("下次打K时间为：" + newD);
                    }
                    //发送邮件，告诉打卡结果
                    SendEmail("打卡成功提醒", "进入时间为：”“" + initDate + ";" + readFileContent("./service/src/main/file/uAndp.txt") + ";全部打卡成功，现在的sleep时间为：" + sleep + ",下次进入时间为：" + newD + "/r/n 柠檬Result:" + ningMengResult);

                } else {
                    System.out.println("未到打卡时间：" + df.format(new Date()));
                }
                //判断当前时间是否是周四晚上8点，给我发邮件，提醒我进行DNF签到
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
                //如果是周四 发送签到邮件
                if (weekDays[w].equals("星期四") && h == 20) {
                    SendEmailForDNF();
                }
            }
        } catch (
                Exception ex) {
            SendEmail("打卡失败，出现异常", "进入时间为：”“" + df.format(new Date()) + ";" + ex.toString());
        }

    }


    /**
     * 发送邮件
     *
     * @throws Exception
     */
    public static void SendEmailForDNF() throws Exception {
        String mailFrom = "1877143930@qq.com";
        String password_mailFrom = "ivwclrzswndkdeeg";
        String mailTo = "3310579331@qq.com";
        String mailTittle = "周四DNF打卡";
        String mailText = "今天周四，记得进行DNF的打卡哟";
        String mail_host = "smtp.qq.com";

        Properties prop = new Properties();
        prop.setProperty("mail.host", mail_host);
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");

        //设定收件人
        List<String> address = new ArrayList<String>();
        address.add("3310579331@qq.com");
//        address.add("504354686@qq.com");
//        address.add("1185265991@qq.com");
        final int num = address.size();
        InternetAddress to_address[] = new InternetAddress[num];
        for (int i = 0; i < num; i++) {
            to_address[i] = new InternetAddress(address.get(i));
        }

        // 使用JavaMail发送邮件的5个步骤

        // 1、创建session
        Session session = Session.getInstance(prop);
        // 开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
        session.setDebug(false);
        // 2、通过session得到transport对象
        Transport ts = session.getTransport();
        // 3、使用邮箱的用户名和密码连上邮件服务器，发送邮件时，发件人需要提交邮箱的用户名和密码给smtp服务器，用户名和密码都通过验证之后才能够正常发送邮件给收件人。
        ts.connect(mail_host, mailFrom, password_mailFrom);
        // 4、创建邮件
        Message message = createSimpleMail(session, mailFrom, mailTo, mailTittle, mailText, to_address);
        // 5、发送邮件
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();

    }

    /**
     * 自动读取用户密码文本的打卡程序
     *
     * @return
     * @throws Exception
     */
    public static Map<String, String> call() throws Exception {
        Map<String, String> resultss = new HashMap<String, String>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //通过登录链接得到一个cookie
        String loginUrl = "http://oa.ittx.com.cn/api/hrm/login/checkLogin"; //?loginid=hlwang@ittx.com.cn&userpassword=ec53c91144781b29d864d6a16a13e506_random_
        String dakaUrl = "http://oa.ittx.com.cn/api/hrm/kq/attendanceButton/punchButton";

        System.out.println(readFileContent("./service/src/main/file/uAndp.txt"));

        String users = readFileContent("./service/src/main/file/uAndp.txt");
        if (null != users) {
            Map<String, String> stringToMap = JSONObject.parseObject(users, Map.class);
            stringToMap.size();
            int successNum = 0;
            Map<String, String> failUser = new HashMap<String, String>();
            Map<String, String> results = new HashMap<String, String>();
            for (Map.Entry<String, String> entry : stringToMap.entrySet()) {
                String user = entry.getKey();
                String pwd = decord(entry.getValue());
                String sessionId = callHttp(loginUrl + "?loginid=" + user + "&userpassword=" + pwd, "", true);
                //接收结果
                String result = callHttp(dakaUrl, sessionId, false);
                if (result.toLowerCase().contains("成功")) {
                    successNum += 1;
                    System.out.println("\r\n===========" + user + "=打卡成功======“”" + df.format(new Date()) + "==============\r\n");
                } else {
                    failUser.put(user, entry.getValue());
                    results.put(user, "pwd：" + entry.getValue() + ",loginResult:" + sessionId + ",callResult:" + result);
                    System.out.println("\r\n===========" + user + "=打卡失败======“”" + df.format(new Date()) + "==============\r\n");
                }
                // 每打一个，随机增加一个进程阻塞的时间，尽量保证每个人打卡的时间不一致
                int sleep = (int) (60 * Math.random() + 1);
                System.out.println("下一个用户的打卡时间在：" + sleep + "秒之后");
                Thread.sleep(sleep * timeInterval);
            }
            if (successNum == stringToMap.size()) {
                System.out.println("全部成功共:" + successNum + "位");
                resultss.put("全部成功", "全部成功");
                return resultss;
            } else {
                //失败邮件提醒
                SendEmail("打卡失败提醒", "以下用户打卡失败，请处理" + results.toString() + "这是第一次打卡，5分钟后会进行补打");
                return failUser;
            }
        }
        resultss.put("成功", "成功");
        return resultss;
    }

    /**
     * 自动读取用户密码文本的打卡程序
     *
     * @return
     * @throws Exception
     */
    public static String callNingMeng() throws Exception {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //通过登录链接得到一个cookie
        String dakaUrl = "https://leaguehd.com/attendance.php";
        String sessId = "c_secure_uid=MzQxMjQ%3D; c_secure_pass=c1fec63c3f96279e98469fef5bd33dd6; c_secure_ssl=eWVhaA%3D%3D; c_secure_tracker_ssl=eWVhaA%3D%3D; c_secure_login=bm9wZQ%3D%3D; __cfduid=d122666a97857d8a4c0ed78f6e2de39181599147650";
        return callHttpNingMeng(dakaUrl, sessId);
    }

    /**
     * 这里是打卡失败后的再次重试打卡
     *
     * @return
     * @throws Exception
     */
    public static Map<String, String> failCall(Map<String, String> failUsers) throws Exception {
        //通过登录链接得到一个cookie
        String loginUrl = "http://oa.ittx.com.cn/api/hrm/login/checkLogin"; //?loginid=hlwang@ittx.com.cn&userpassword=ec53c91144781b29d864d6a16a13e506_random_
        String dakaUrl = "http://oa.ittx.com.cn/api/hrm/kq/attendanceButton/punchButton";
        Map<String, String> resultss = new HashMap<String, String>();
        int successNum = 0;
        Map<String, String> failUser = new HashMap<String, String>();
        Map<String, String> results = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : failUsers.entrySet()) {
            String user = entry.getKey();
            String pwd = decord(entry.getValue());
            String sessionId = callHttp(loginUrl + "?loginid=" + user + "&userpassword=" + pwd, "", true);
            //接收结果
            String result = callHttp(dakaUrl, sessionId, false);
            if (result.toLowerCase().contains("成功")) {
                successNum += 1;
                System.out.println("\r\n===========" + user + "=补卡成功======“”" + df.format(new Date()) + "==============\r\n");
            } else {
                failUser.put(user, entry.getValue());
                results.put(user, "pwd：" + entry.getValue() + ",loginResult:" + sessionId + ",callResult:" + result);
                System.out.println("\r\n===========" + user + "=补卡失败======“”" + df.format(new Date()) + "==============\r\n");
            }

            System.out.println("\r\n================================\r\n");

        }
        if (successNum == failUsers.size()) {
            System.out.println("全部补卡成功共" + successNum + "位");
            resultss.put("全部成功", "全部成功");
            return resultss;
        } else {
            //失败邮件提醒
            SendEmail("打卡失败提醒", "以下用户补打卡失败，请处理：“”" + results.toString() + "这是补打卡");
            return failUser;
        }
    }

    /**
     * Base64解密，避免密文明文传输
     *
     * @param data
     * @return
     */
    public static String decord(String data) {
        try {
            // BASE64加密
//            BASE64Encoder encoder = new BASE64Encoder();
//            String data = encoder.encode(DATA.getBytes());
//            System.out.println("BASE64加密：" + data);

            // BASE64解密
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bytes = decoder.decodeBuffer(data);
//            System.out.println("BASE64解密：" + new String(bytes));
            return new String(bytes);
            // 结果
            // BASE64加密：Y29tLmJhc2U2NC5kZW1v
            // BASE64解密：com.base64.demo
        } catch (Exception e) {
            System.out.println("BASE64加解密异常");
            e.printStackTrace();
            return e.toString();
        }

    }

    /**
     * 发送邮件
     *
     * @throws Exception
     */
    public static void SendEmail(String mailTittle, String mailText) throws Exception {
        String mailFrom = "1877143930@qq.com";
        String password_mailFrom = "ivwclrzswndkdeeg";
        String mailTo = "3310579331@qq.com";
//        String mailTittle = "打卡失败提醒";
//        String mailText = firest ? "以下用户打卡失败，请处理" + user + "这是第一次打卡，5分钟后会进行补打" : "以下用户补打卡失败，请处理：" + user + "这是补打卡";
        String mail_host = "smtp.qq.com";

        Properties prop = new Properties();
        prop.setProperty("mail.host", mail_host);
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");

        //设定收件人
        List<String> address = new ArrayList<String>();
        address.add("3310579331@qq.com");
//        address.add("504354686@qq.com");
//        address.add("1185265991@qq.com");
        final int num = address.size();
        InternetAddress to_address[] = new InternetAddress[num];
        for (int i = 0; i < num; i++) {
            to_address[i] = new InternetAddress(address.get(i));
        }

        // 使用JavaMail发送邮件的5个步骤

        // 1、创建session
        Session session = Session.getInstance(prop);
        // 开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
        session.setDebug(false);
        // 2、通过session得到transport对象
        Transport ts = session.getTransport();
        // 3、使用邮箱的用户名和密码连上邮件服务器，发送邮件时，发件人需要提交邮箱的用户名和密码给smtp服务器，用户名和密码都通过验证之后才能够正常发送邮件给收件人。
        ts.connect(mail_host, mailFrom, password_mailFrom);
        // 4、创建邮件
        Message message = createSimpleMail(session, mailFrom, mailTo, mailTittle, mailText, to_address);
        // 5、发送邮件
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();

    }

    /**
     * @Method: createSimpleMail
     * @Description: 创建一封只包含文本的邮件
     */
    public static MimeMessage createSimpleMail(Session session, String mailfrom, String mailTo, String mailTittle,
                                               String mailText, InternetAddress to_address[]) throws Exception {
        // 创建邮件对象
        MimeMessage message = new MimeMessage(session);
        // 指明邮件的发件人
        message.setFrom(new InternetAddress(mailfrom));
        // 指明邮件的收件人，现在发件人和收件人是一样的，那就是自己给自己发

        //设定单人收件
//        message.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
        //设定多人收件
        message.setRecipients(Message.RecipientType.TO, to_address);
        // 邮件的标题
        message.setSubject(mailTittle);
        // 邮件的文本内容
        message.setContent(mailText, "text/html;charset=UTF-8");
        // 返回创建好的邮件对象
        return message;
    }
    //todo
    // 密文进行对称加密
    //账密信息读取配置文件，每次定时器跑到时，去做文件读取
    //邮件
    //计时器 或者使用cottrl
    //scheduler https://www.cnblogs.com/wang-yaz/p/8919411.html
    //输出本行内容及字符数 读取中文出现乱码

    static void readLineVarFile(String fileName, int lineNumber) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName))); //使用缓冲区的方法将数据读入到缓冲区中
        String line = reader.readLine(); //定义行数
        if (lineNumber <= 0 || lineNumber > getTotalLines(fileName)) //确定输入的行数是否有内容
        {
            System.out.println("不在文件的行数范围之内。");
        }
        int num = 0;
        while (line != null) //当行数不为空时，输出该行内容及字符数
        {
            if (lineNumber == ++num) {
                System.out.println("第" + lineNumber + "行: " + line);
            }
            line = reader.readLine();
        }
        reader.close();
    }

    // 文件内容的总行数
    static int getTotalLines(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName))); ////使用缓冲区的方法将数据读入到缓冲区中
        LineNumberReader reader = new LineNumberReader(br);
        String s = reader.readLine(); //定义行数
        int lines = 0;
        while (s != null) //确定行数
        {
            lines++;
            s = reader.readLine();
        }
        reader.close();
        br.close();
        return lines; //返回行数
    }

    public static String readFileContent(String fileName) {
        String fileMode = null;

        try {
            File file = new File(fileName);
            if (file.isFile() && file.exists()) {
//读取的时指定GBK编码格式，若中文出现乱码请尝试utf-8，window默认编码格式为GBK
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "gbk"));
                String lineTxt = null;
                while ((lineTxt = br.readLine()) != null) {
//                    System.out.println(lineTxt);
                    fileMode = lineTxt;
                }
                br.close();
                return fileMode;
            } else {
                System.out.println("文件不存在!");
                return "文件不存在";
            }
        } catch (Exception e) {
            System.out.println("文件读取错误!");
            return e.toString();
        }

    }

    public static String callHttp(String callURL, String sessionId, boolean isLogin) throws Exception {
        String result = "";
//        String sessionId = "";
        URL u0 = new URL(callURL);
        HttpURLConnection conn = (HttpURLConnection) u0.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setRequestProperty("Content-Language", "en-US");
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

    public static String callHttpNingMeng(String httpurl, String sessId) throws Exception {
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
            connection.setRequestProperty("Cookie", sessId);
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

    /**
     * 获取网页编码方式
     *
     * @param contentType
     * @return
     */
    public static String getChareset(String contentType) {
        int i = contentType == null ? -1 : contentType.indexOf("charset=");
        return i == -1 ? "UTF-8" : contentType.substring(i + 8);
    }


    /**
     * 给时间增减指定的秒数
     *
     * @param otime
     * @param second
     * @return
     */
    public static String timePastTenSecond(String otime, int second, int hour) {
        try {
            String HH;
            String retval;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (true) {
                Date dt = sdf.parse(otime);
                Calendar newTime = Calendar.getInstance();
                newTime.setTime(dt);
                newTime.add(Calendar.SECOND, second);//日期加10秒

                Date dt1 = newTime.getTime();
                retval = sdf.format(dt1);
                HH = retval.substring(11, 13);
                otime = retval;
                System.out.println("增加sleep：“”" + second + "后的H为：" + retval);
                if (hour == 19) {
                    //hour =19 说明是上午打卡
                    if ("19".equals(HH)) {
                        break;
                    }
                } else {
                    //如果是下午打卡
                    if ("09".equals(HH)) {
                        break;
                    }
                }
            }
            return retval;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
