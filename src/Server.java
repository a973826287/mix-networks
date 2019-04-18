import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.*;

public class Server {

    private List<Socket> socketList=new ArrayList<Socket>();
    private Map<String ,Socket> socketMap = new HashMap();

    //初始化服务器
    public void init() throws IOException{
        try {
            ServerSocket server=new ServerSocket(8888);
            System.out.println("Server starts：");
            while(true){
                Socket socket=server.accept();
                socketList.add(socket);
                Thread recieve = new Thread(new ThreadServer(socket));
                recieve.start();
                }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("网络错误");
        }
    }

    //main
    public static void main(String[] args) throws IOException {
        new Server().init();
    }


    class ThreadServer implements Runnable{
        private Socket socket;
        public ThreadServer(Socket socket){
            this.socket=socket;
        }

        @Override
        public void run() {
            try {
                String socAddr=socket.getInetAddress().toString();
                String socPort=socket.getPort()+"";
                BufferedReader in= new BufferedReader(new InputStreamReader(
                        socket.getInputStream(),"utf-8"));
                System.out.println(socAddr + ":" + socPort + " is starting");
                String id = in.readLine();
                socketMap.put(id,socket);
                System.out.println(socketMap);
                while(true){
                    String s=in.readLine();
                    System.out.println("来自" + socAddr+":"+socPort+" ： "+s);
                    //指定发送给谁
                    if(s.endsWith("mix0"))
                    {

                        Socket socket2 = socketMap.get("mix0");
                        PrintWriter out =new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                        out.write(s+"\n");
                        out.flush();
                    }else if(s.endsWith("mix1"))
                    {
                        Socket socket2 = socketMap.get("mix1");
                        PrintWriter out =new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                        out.write(s+"\n");
                        out.flush();
                    }else if(s.endsWith("mix2"))
                    {
                        Socket socket2 = socketMap.get("mix2");
                        PrintWriter out =new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                        out.write(s+"\n");
                        out.flush();
                    }else if(s.endsWith("client0"))
                    {
                        Socket socket2 = socketMap.get("client0");
                        PrintWriter out =new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                        out.write(s+"\n");
                        out.flush();
                    }else if(s.endsWith("client1"))
                    {
                        Socket socket2 = socketMap.get("client1");
                        PrintWriter out =new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                        out.write(s+"\n");
                        out.flush();
                    }else if(s.endsWith("mix3"))
                    {
                        Socket socket2 = socketMap.get("mix3");
                        PrintWriter out =new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                        out.write(s+"\n");
                        out.flush();
                    }

                    //广播模式
                    /*
                    for(Socket socket2:socketList){
                        if(socket2==socket){
                            continue;
                        }
                        PrintWriter out =new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                        out.write(send+"\n");
                        out.flush();
                    }

                     */

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


