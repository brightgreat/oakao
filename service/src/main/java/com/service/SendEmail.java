package com.service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SendEmail {
    private static String mailFrom = null;// 指明邮件的发件人
    private static String password_mailFrom = null;// 指明邮件的发件人登陆密码
    private static String mailTo = null;	// 指明邮件的收件人
    private static String mailTittle = null;// 邮件的标题
    private static String mailText =null;	// 邮件的文本内容
    private static String mail_host =null;	// 邮件的服务器域名
    public static void main(String[] args) throws Exception{



        mailFrom = "1877143930@qq.com";
        password_mailFrom="ivwclrzswndkdeeg";
        mailTo = "3310579331@qq.com";
        mailTittle="===重要通知===";
        mailText = "由于公司业务调整，本程序，将于今天下线，后面请大家自己进行手机端定位DK操作，后续启用时间，另行通知；支持正版，人人有责";
        mail_host="smtp.qq.com";

        Properties prop = new Properties();
        prop.setProperty("mail.host", mail_host);
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");


        //设定收件人
        List<String> address=new ArrayList<String>();
        address.add("2095756992@qq.com");
        address.add("1120964530@qq.com");
        address.add("277090648@qq.com");
        address.add("1018765067@qq.com");
        address.add("791618324@qq.com");
        address.add("1197980130@qq.com");
        address.add("1090693660@qq.com");
        address.add("1752579954@qq.com");
        address.add("3310579331@qq.com");
        final int num = address.size();
        InternetAddress to_address[] = new InternetAddress[num];
        for(int i=0; i<num; i++){
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
        ts.connect(mail_host,mailFrom, password_mailFrom);
        // 4、创建邮件
        Message message = createSimpleMail(session,mailFrom,mailTo,mailTittle,mailText,to_address);
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

    public static void send() {

        // 收件人电子邮箱
        String to = "jnwu@ittx.com.cn";

        // 发件人电子邮箱
        String from = "hlwang@ittx.com.cn";

        // 指定发送邮件的主机为 localhost
        String host = "smtp.mxhichina.com";

        // 获取系统属性
        Properties properties = System.getProperties();

        // 设置邮件服务器
        properties.setProperty("mail.smtp.host", host);
        properties.put("mail.smtp.auth", "true");
        properties.setProperty("mail.user", "hlwang@ittx.com.cn");
        properties.setProperty("mail.password", "Wl3.1415926");

        // 获取默认session对象
        Session session = Session.getDefaultInstance(properties);

        try {
            // 创建默认的 MimeMessage 对象
            MimeMessage message = new MimeMessage(session);

            // Set From: 头部头字段
            message.setFrom(new InternetAddress(from));

            // Set To: 头部头字段
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(to));

            // Set Subject: 头部头字段
            message.setSubject("This is the Subject Line!");

            // 设置消息体
            message.setText("This is actual message");

            // 发送消息
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
