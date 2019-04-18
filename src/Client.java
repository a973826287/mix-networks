import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;


public class Client{
    private String idString = "client0";
    private char secret[] = {'7','8','9','5','6','1'};//一次是mix0,mix1,mix2,client0,client1
    public  Vector path = new Vector();
    public  String idStrings[] = {"mix0", "mix1", "mix2", "client0", "client1","mix3"};


    public void init() throws IOException { 
        try { 
            Socket socket = new Socket("127.0.0.1", 8888);
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
        new Client().init();

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
                s = s.substring(0,s.length()-7);
                s = EncryptUncrypt.encryptAndUncrypt(s,'5');
                if(s.startsWith("000"))
                {
                    s = s.substring(3);
                }
                else if(s.startsWith("00"))
                {
                    s = s.substring(2);
                } else if (s.startsWith("0"))
                {
                    s = s.substring(1);
                }
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
                out.write(idString+"\n");
                out.flush();
                Scanner messages=new Scanner(System.in);//得到传输的messages
                System.out.println("请输入传输路线经过的节点数：");
                int n = messages.nextInt();
                System.out.println("请输入path(0,1,2,3,4,5)" +
                        "--分别表示mix0,mix1,mix2,client0,client1,mix3 ：");
                for(int i = 0; i < n; i++){
                    path.add(messages.nextInt());
                }
                System.out.println("请输入发送的信息：");
                while(true){
                    String s=messages.nextLine();
                    //定长数据包内传输字符串长度
                    int num_package = 0;
                    String len4_message;
                    String supplement = "";
                    if(s.isEmpty())
                        continue;

                    if(s.length() <= 4)
                    {
                        num_package = 1;
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
                        for(int j = n;j > 0;j--) {
                            len4_message = EncryptUncrypt.encryptAndUncrypt(
                                    len4_message, secret[(int) path.get(j-1)])
                                    + idStrings[(int)path.get(j-1)];
                        }
                        out.write(len4_message+"\n");
                        out.flush();
                    }

                    }    
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
