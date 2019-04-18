import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

//模拟恶意节点
public class mix3 {
    private String idString = "mix3";

    public void init() throws IOException {
        try {
            Socket socket = new Socket("127.0.0.1", 8888);
            char secret = '1';
            new Thread(new mix3.RecieverThread(socket)).start();
            new Thread(new mix3.SendMessage(socket)).start();
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
        new mix3().init();
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
                    s = s.substring(0,s.length()-4);
                    s = EncryptUncrypt.encryptAndUncrypt(s,'1');
                    System.out.println(s);
                    PrintWriter out =new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                            socket.getOutputStream(),"utf-8")));
                    out.write(s+"\n");
                    out.flush();

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
                out.write(idString+"\n");
                out.flush();
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
