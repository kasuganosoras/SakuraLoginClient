/*
 * Sakura Minecraft Login
 * 开源的 Minecraft 服务器外置登录解决方案
 * 官网：https://www.moemc.cn/
 */
package com.moemc.login.client;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client {

    public static int trynum = 0;
    public static String hostname, aeskey;
    public static int port;

    public static void main(String[] args) {
        if (args.length > 4) {
            try {
                hostname = args[0];
                port = Integer.parseInt(args[1]);
                aeskey = args[2];
                if(aeskey.length() != 16) {
                    Sora.log("Error: AES key length must be 16 chars.");
                    System.exit(1);
                }
                final long timeInterval = 2000;
                Runnable runnable = () -> {
                    while (true) {
                        try {
                            if (trynum >= 60) {
                                Sora.log("Login Cancelled.");
                                System.exit(1);
                            } else {
                                TestClient client = TestClientFactory.createClient();
                                client.send(AES.encrypt(aeskey, args[3] + "/" + args[4]));
                                client.receive();
                                Thread.sleep(timeInterval);
                                trynum++;
                            }
                        } catch (InterruptedException e) {
                            Sora.log(e);
                        } catch (Exception ex) {
                            Sora.log(ex);
                        }
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            } catch (NumberFormatException es) {
                Sora.log("Error: Port must be a number.");
                System.exit(1);
            }
        } else {
            Sora.log("Error: Need more arguments.");
            Sora.log("Format: java -jar SoraLoginClient.jar <hostname> <port> <aeskey> <username> <password>");
            System.exit(1);
        }
    }

    static class TestClientFactory {

        public static TestClient createClient() throws Exception {
            return new TestClient(hostname, port);
        }

    }

    static class TestClient {

        public TestClient(String host, int port) throws Exception {
            this.client = new Socket(host, port);
            Sora.log("Try to Login...");
        }

        private final Socket client;

        private Writer writer;

        public void send(String msg) throws Exception {
            if (writer == null) {
                writer = new OutputStreamWriter(client.getOutputStream(), "UTF-8");
            }
            writer.write(msg);
            writer.write("eof\n");
            writer.flush();
        }

        public void receive() throws Exception {
            try (Reader reader = new InputStreamReader(client.getInputStream(), "UTF-8")) {
                client.setSoTimeout(10 * 1000);
                char[] chars = new char[64];
                int len;
                StringBuilder sb = new StringBuilder();
                while ((len = reader.read(chars)) != -1) {
                    sb.append(new String(chars, 0, len));
                }
                if (sb.toString().equals("exit")) {
                    Sora.log("Login successful.");
                    System.exit(0);
                } else {
                    Sora.log("Server return: " + sb.toString());
                }
            }
            writer.close();
            client.close();
        }

    }

}

class Sora {

    public static void log(String str) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + sdf.format(d) + "] " + str);
    }

    public static void log(int str) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + sdf.format(d) + "] " + str);
    }

    public static void log(Exception str) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + sdf.format(d) + "] " + str);
    }

    public static void log(Object str) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + sdf.format(d) + "] " + str);
    }
}
