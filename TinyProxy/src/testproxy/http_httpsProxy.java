package testproxy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class http_httpsProxy extends Thread{

    static private http_httpsProxy instance=null;
    //代理服务器实例
    //操作实现代理服务器的类
    public static int clientTIMEOUT = 60*1000;  //30s
    //代理服务器等待客户端Socket输入的等待时间
    public static int proxyTIMEOUT =60*1000;  //30s
    //设置代理服务器与服务器端的连接未活动超时时间
    public static int BUFSIZ = 1024;   //1024bytes
    //输入的缓冲大小
    static public boolean logging = true;
    //是否要求代理服务器在日志中记录所有已传输的数据
    static public OutputStream log = System.out;
    //默认日志例程将向该OutputStream对象输出日志信息
    public static int proxyPort = 908;
    //代理服务器端口
    public static int protocolPort = 80;
    //协议对应的端口
    //80 http
    //344 https
    public static String parent = null;
    //上级代理服务器
    public static int parentPort = -1;
    // 用来把一个代理服务器链接到另一个代理服务器（需要指定另一个服务器的名称和端口）。

    @SuppressWarnings("resource")
    //public static void main(String[] args) {
    public void run(){
        System.out.println("HTTP代理服务器已经成功启动！");
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(proxyPort);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        while(true) {
            try {
                final Socket socket = ss.accept();
                socket.setSoTimeout(clientTIMEOUT);//设置代理服务器与客户端的连接未活动超时时间
                //新建线程来处理accept到的客户端socket 连接
                new Thread() {
                    @Override
                    public void run() {
                        String line = "";
                        InputStream is = null;
                        try {
                            is = socket.getInputStream();//输入流
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String tempHost = "", host;
                        int port = protocolPort;//协议端口
                        String type = null;   //判断链接类型 http/https

                        OutputStream os = null;//输出流
                        try {
                            os = socket.getOutputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));//输入流缓存

                        int temp = 1;
                        StringBuilder sb = new StringBuilder();

                        while (true) {
                            try {
                                if ((line = br.readLine()) == null) break;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println("\nrequest:"+line);


                            if (temp == 1) {
                                //获取请求行中请求方法，下面会需要这个来判断是http还是https
                                type = line.split(" ")[0];
                                if (type == null) continue;
                            }
                            temp++;
                            String[] s1 = line.split(": ");
                            if (line.isEmpty()) {
                                break;
                            }
                            for (int i = 0; i < s1.length; i++) {
                                if (s1[i].equalsIgnoreCase("host")) {
                                    tempHost = s1[i + 1];
                                }
                            }
                            sb.append(line + "\r\n");
                            line = null;
                        }
                        sb.append("\r\n");          //不加上这行http请求则无法进行。这其实就是告诉服务端一个请求结束了

                        if (tempHost.split(":").length > 1) {
                            port = Integer.valueOf(tempHost.split(":")[1]);
                        }
                        host = tempHost.split(":")[0];
                        Socket proxySocket = null;
                        if (host != null && !host.equals("")) {
                            printLog(host, port, socket);
                            // 查看是否有上级代理服务器
                            if (parent != null) {
                                host = parent;
                                port = parentPort;
                            }
                            try {
                                proxySocket = new Socket(host, port);
                                proxySocket.setSoTimeout(proxyTIMEOUT);//设置代理服务器与服务器端的连接未活动超时时间
                                OutputStream proxyOs = proxySocket.getOutputStream();
                                InputStream proxyIs = proxySocket.getInputStream();
                                if (type.equalsIgnoreCase("connect")) {
                                    //https请求的话，告诉客户端连接已经建立（下面代码建立）
                                    os.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                                    os.flush();
                                } else {//http请求则直接转发
                                    proxyOs.write(sb.toString().getBytes("UTF-8"));
                                    proxyOs.flush();
                                }
                                new ProxyHandleThread(is,proxyOs).start();
                                //监听客户端传来消息并转发给服务器
                                new ProxyHandleThread(proxyIs, os).start();
                                //监听服务器传来消息并转发给客户端
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }catch (NullPointerException e2){
                                e2.printStackTrace();
                            }
                        }
                    }
                }.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private  http_httpsProxy(){
    }
    //单例模式
    public static http_httpsProxy  getInstance(){
        if(instance==null){
            instance=new http_httpsProxy();
        }
        return instance;
    }

    public static void writeLog(int c, boolean browser) throws IOException {
        //写记录
        log.write(c);

    }
    public static void writeLog(byte[] bytes, int offset, int len, boolean browser)
            throws IOException {
        //循环写记录
        for (int i = 0; i < len; i++)
            writeLog((int) bytes[offset + i], browser);
    }

    // 默认情况下，日志信息输出到控制台或文件
    public static void printLog(String url, int port, Socket sock) {
        java.text.DateFormat cal = java.text.DateFormat.getDateTimeInstance();
        System.out.println("\n"+cal.format(new java.util.Date()) + " - " + url + "-"
                + sock.getInetAddress());
    }
}
class ProxyHandleThread extends Thread {
    //用于完成消息转发，不涉及链接建立，在建立连接得到的输入输出流上操作消息交换
    private InputStream Input;
    private OutputStream Output;

    public ProxyHandleThread(InputStream Input, OutputStream Output) {
        this.Input = Input;
        this.Output= Output;
    }

    @Override
    public void run() {
        BufferedInputStream clientbis = new BufferedInputStream(Input);
        byte[] bytes =new byte[http_httpsProxy.BUFSIZ];
        int length=-1;

        try {
            while (true) {

                while((length=clientbis.read(bytes))!=-1) {
                    //这里最好是字节转发，不要用上面的InputStreamReader，因为https传递的都是密文，那样会乱码，消息传到服务器端也会出错。
                    Output.write(bytes, 0, length);
                    length =-1;
//					System.out.println("客户端通过代理服务器给服务器发送消息"+input+host);
                   //if (http_httpsProxy.logging)
                    //   http_httpsProxy.writeLog(bytes, 0, length, true);
                    //将传输的消息写入日志
                }
                Output.flush();

                try {
                    Thread.sleep(10);     //避免此线程独占cpu
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketTimeoutException e) {
            try {
                Input.close();
                Output.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }catch (IOException e) {
            System.out.println(e);
        }finally {
            try {
                Input.close();
                Output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

