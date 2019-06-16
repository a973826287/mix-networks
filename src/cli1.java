import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
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

public class cli1{

    private String idString = "cli1";
    private String Receive = "0client";
    private String record_send_message;//记录自己发送的信息
    private String record_receive_message;//记录自己收到的信息
    private int secret[] = {7,-8,9,-5,6,-1};//一次是mix0,mix1,mix2,client0,client1,mix3
    public Vector path = new Vector();
    public  String idStrings[] = {"mix0", "mix1", "mix2", "cli0", "cli1","mix3","mix4","mix5","mix6","mix7"};
    //UI声明段
    private JFrame frame;
    private JList userList;
    private JTextArea textArea;
    private JTextField textField;
    private JLabel lb_port;
    private JLabel lb_hostIp;
    private JLabel lb_name;
    private JButton btn_start;
    private JButton btn_stop;
    private JButton btn_send;
    private JButton btn_all;
    private JButton btn_one;
    private JPanel northPanel;
    private JPanel southPanel;
    private JScrollPane rightScroll;
    private JScrollPane leftScroll;
    private JSplitPane centerSplit;


    private DefaultListModel listModel;
    private boolean isConnected = false;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private MessageThread messageThread;// 负责接收消息的线程
    private Map<String, User> onLineUsers = new HashMap<String, User>();// 所有在线用户

    // 主方法,程序入口
    public static void main(String[] args) {
        new cli1();
    }

    // 构造方法
    public cli1() {
        // 成员变量
        textArea = new JTextArea();
        textArea.setEditable(false);
        textField = new JTextField();
        lb_port = new JLabel("8080");
        lb_hostIp = new JLabel("127.0.0.1");
        lb_name = new JLabel("cli1");
        btn_start = new JButton("连接");
        btn_stop = new JButton("断开");
        btn_send = new JButton("发送");

        btn_all = new JButton("群聊");
        btn_one = new JButton("私聊");

        listModel = new DefaultListModel();
        userList = new JList(listModel);

        //布局
        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 7));

        northPanel.add(new JLabel("用户姓名："));
        northPanel.add(lb_name);
        northPanel.add(btn_all);
        northPanel.add(btn_one);
        northPanel.add(btn_start);
        northPanel.add(btn_stop);
        northPanel.setBorder(new TitledBorder("连接状态区"));

        rightScroll = new JScrollPane(textArea);
        rightScroll.setBorder(new TitledBorder("消息显示区"));

        leftScroll = new JScrollPane(userList);
        leftScroll.setBorder(new TitledBorder("节点群"));

        southPanel = new JPanel(new BorderLayout());
        southPanel.add(textField, "Center");
        southPanel.add(btn_send, "East");
        southPanel.setBorder(new TitledBorder("编写消息区"));

        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll,
                rightScroll);
        centerSplit.setDividerLocation(100);

        frame = new JFrame("cli1");
        frame.setLayout(new BorderLayout());
        frame.add(northPanel, "North");
        frame.add(centerSplit, "Center");
        frame.add(southPanel, "South");
        frame.setSize(600, 400);
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setLocation((screen_width - frame.getWidth()) / 2,
                (screen_height - frame.getHeight()) / 2);
        frame.setVisible(true);

        // 写消息的文本框中按回车键时事件
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                send();
            }
        });

        // 单击发送按钮时事件
        btn_send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });

        // 单击连接按钮时事件
        btn_start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int port;
                port = Integer.parseInt(lb_port.getText());
                String hostIp = lb_hostIp.getText();
                String name = lb_name.getText();
                connectServer(port, hostIp, name);
                frame.setTitle(name);

            }
        });

        // 单击断开按钮时事件
        btn_stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                closeConnection();// 断开连接
                textArea.setText("");
            }
        });

        //群聊
        btn_all.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userList.clearSelection();
                textArea.setText("");
                textArea.append("群聊中..."+"\r\n");
            }
        });
        // 私聊
        btn_one.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
                textArea.append(" 私聊中..."+"\r\n");
            }
        });
        // 关闭窗口时事件
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isConnected) {
                    closeConnection();// 关闭连接
                }
                System.exit(0);// 退出程序
            }
        });
    }

    // 连接服务器
    public boolean connectServer(int port, String hostIp, String name) {

        try {
            socket = new Socket(hostIp, port);// 根据端口号和服务器ip建立连接
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));
            // 发送客户端用户基本信息(用户名和ip地址)
            sendMessage(name + "@" + socket.getLocalAddress().toString());
            // 开启接收消息的线程
            messageThread = new MessageThread(reader, textArea);
            messageThread.start();
            isConnected = true;// 已经连接上了
            return true;
        } catch (Exception e) {
            textArea.append("与端口号为：" + port + "    IP地址为：" + hostIp
                    + "   的服务器连接失败!" + "\r\n");
            isConnected = false;// 未连接上
            return false;
        }
    }

    // 执行发送
    public void send() {
        Vector len4_messages = new Vector();
        String message = textField.getText();

        if(userList.isSelectionEmpty()){
            String message1= "all";
            textArea.append(frame.getTitle()+":"+message+"\r\n");
            sendMessage(frame.getTitle() + "@" + "ALL" + "@" + message+"@"+message1);
            textField.setText(null);
        }else{
            String message1 = (String) userList.getSelectedValue();
            textArea.append(frame.getTitle()+":"+message+"\r\n");
            //得到路径信息
            int n = getMixNumber();
            getPath(n,path);

            len4_messages = messageProcess(n,message);
            for(int i = 0;i < len4_messages.size(); i++){
                String s = (String)len4_messages.get(i);
                sendMessage(frame.getTitle() + "@" + "ONE" + "@"+ s +"@"+message1);
            }
            textField.setText(null);
        }
    }

    // 发送消息
    public void sendMessage(String message) {
        writer.println(message);
        writer.flush();
    }

    // 客户端主动关闭连接
    public synchronized boolean closeConnection() {
        try {
            sendMessage("CLOSE");// 发送断开连接命令给服务器
            messageThread.stop();// 停止接受消息线程
            // 释放资源
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            isConnected = false;
            return true;
        } catch (IOException e1) {
            e1.printStackTrace();
            isConnected = true;
            return false;
        }
    }


    // 不断接收消息的线程
    class MessageThread extends Thread {
        private BufferedReader reader;
        private JTextArea textArea;

        // 接收消息线程的构造方法
        public MessageThread(BufferedReader reader, JTextArea textArea) {
            this.reader = reader;
            this.textArea = textArea;
        }

        // 被动的关闭连接
        public synchronized void closeCon() throws Exception {
            // 清空用户列表
            listModel.removeAllElements();
            // 被动的关闭连接释放资源
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            isConnected = false;// 修改状态为断开
        }

        //去除填充位
        public String receiveProcess(String message){
            //message = message.substring(0,message.length()-4);
            if(message.startsWith("000"))
            {
                message = message.substring(3);
            }
            else if(message.startsWith("00"))
            {
                message = message.substring(2);
            } else if (message.startsWith("0"))
            {
                message = message.substring(1);
            }
            System.out.println(message);
            return message;

        }


        public void run() {
            String message = "";
            while (true) {
                try {
                    message = reader.readLine();
                    StringTokenizer stringTokenizer = new StringTokenizer(
                            message, "/@");
                    String command = stringTokenizer.nextToken();// 命令，第一段
                    //处理标记信息
                    if (command.equals("CLOSE"))// 服务器已关闭命令
                    {
                        textArea.append("服务器已关闭!\r\n");
                        closeCon();// 被动的关闭连接
                        return;// 结束线程
                    } else if (command.equals("ADD")) {// 有用户上线更新在线列表
                        String username = "";
                        String userIp = "";
                        if ((username = stringTokenizer.nextToken()) != null
                                && (userIp = stringTokenizer.nextToken()) != null) {
                            User user = new User(username, userIp);
                            onLineUsers.put(username, user);
                            listModel.addElement(username);
                        }
                    } else if (command.equals("DELETE")) {// 有用户下线更新在线列表
                        String username = stringTokenizer.nextToken();
                        User user = (User) onLineUsers.get(username);
                        onLineUsers.remove(user);
                        listModel.removeElement(username);
                    } else if (command.equals("USERLIST")) {// 加载在线用户列表
                        int size = Integer
                                .parseInt(stringTokenizer.nextToken());
                        String username = null;
                        String userIp = null;

                        for (int i = 0; i < size; i++) {
                            username = stringTokenizer.nextToken();
                            userIp = stringTokenizer.nextToken();
                            User user = new User(username, userIp);
                            onLineUsers.put(username, user);
                            listModel.addElement(username);
                        }
                    }  else {// 普通消息
                        receiveProcess(message);//填充位的去除
                        //message = "cli0: " + message.substring(message.length()-4,message.length());
                        textArea.append(message + "\r\n");

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }



    //信息处理
    public Vector messageProcess(int n, String s){
        Vector len4_messages = new Vector();
        int num_package = 0;
        String len4_message;
        String supplement = "";

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
                len4_message =EncryptUncrypt.encrypt(
                        len4_message, secret[(int) path.get(j-1)])
                        + idStrings[(int)path.get(j-1)];
            }

            len4_messages.add(len4_message);
            path.clear();
        }
        return len4_messages;
    }

    //得到通过的节点数量
    public int getMixNumber(){
        Scanner messages=new Scanner(System.in);
        System.out.println("请输入节点个数：");
        int n = messages.nextInt();
        return n;
    }

    //得到数据传输的MIX路径
    public Vector getPath(int n, Vector path){
        Scanner messages=new Scanner(System.in);
        System.out.println("请输入path(0,1,2,3,4,5)" +
                "--分别表示mix0,mix1,mix2,client0,client1,mix3 ：");
        for(int i = 0; i < n; i++){
            path.add(messages.nextInt());
        }
        return path;
    }

}

