import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client1{
    private String idString = "client1";


    public void init() throws IOException {
        try {
            Socket socket = new Socket("192.168.68.5", 8888);
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
        new Client1().init();

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
                Scanner messages=new Scanner(System.in);//得到传输的messages
                while(true){
                    String s=messages.nextLine();

                    //定长数据包内传输字符串长度
                    int num_package = 1;
                    String len4_message;
                    String supplement = "";

                    if(s.length() <= 4)
                    {

                        for(int i = 0; i < (4 - s.length()); i++){
                            supplement = supplement.concat("0");
                        }
                        s = supplement.concat(s);

                    } else
                    {
                        num_package = s.length()/4;
                        if(s.length()%4 != 0) {
                            num_package = s.length() / 4 + 1;
                            for(int i = 0; i < (4 - s.length()%4); i++)
                            {
                                supplement = supplement.concat("0");
                            }
                            s = supplement.concat(s);
                        }

                    }

                    for(int i = 0; i < num_package; i++)
                    {
                        len4_message = s.substring(4*i,4*i+4);


                    }
                    out.write(s+"\n");
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}