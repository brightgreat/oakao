package com.service;


import java.io.FileOutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


public class SendEmailForPic {
    private static String mailFrom = null;// 指明邮件的发件人
    private static String password_mailFrom = null;// 指明邮件的发件人登陆密码

    private static String mailTo = null; // 指明邮件的收件人

    private static String mailTittle = null;// 邮件的标题

    private static String mailText = null; // 邮件的文本内容

    private static String mail_host = null; // 邮件的服务器域名

    private static String photoSrc = null; // 发送图片的路径

    public static void main(String[] args) throws Exception {
        mailFrom = "1877143930@qq.com";
        password_mailFrom = "socfghvyxjeofcjh";
        mailTo = "3310579331@qq.com";
        mailTittle = "带图片的邮件";
        mailText = "这是一封邮件正文带图片的邮件";
        mail_host = "smtp.qq.com";
        photoSrc = "./service/src/main/file/1.jpg";

        Properties prop = new Properties();
        prop.setProperty("mail.host", mail_host);// 需要修改
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");
        // 使用JavaMail发送邮件的5个步骤
        // 1、创建session
        Session session = Session.getInstance(prop);
        // 开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
        session.setDebug(true);
        // 2、通过session得到transport对象
        Transport ts = session.getTransport();
        // 3、连上邮件服务器，需要发件人提供邮箱的用户名和密码进行验证
        ts.connect(mail_host, mailFrom, password_mailFrom);// 需要修改
        // 4、创建邮件
        Message message = createImageMail(session);
        // 5、发送邮件
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();
    }

    /**
     * @param session
     * @return
     * @throws Exception
     * @Method: createImageMail
     * @Description: 生成一封邮件正文带图片的邮件
     */
    public static MimeMessage createImageMail(Session session) throws Exception {
        // 创建邮件
        MimeMessage message = new MimeMessage(session);
        // 设置邮件的基本信息

        message.setFrom(new InternetAddress(mailFrom)); // 发件人

        message.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo)); // 收件人
        // 邮件标题
        message.setSubject(mailTittle);

        // 准备邮件数据
        // 准备邮件正文数据
        MimeBodyPart text = new MimeBodyPart();
        text.setContent(mailText + "<br/><img src='cid:1.jpg' width=400px height=400px><br/><img src='cid:1.jpg' width=500px height=300px>", "text/html;charset=UTF-8");// 需要修改
        // 准备图片数据
        MimeBodyPart image = new MimeBodyPart();
        DataHandler dh = new DataHandler(new FileDataSource(photoSrc));// 需要修改
        image.setDataHandler(dh);
        image.setContentID("1.jpg");// 需要修改
        // 描述数据关系
        MimeMultipart mm = new MimeMultipart();
        mm.addBodyPart(text);
        mm.addBodyPart(image);
        mm.setSubType("related");

        message.setContent(mm);
        message.saveChanges();
        // 将创建好的邮件写入到F盘以文件的形式进行保存
        message.writeTo(new FileOutputStream("C:\\Program Files\\TestMail\\ImageMail.eml"));// 需要修改
        // 返回创建好的邮件
        return message;
    }


}
