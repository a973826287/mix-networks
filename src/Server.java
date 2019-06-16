
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Server {

    public  String idStrings[] = {"mix0", "mix1", "mix2", "cli0", "cli1","mix3" ,
            "mix4", "mix5", "mix6","mix7"};//3,7是恶意的

    //一次是mix0,mix1,mix2,client0,client1,mix3,mix4,mix5,mix6,mix7
    private int secret[] = {7, -8, 9, -5, 6, -1, -4, 5, -6, 2};
    private Map<String ,Socket> socketMap = new HashMap();
    private Map<String, Double> probablyMap = new HashMap();

    private JFrame frame;
    private JTextArea contentArea;
    private JTextField txt_message;
    private JTextField txt_port;
    private JButton btn_start;
    private JButton btn_stop;
    private JButton btn_send;
    private JPanel northPanel;
    private JPanel southPanel;
    private JScrollPane rightPanel;
    private JScrollPane leftPanel;
    private JSplitPane centerSplit;
    private JList userList;
    private DefaultListModel listModel;

    private ServerSocket serverSocket;
    private ServerThread serverThread;
    private ArrayList<ClientThread> clients;

    private boolean isStart = false;

    // 主方法,程序执行入口
    public static void main(String[] args) {
        new Server();
    }

    // 执行消息发送
    public void send() {

        String message = txt_message.getText();
        sendServerMessage(message);// 群发服务器消息
        contentArea.append("服务器说：" + txt_message.getText() + "\r\n");
        txt_message.setText(null);
    }

    // 构造放法
    public Server() {
        frame = new JFrame("服务器");
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        txt_message = new JTextField();
        txt_port = new JTextField("8080");
        btn_start = new JButton("启动");
        btn_stop = new JButton("停止");
        btn_send = new JButton("发送");
        btn_stop.setEnabled(false);
        listModel = new DefaultListModel();
        userList = new JList(listModel);

        southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new TitledBorder("消息编写区"));
        southPanel.add(txt_message, "Center");
        southPanel.add(btn_send, "East");
        leftPanel = new JScrollPane(userList);
        leftPanel.setBorder(new TitledBorder("节点群"));

        rightPanel = new JScrollPane(contentArea);
        rightPanel.setBorder(new TitledBorder("消息显示区"));

        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,
                rightPanel);
        centerSplit.setDividerLocation(100);
        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 6));

        northPanel.add(btn_start);
        northPanel.add(btn_stop);
        northPanel.setBorder(new TitledBorder("服务器启动/关闭区"));

        frame.setLayout(new BorderLayout());
        frame.add(northPanel, "North");
        frame.add(centerSplit, "Center");
        frame.add(southPanel, "South");
        frame.setSize(600, 400);
        //frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());//设置全屏
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setLocation((screen_width - frame.getWidth()) / 2,
                (screen_height - frame.getHeight()) / 2);
        frame.setVisible(true);

        // 关闭窗口时事件
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isStart) {
                    closeServer();// 关闭服务器
                }
                System.exit(0);// 退出程序
            }
        });

        // 文本框按回车键时事件
        txt_message.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });

        // 单击发送按钮时事件
        btn_send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                send();
            }
        });

        // 单击启动服务器按钮时事件
        btn_start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int port;
                port = Integer.parseInt(txt_port.getText());
                try {
                    serverStart(port);
                } catch (BindException e1) {
                    e1.printStackTrace();
                }
                JOptionPane.showMessageDialog(frame, "服务器成功启动!");
                txt_port.setEnabled(false);
                btn_stop.setEnabled(true);

            }
        });


        // 单击停止服务器按钮时事件
        btn_stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                closeServer();
                btn_start.setEnabled(true);
                btn_stop.setEnabled(false);
                contentArea.append("服务器成功停止!\r\n");
            }
        });
    }

    // 启动服务器
    public void serverStart(int port) throws java.net.BindException {
        try {
            clients = new ArrayList<ClientThread>();
            serverSocket = new ServerSocket(port);
            serverThread = new ServerThread(serverSocket);
            serverThread.start();
            isStart = true;

        } catch (BindException e) {

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // 关闭服务器

    public void closeServer() {
        try {
            if (serverThread != null)
                serverThread.stop();// 停止服务器线程

            for (int i = clients.size() - 1; i >= 0; i--) {
                // 给所有在线用户发送关闭命令
                clients.get(i).getWriter().println("CLOSE");
                clients.get(i).getWriter().flush();
                // 释放资源
                clients.get(i).stop();// 停止此条为客户端服务的线程
                clients.get(i).reader.close();
                clients.get(i).writer.close();
                clients.get(i).socket.close();
                clients.remove(i);
            }
            if (serverSocket != null) {
                serverSocket.close();// 关闭服务器端连接
            }
            listModel.removeAllElements();// 清空用户列表
            isStart = false;
        } catch (IOException e) {
            e.printStackTrace();
            isStart = true;
        }
    }

    // 群发服务器消息
    public void sendServerMessage(String message) {
        for (int i = clients.size() - 1; i >= 0; i--) {
            clients.get(i).getWriter().println("服务器：" + message + "(群聊)");
            clients.get(i).getWriter().flush();
        }
    }

    // 服务器线程
    class ServerThread extends Thread {
        private ServerSocket serverSocket;

        // 服务器线程的构造方法
        public ServerThread(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {
            while (true) {// 不停的等待客户端的链接
                try {
                    Socket socket = serverSocket.accept();
                    ClientThread client = new ClientThread(socket);
                    client.start();// 开启对此客户端服务的线程
                    clients.add(client);
                    listModel.addElement(client.getUser().getName());// 更新在线列表
                    contentArea.append(client.getUser().getName()
                            + client.getUser().getIp() + "上线!\r\n");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 为一个客户端服务的线程
    class ClientThread extends Thread {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private User user;

        public BufferedReader getReader() {
            return reader;
        }

        public PrintWriter getWriter() {
            return writer;
        }

        public User getUser() {
            return user;
        }

        // 客户端线程的构造方法
        public ClientThread(Socket socket) {
            try {
                this.socket = socket;
                reader = new BufferedReader(new InputStreamReader(socket
                        .getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                // 接收客户端的基本用户信息
                String inf = reader.readLine();

                //得到接入的是谁
                StringTokenizer st = new StringTokenizer(inf, "@");
                String name = st.nextToken();
                String ip = st.nextToken();

                socketMap.put(name,socket);
                probablyMap.put(name,1.0);
                System.out.println(socketMap);
                System.out.println(probablyMap);

                user = new User(name, ip);

                // 反馈连接成功信息
                writer.println(user.getName() + user.getIp() + "与服务器连接成功!");
                writer.flush();
                // 反馈当前在线用户信息
                if (clients.size() > 0) {
                    String temp = "";
                    for (int i = clients.size()-1; i >= 0; i--) {
                        temp += (clients.get(i).getUser().getName() + "/" + clients
                                .get(i).getUser().getIp())
                                + "@";
                    }
                    writer.println("USERLIST@" + clients.size() + "@" + temp);
                    writer.flush();
                }
                // 向所有在线用户发送该用户上线命令
                for (int i = clients.size() - 1; i >= 0; i--) {
                    clients.get(i).getWriter().println(
                            "ADD@" + user.getName() + user.getIp());
                    clients.get(i).getWriter().flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 不断接收客户端的消息，进行处理。
        public void run() {
            String message = null;
            while (true) {
                try {
                    message = reader.readLine();// 接收客户端消息
                    if (message.equals("CLOSE"))// 下线命令
                    {
                        contentArea.append(this.getUser().getName()
                                + this.getUser().getIp() + "下线!\r\n");
                        // 断开连接释放资源
                        reader.close();
                        writer.close();
                        socket.close();

                        // 向所有在线用户发送该用户的下线命令
                        for (int i = clients.size() - 1; i >= 0; i--) {
                            clients.get(i).getWriter().println(
                                    "DELETE@" + user.getName());
                            clients.get(i).getWriter().flush();
                        }

                        listModel.removeElement(user.getName());// 更新在线列表

                        // 删除此条客户端服务线程
                        for (int i = clients.size() - 1; i >= 0; i--) {
                            if (clients.get(i).getUser() == user) {
                                ClientThread temp = clients.get(i);
                                clients.remove(i);// 删除此用户的服务线程
                                temp.stop();// 停止这条服务线程
                                return;
                            }
                        }
                    } else {
                        System.out.println(message);


                        if(!message.substring(message.length()-5,message.length()-4).equals("@")) {

                            if (message.startsWith("mix0")) {
                                increase_propablity("mix0");
                                System.out.println(probablyMap);
                                message = message.substring(4);
                            } else if (message.startsWith("mix1")) {
                                increase_propablity("mix1");
                                System.out.println(probablyMap);
                                message = message.substring(4, message.length());
                            } else if (message.startsWith("mix2")) {
                                increase_propablity("mix2");
                                System.out.println(probablyMap);
                                message = message.substring(4, message.length());
                            } else if (message.startsWith("mix3")) {
                                increase_propablity("mix3");
                                System.out.println(probablyMap);
                                message = message.substring(4, message.length());
                            }else if (message.startsWith("mix4")) {
                                increase_propablity("mix4");
                                System.out.println(probablyMap);
                                message = message.substring(4, message.length());
                            }else if (message.startsWith("mix5")) {
                                increase_propablity("mix5");
                                System.out.println(probablyMap);
                                message = message.substring(4, message.length());
                            } else if (message.startsWith("mix6")) {
                                increase_propablity("mix6");
                                System.out.println(probablyMap);
                                message = message.substring(4, message.length());
                            }else if(message.startsWith("cli0")){
                                message = message.substring(4, message.length());
                            }else if(message.startsWith("cli1")){
                                message = message.substring(4,message.length());
                            }

                            if(message.endsWith("mix0"))
                            {
                                Socket socket2 = socketMap.get("mix0");
                                decrease_propablity("mix0");
                                System.out.println(probablyMap);
                                PrintWriter out =new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                                out.write(message+"\n");
                                out.flush();
                            }else if(message.endsWith("mix1"))
                            {
                                Socket socket2 = socketMap.get("mix1");
                                decrease_propablity("mix1");
                                System.out.println(probablyMap);
                                PrintWriter out =new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                                out.write(message+"\n");
                                out.flush();
                            }else if(message.endsWith("mix2"))
                            {
                                Socket socket2 = socketMap.get("mix2");
                                decrease_propablity("mix2");
                                System.out.println(probablyMap);
                                PrintWriter out =new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                                out.write(message+"\n");
                                out.flush();
                            }else if(message.endsWith("mix3"))
                            {
                                Socket socket2 = socketMap.get("mix3");
                                decrease_propablity("mix3");
                                System.out.println(probablyMap);
                                PrintWriter out =new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                                out.write(message+"\n");
                                out.flush();
                            }else if(message.endsWith("mix4"))
                            {
                                Socket socket2 = socketMap.get("mix4");
                                decrease_propablity("mix4");
                                System.out.println(probablyMap);
                                PrintWriter out =new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                                out.write(message+"\n");
                                out.flush();
                            }else if(message.endsWith("mix5"))
                            {
                                Socket socket2 = socketMap.get("mix5");
                                decrease_propablity("mix5");
                                System.out.println(probablyMap);
                                PrintWriter out =new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                                out.write(message+"\n");
                                out.flush();
                            }else if(message.endsWith("mix6"))
                            {
                                Socket socket2 = socketMap.get("mix6");
                                decrease_propablity("mix6");
                                System.out.println(probablyMap);
                                PrintWriter out =new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                                out.write(message+"\n");
                                out.flush();
                            }else if(message.endsWith("mix7"))
                            {
                                Socket socket2 = socketMap.get("mix7");
                                decrease_propablity("mix7");
                                System.out.println(probablyMap);
                                PrintWriter out =new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                                out.write(message+"\n");
                                out.flush();
                            }else if(message.endsWith("cli0")){
                                Socket socket2 = socketMap.get("cli0");
                                System.out.println(probablyMap);
                                message = message.substring(0,message.length()-4);
                                message = EncryptUncrypt.uncrypt(message, -5);
                                PrintWriter out =new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                                out.write(message+"\n");
                                out.flush();
                            }
                            else if(message.endsWith("cli1")){
                                Socket socket2 = socketMap.get("cli1");
                                System.out.println(probablyMap);
                                message = message.substring(0,message.length()-4);
                                message = EncryptUncrypt.uncrypt(message, 6);
                                PrintWriter out =new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                                out.write(message+"\n");
                                out.flush();
                            }
                        }
                        if(message.length() > 4) {
                            if (message.substring(message.length() - 5, message.length() - 4).equals("@")) {

                                if (message.startsWith("mix0")) {
                                    increase_propablity("mix0");
                                    System.out.println(probablyMap);
                                } else if (message.startsWith("mix1")) {
                                    increase_propablity("mix1");
                                    System.out.println(probablyMap);
                                } else if (message.startsWith("mix2")) {
                                    increase_propablity("mix2");
                                    System.out.println(probablyMap);
                                } else if (message.startsWith("mix3")) {
                                    increase_propablity("mix3");
                                    System.out.println(probablyMap);
                                } else if (message.startsWith("mix4")) {
                                    increase_propablity("mix4");
                                    System.out.println(probablyMap);
                                } else if (message.startsWith("mix5")) {
                                    increase_propablity("mix5");
                                    System.out.println(probablyMap);
                                } else if (message.startsWith("mix6")) {
                                    increase_propablity("mix6");
                                    System.out.println(probablyMap);
                                }else if (message.startsWith("mix7")) {
                                    increase_propablity("mix7");
                                    System.out.println(probablyMap);
                                }

                                dispatcherMessage(message);// 转发消息
                            }
                        }
                    }
                    EdgeWeightedGraph gra = makeMap(probablyMap);
                    simpleDetect(probablyMap);
                    communityDetect(gra);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                simpleDetect(probablyMap);
            }
        }

        // 转发消息 进行收到信息的切分
        public void dispatcherMessage(String message) throws IOException {
            StringTokenizer stringTokenizer = new StringTokenizer(message, "@");
            String source = stringTokenizer.nextToken();
            String owner = stringTokenizer.nextToken();
            String content = stringTokenizer.nextToken();
            String chatName=stringTokenizer.nextToken();
            message = source + "说：" + content;
            contentArea.append(message + "\r\n");
            if (owner.equals("ALL")) {// 群发
                for (int i = clients.size() - 1; i >= 0; i--) {
                    clients.get(i).getWriter().println(message + "(多人发送)");
                    clients.get(i).getWriter().flush();
                }
            }else{//私聊

                content = content.substring(4);
                if(content.endsWith("mix0"))
                {
                    Socket socket2 = socketMap.get("mix0");
                    decrease_propablity("mix0");
                    System.out.println(probablyMap);
                    PrintWriter out =new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                    out.write(content+"\n");
                    out.flush();
                }else if(content.endsWith("mix1"))
                {
                    Socket socket2 = socketMap.get("mix1");
                    decrease_propablity("mix1");
                    System.out.println(probablyMap);
                    PrintWriter out =new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                    out.write(content+"\n");
                    out.flush();
                }else if(content.endsWith("mix2"))
                {
                    Socket socket2 = socketMap.get("mix2");
                    decrease_propablity("mix2");
                    System.out.println(probablyMap);
                    PrintWriter out =new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                    out.write(content+"\n");
                    out.flush();
                }else if(content.endsWith("mix3"))
                {
                    Socket socket2 = socketMap.get("mix3");
                    decrease_propablity("mix3");
                    System.out.println(probablyMap);
                    PrintWriter out =new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                    out.write(content+"\n");
                    out.flush();
                }else if(content.endsWith("mix4"))
                {
                    Socket socket2 = socketMap.get("mix4");
                    decrease_propablity("mix4");
                    System.out.println(probablyMap);
                    PrintWriter out =new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                    out.write(content+"\n");
                    out.flush();
                }else if(content.endsWith("mix5"))
                {
                    Socket socket2 = socketMap.get("mix5");
                    decrease_propablity("mix5");
                    System.out.println(probablyMap);
                    PrintWriter out =new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                    out.write(content+"\n");
                    out.flush();
                }else if(content.endsWith("mix6"))
                {
                    Socket socket2 = socketMap.get("mix6");
                    decrease_propablity("mix6");
                    System.out.println(probablyMap);
                    PrintWriter out =new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                    out.write(content+"\n");
                    out.flush();
                }else if(content.endsWith("mix7"))
                {
                    Socket socket2 = socketMap.get("mix7");
                    decrease_propablity("mix7");
                    System.out.println(probablyMap);
                    PrintWriter out =new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                    out.write(content+"\n");
                    out.flush();
                }
                else {
/*
                else if(s.endsWith("cli0"))
                {
                    Socket socket2 = socketMap.get("cli0");
                    PrintWriter out =new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                    out.write(s+"\n");
                    out.flush();
                }else if(s.endsWith("cli1"))
                {
                    Socket socket2 = socketMap.get("cli1");
                    PrintWriter out =new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket2.getOutputStream(),"utf-8")));
                    out.write(s+"\n");
                    out.flush();
                }
*/
                for (int i = clients.size() - 1; i >= 0; i--) {
                    if (listModel.getElementAt(i).equals(chatName)) {

                        if (content.endsWith("cli0")) {
                            content = content.substring(0, content.length() - 4);
                            content = EncryptUncrypt.uncrypt(content, secret[3]);
                        } else if (content.endsWith("cli1")) {
                            content = content.substring(0, content.length() - 4);
                            content = EncryptUncrypt.uncrypt(content, secret[4]);
                        }
                        System.out.println(content);

                        clients.get(i).getWriter().println(content + "(私聊)");
                        clients.get(i).getWriter().flush();

                    }
                }

                }
            }
        }
    }

    //声誉增加
    public Map<String,Double> increase_propablity(String id){
        probablyMap.put(id,probablyMap.get(id)+0.1);
        return probablyMap;
    }

    //声誉减少
    public Map<String,Double> decrease_propablity(String id) {
        probablyMap.put(id, probablyMap.get(id) - 0.1);
        return probablyMap;
    }

    //节点声誉低于榨汁，直接删掉
    public void simpleDetect(Map<String,Double> probablyMap) {
        boolean abc = false;
        for(String key : probablyMap.keySet()) {
            for(int i = 0; i > 0; i++) {
                if(key == idStrings[i]) {
                    if(probablyMap.get(key) <= 0.7){
                        abc = true;
                        String aa = "发现恶意节点" + idStrings[i] + ",将其隔离。";
                        System.out.println(aa);
                        contentArea.append(aa + "\r\n");
                    }
                }
            }
        }
        if(!abc)
            contentArea.append("简单检测通过，目前未发现可疑节点" + "\r\n");
    }

    //社区检查
    public void communityDetect(EdgeWeightedGraph graph){
        List<Edge> unhonestedges = new ArrayList<Edge>();
        double averageProbably=0;
        for(int i = 0;i <graph.V();i++) {
            for (Edge e : graph.adj[i]){
                if(e.weight >= 0.85)
                    continue;
                if((e.weight>=0.7)&&(e.weight<0.85)) {
                    contentArea.append("社区检查,找到可疑临界边" + e + "\r\n");
                    unhonestedges.add(e);
                }
                }
            for(int j = 0; j< unhonestedges.size();j++){
                averageProbably += unhonestedges.get(j).weight;
            }
            }
        averageProbably = averageProbably/unhonestedges.size();
        contentArea.append("可疑链接平均声誉值" + averageProbably);
        if(averageProbably < 0.8&&unhonestedges.size()>5){
            contentArea.append("社区检查,找到临界边：" + "\r\n");
            for (Edge ee : unhonestedges)
            contentArea.append("                   " + ee + "\r\n");
        }
        }

    //生成概率拓扑图
    public EdgeWeightedGraph makeMap(Map<String,Double> probablyMap){
        EdgeWeightedGraph graph = new EdgeWeightedGraph(10);
        double[] probably = new double[10];
        Map<String,Edge> mapp = new HashMap<>();
        for(String key : probablyMap.keySet()){
            if(key.equals("mix0"))
                probably[0] = probablyMap.get("mix0");
            else if(key.equals("mix1"))
                probably[1] = probablyMap.get("mix1");
            else if(key.equals("mix2"))
                probably[2] = probablyMap.get("mix2");
            else if(key.equals("mix3"))
                probably[5] = probablyMap.get("mix3");
            else if(key.equals("mix4"))
                probably[6] = probablyMap.get("mix4");
            else if(key.equals("mix5"))
                probably[7] = probablyMap.get("mix5");
            else if(key.equals("mix6"))
                probably[8] = probablyMap.get("mix6");
            else if(key.equals("mix7"))
                probably[9] = probablyMap.get("mix7");
            else if(key.equals("cli0"))
                probably[3] = probablyMap.get("cli0");
            else if(key.equals("cli1"))
                probably[4] = probablyMap.get("cli1");
        }
        for(int i = 0;i < 10;i++){
            for(int j = 0;j < i;j++){
                mapp.put(idStrings[j]+"-"+idStrings[i],new Edge(j, i, (probably[i]+probably[j])/2));
                graph.addEdge(mapp.get(idStrings[j]+"-"+idStrings[i]));
            }
        }
        System.out.println(graph);
        contentArea.append(graph + "\r\n");
        return graph;
    }

    public void makeCascade(Map<String,Double> probablyMap){
        Vector Cascade = new Vector();
        Map<String, Integer> probably = new HashMap();
        for(String key : probablyMap.keySet()) {
            probably.put(key, (int)(10*probablyMap.get(key)));
        }

    }
}