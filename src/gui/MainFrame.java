package gui;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import filetransmission.MainThread;
import usage.UserInfo;
import web.ServerRequester;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MainFrame extends JFrame {
    private UserInfo userInfo;
    private ServerRequester requester;
    private MainThread mainThread;
    TaskPanel tasksPanel;
    
    public MainFrame(UserInfo userInfo, ServerRequester requester) {
        super("p2p Resource Sharing forum");
        this.userInfo=userInfo;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addWindowListener(new MainFrameWindowListener());

        mainThread=new MainThread(requester,userInfo);
        Thread mainT=new Thread(mainThread);
        mainT.start();

        // the 4 below tabs of JPanel
        JTabbedPane tabbedPane=new JTabbedPane(JTabbedPane.BOTTOM);
        this.requester=requester;
        //Publish Panel
        PublishPanel publishPanel=new PublishPanel(requester, userInfo, mainThread);

        //Search Panel
        SearchPanel searchPanel=new SearchPanel(requester, userInfo, mainThread);

        // task panel
        tasksPanel=new TaskPanel(mainThread);
        Thread taskT=new Thread(tasksPanel);
        taskT.start();

        // profile panel
        ProfilePanel profilePanel=new ProfilePanel(userInfo);


        tabbedPane.addTab("Publish", publishPanel);
        tabbedPane.addTab("Search", searchPanel);
        tabbedPane.addTab("Tasks", tasksPanel);
        tabbedPane.addTab("Profile", profilePanel);

        //Every time the user selects the profile panel,
        // the program retrieve UserInfo from the server and updates the panel.
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(tabbedPane.getTitleAt(((JTabbedPane)(e.getSource())).getSelectedIndex()).equals("Profile")){
                    try{
                        JSONObject res=requester.getuserinfo(userInfo.getUsername());
                        if(!res.getBoolean("success")){
                            JOptionPane.showMessageDialog(MainFrame.this,"Failed to retrieve user information from the server.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        profilePanel.updatePanel(JSON.parseObject(res.get("data").toString(),UserInfo.class));
                    }catch (Exception exception){
                        //exception.printStackTrace();
                    }
                }
                if(tabbedPane.getTitleAt(((JTabbedPane)(e.getSource())).getSelectedIndex()).equals("Tasks")){
                    tasksPanel.drawTaskListPanel();
                }
            }
        });
        this.getContentPane().add(tabbedPane);
        this.setLocationRelativeTo(null);
        this.setSize(1000,800);
        this.setVisible(true);
        JOptionPane.showMessageDialog(this,"Login successful.","Welcome",JOptionPane.INFORMATION_MESSAGE);
    }
    
    class MainFrameWindowListener extends WindowAdapter{
    	public void windowClosing(WindowEvent e) {
    		super.windowClosing(e);
    		mainThread.exitProgram();
    		tasksPanel.setIsClosed(true);
    	}
    }
}