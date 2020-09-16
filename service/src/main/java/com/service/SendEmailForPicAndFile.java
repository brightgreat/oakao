package com.service;


import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SendEmailForPicAndFile {

    private static String mailFrom = null;// 指明邮件的发件人
    private static String password_mailFrom = null;// 指明邮件的发件人登陆密码
    private static String mailTo = null; // 指明邮件的收件人
    private static String mailTittle = null;// 邮件的标题
    private static String mailText = null; // 邮件的文本内容
    private static String mail_host = null; // 邮件的服务器域名
    private static String photoSrc = null; // 发送图片的路径
    private static List<String> fileList = new ArrayList<String>(); // 发送附件的路径

    public static void main(String[] args) throws Exception {
        mailFrom = "1877143930@qq.com";
        password_mailFrom = "socfghvyxjeofcjh";
        mailTo = "3310579331@qq.com";
        mailTittle = "带附件和带图片的的邮件";
        mailText ="带附件和带图片的的邮件xxxxxx";
        mail_host = "smtp.qq.com";
        photoSrc = "./service/src/main/file/1.jpg";
        fileList.add(0,"./service/src/main/file/backup.txt");
        fileList.add(1,"./service/src/main/file/uAndp.txt");


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
        Message message = createMixedMail(session);
        // 5、发送邮件
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();
    }

    /**
     * @Method: createMixedMail
     * @Description: 生成一封带附件和带图片的邮件
     */
    public static MimeMessage createMixedMail(Session session) throws Exception {
        MimeMessage message = new MimeMessage(session);
        // 设置邮件的基本信息
        message.setFrom(new InternetAddress(mailFrom));	// 发件人
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));// 收件人
        message.setSubject(mailTittle);

        // 正文
        MimeBodyPart text = new MimeBodyPart();
        text.setContent(mailText+"<br/><img src='cid:1.jpg'>", "text/html;charset=UTF-8");

        // 图片
        MimeBodyPart image = new MimeBodyPart();
        image.setDataHandler(new DataHandler(new FileDataSource(photoSrc)));
        image.setContentID("1.jpg");

        // 附件1
        MimeBodyPart attach = new MimeBodyPart();
        DataHandler dh = new DataHandler(new FileDataSource(fileList.get(0)));
        attach.setDataHandler(dh);
        attach.setFileName(dh.getName());

        // 附件2
        MimeBodyPart attach2 = new MimeBodyPart();
        DataHandler dh2 = new DataHandler(new FileDataSource(fileList.get(1)));
        attach2.setDataHandler(dh2);
        attach2.setFileName(MimeUtility.encodeText(dh2.getName()));

        // 描述关系:正文和图片
        MimeMultipart mp1 = new MimeMultipart();
        mp1.addBodyPart(text);
        mp1.addBodyPart(image);
        mp1.setSubType("related");

        // 描述关系:正文和附件
        MimeMultipart mp2 = new MimeMultipart();
        mp2.addBodyPart(attach);
        mp2.addBodyPart(attach2);

        // 代表正文的bodypart
        MimeBodyPart content = new MimeBodyPart();
        content.setContent(mp1);
        mp2.addBodyPart(content);
        mp2.setSubType("mixed");

        message.setContent(mp2);
        message.saveChanges();

        message.writeTo(new FileOutputStream("C:\\Program Files\\TestMail\\ImageMail.eml"));
        // 返回创建好的的邮件
        return message;
    }


}
