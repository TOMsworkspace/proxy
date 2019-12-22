package testproxy;


public class TestProxy {
    public static void main(String [] argvs){
        http_httpsProxy proxy=http_httpsProxy.getInstance();

        //服务器配置
        //proxy.clientTIMEOUT = 60*1000;  //30s
        //代理服务器等待客户端Socket输入的等待时间
        //proxy.proxyTIMEOUT =60*1000;  //30s
        //设置代理服务器与服务器端的连接未活动超时时间
        //proxy.logging = true;
        //是否要求代理服务器在日志中记录所有已传输的数据
        //proxy.log = System.out;
        //默认日志例程将向该OutputStream对象输出日志信息
        //proxy.proxyPort = 9080;
        //代理服务器端口
        //proxy.protocolPort = 80;
        //协议对应的端口
        //80 http
        //344 https
        //proxy.parent = null;
        //上级代理服务器
        //proxy.parentPort = -1;
        // 用来把一个代理服务器链接到另一个代理服务器（需要指定另一个服务器的名称和端口）。
        proxy.start();  //代理服务器线程
    }
}
