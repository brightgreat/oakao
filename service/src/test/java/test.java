package test.java;

import static main.java.com.service.OaKaoq.call;
import static main.java.com.service.OaKaoq.readFileContent;

public class test {
    public static void main(String[] args) throws Exception {
        System.out.println(readFileContent("./service/src/main/file/uAndp.txt"));

        String users = readFileContent("./service/src/main/file/uAndp.txt");
        call(users);
    }
}
