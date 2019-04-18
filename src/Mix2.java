import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Mix2 {
    String idString = "mix2";

        public void init() throws IOException {
            try {
                Socket socket = new Socket("192.168.68.5", 8888);
                char secret = '9';

                new Thread(new RecieverThread(socket)).start();
                new Thread(new SendMessage(socket)).start();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.out.println("链接错误");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("流错误");
            } finally {

            }
        }

        public static void main(String[] args) throws Exception {
            new Mix2().init();
        }

        class RecieverThread implements Runnable{
            private Socket socket;
            public RecieverThread(Socket socket){
                this.socket=socket;
            }
            @Override
            public void run() {
                try {
                    while(true){
                        BufferedReader in= new BufferedReader(new InputStreamReader(
                                socket.getInputStream(),"utf-8"));
                        String s=in.readLine();
                        System.out.println(s);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

        class SendMessage implements Runnable{
            private Socket socket;
            public SendMessage(Socket socket){
                this.socket=socket;
            }
            @Override
            public void run() {
                try {
                    PrintWriter out =new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                            socket.getOutputStream(),"utf-8")));
                    Scanner sc=new Scanner(System.in);

                    while(true){
                        String s=sc.nextLine();
                        out.write(s+"\n");
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
}
