import javax.security.auth.callback.CallbackHandler;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Scanner;


public class clientProxy extends JFrame {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
    	clientProxy client=new clientProxy();
        client.clientGUI();
    }

    public void clientGUI()
    {
        JFrame GUI=new JFrame();
        JTextField IP[]=new JTextField[4];

        JTextField Port[]=new JTextField[4];
        for(int i=0;i<4;i++){
            IP[i]=new JTextField("",12);
            Port[i]=new JTextField("",10);
        }

        JButton btn1 =new JButton("  设置  ");
        JButton btn2 =new JButton("删除设置");
        JButton btn3 =new JButton("退出");

        JLabel lab1=new JLabel("协议");
        JLabel lab2=new JLabel("代理服务器地址");
        JLabel lab3=new JLabel("端口");

        JCheckBox pthttp=new JCheckBox("HTTP",false);
        JCheckBox pthttps=new JCheckBox("HTTPs",false);
        JCheckBox ptftp=new JCheckBox("FTP",false);
        JCheckBox ptsocks=new JCheckBox("Socks",false);

        JCheckBox b1=new JCheckBox("为所有协议应用相同的IP和端口", false);
        //用给定的标题s构造选择框，参数b设置选中与否的初始状态。
        JCheckBox b2=new JCheckBox("代理不用于本地地址", true);
        JTextArea textArea=new JTextArea("127.0.0.1;",10,43);

        GUI.setTitle("代理服务器客户端");

        Font font=new Font("TimesRoman", Font.BOLD,15);

        JPanel JP=new JPanel();  //主布局
        JPanel JP1=new JPanel(new GridLayout(5,3,40,15));
        JP1.setFont(font);
        JPanel JP2=new JPanel(new FlowLayout());
        JPanel JP3=new JPanel(new FlowLayout());
        JPanel JP4=new JPanel(new FlowLayout());

        JP1.setSize(400,400);
        JP1.setBorder(BorderFactory.createTitledBorder("设置"));
        JP4.setBorder(BorderFactory.createTitledBorder("设置例外地址 ( 以 ; 分隔 )"));
        JP2.setSize(400,20);
        JP4.setSize(400,70);

        for(int i=0;i<4;i++){
            IP[i].setFont(font);
            Port[i].setFont(font);
        }

        textArea.setFont(font);
        b1.setFont(font);
        b2.setFont(font);

        GUI.setFont(font);
        GUI.setFont(font);
        GUI.setBounds(400, 200, 600, 650);
        GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        btn1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //设置代理服务器配置
                String ip=null;
                String port=null;

                int i=0;
                String exclude=null;
                while((ip==null||ip.equals("")||port==null||port.equals(""))&&i<4){
                    ip=IP[i].getText();
                    port=Port[i].getText();
                    i++;
                }
                boolean optionA=b1.isSelected(); //单选框
                boolean optionB=b2.isSelected(); //单选框
                String  exludeIP=textArea.getText(); //额外ip

                if(!ip.equals("")&&!port.equals("")){
                    boolean flag=setProxy(ip,port,exludeIP);
                    if(flag)
                        JOptionPane.showMessageDialog(new JPanel(),"设置成功", "message",JOptionPane.WARNING_MESSAGE);
                }
                else{
                    setProxy("172.0.0.1","9080",exludeIP);
                }
            }
        });

        btn2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //删除代理服务器配置
                removeProxy();
                JOptionPane.showMessageDialog(new JPanel(),"删除配置成功", "message",JOptionPane.WARNING_MESSAGE);
            }
        });

        btn3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //退出
                GUI.setVisible(false);
                GUI.dispose();
            }
        });

        JP1.add(lab1);
        JP1.add(lab2);
        JP1.add(lab3);

        JP1.add(pthttp);
        JP1.add(IP[0]);
        JP1.add(Port[0]);

        JP1.add(pthttps);
        JP1.add(IP[1]);
        JP1.add(Port[1]);

        JP1.add(ptftp);
        JP1.add(IP[2]);
        JP1.add(Port[2]);

        JP1.add(ptsocks);
        JP1.add(IP[3]);
        JP1.add(Port[3]);

        JP2.add(b1);
        JP2.add(b2);

        JP3.add(btn1);
        JP3.add(btn2);
        JP3.add(btn3);

        JP4.add(textArea);

        JP.add(BorderLayout.NORTH,JP1);
        JP.add(BorderLayout.CENTER,JP2);
        JP.add(BorderLayout.SOUTH,JP3);
        JP.add(BorderLayout.SOUTH,JP4);
        GUI.add(JP);
        GUI.setVisible(true);
    }

    public static boolean setProxy(String ip, String port,String exclude){
        String code = "@echo off\r\n"
                + "set ip=" + ip + "\r\n"
                + "set port=" + port + "\r\n"
                + "set exclude="+exclude+"\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\\Connections\" /v \"DefaultConnectionSettings\" /t  REG_BINARY /d \"3C000000AA0100000B0000000F000000\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyEnable\" /t  REG_DWORD /d \"1\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyServer\" /t  REG_SZ /d \"%ip%:%port%\" /f\r\n"
                + "REG ADD \"HKLM\\System\\CurrentControlSet\\Hardware Profiles\\0001\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyEnable\" /t  REG_DWORD /d \"1\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\\Connections\" /v \"SavedLegacySettings\" /t  REG_BINARY /d \"3C000000AE0100000B0000000F000000\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyOverride\" /t  REG_SZ /d \"%exclude%;<local>\" /f\r\n"
                + "set rp=\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\"\r\n"
                + "set rk=\"ProxyServer\"\r\n"
                + "for /f \"tokens=*\" %%a in ('reg query %rp% /v %rk%^|findstr %rk%') do (\r\n"
                + "call :doit %%a\r\n"
                + ")\r\n"
                + "goto :eof\r\n"
                + ":doit\r\n"
                + "echo %3\r\n"
                + "echo.&echo.\r\n"
                + "exit";
        String setVaildNow="@echo off\r\n"
                +"ipconfig /flushdns\r\n"
                +"SHChangeNotify(SHCNE_ASSOCCHANGED, SHCNF_FLUSH, NULL, NULL);\r\n"
                +"SendMessage(HWND_BROADCAST, WM_SETTINGCHANGE, 0, 0);\r\n"
                +"ms-settings:network\r\n"
                +"exit";

       boolean set=runBat(code,ip+":"+port);
       runBat(setVaildNow,"");
        return set;
    }
    public static void removeProxy(){
        String code = "@echo off\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\\Connections\" /v \"DefaultConnectionSettings\" /t  REG_BINARY /d \"3C000000AA0100000B0000000F000000\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyEnable\" /t  REG_DWORD /d \"0\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyServer\" /t  REG_SZ /d \"\" /f\r\n"
                + "REG ADD \"HKLM\\System\\CurrentControlSet\\Hardware Profiles\\0001\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyEnable\" /t  REG_DWORD /d \"1\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\\Connections\" /v \"SavedLegacySettings\" /t  REG_BINARY /d \"3C000000AE0100000B0000000F000000\" /f\r\n"
                + "REG ADD \"HKCU\\SOFTWARE\\MICROSOFT\\Windows\\CURRENTVERSION\\Internet Settings\" /v \"ProxyOverride\" /t  REG_SZ /d \"<local>\" /f\r\n"
                + "exit";
        runBat(code,"");

        String setVaildNow="@echo off\r\n"
                +"ipconfig /flushdns\r\n"
                +"SHChangeNotify(SHCNE_ASSOCCHANGED, SHCNF_FLUSH, NULL, NULL);\r\n"
                +"SendMessage(HWND_BROADCAST, WM_SETTINGCHANGE, 0, 0);\r\n"
                +"ms-settings:network\r\n"
                +"exit";
        runBat(setVaildNow,"");
    }

    private static boolean runBat(String code,String flag){

        //https://blog.csdn.net/baidu_23275675/article/details/84427757
        File file = new File("temp.bat");
        String str = "";
        try {
            if(!file.exists()) file.createNewFile();
            PrintWriter pw = new PrintWriter(file);
            pw.write(code);
            pw.flush();
            pw.close();
            Process child = Runtime.getRuntime().exec("cmd /c temp.bat");
            InputStream in = child.getInputStream();
            Scanner sc = new Scanner(in,"gbk");
            while(sc.hasNext()){
                str += sc.nextLine();
            }
            try {
                child.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sc.close();
            Thread.sleep(1000);
            file.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(str.contains(flag)) return true;
        else return false;
    }

}





