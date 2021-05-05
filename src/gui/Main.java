package gui;

import com.alibaba.fastjson.JSONObject;
import filetransmission.MainThread;
import usage.UserInfo;
import web.ServerRequester;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Enumeration;
import org.apache.commons.codec.digest.DigestUtils;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        InetSocketAddress inetSocketAddress=new InetSocketAddress("8.208.113.138",80);
        ServerRequester requester=new ServerRequester(inetSocketAddress);

        // user information
        String username;

        JFrame frame=new JFrame("p2p file sharing forum");
        frame.setSize(600,450);
        frame.setLocationRelativeTo(null);

        //login page
        // all user information inputs panels: username & password
        JPanel loginPanel=new JPanel();
        loginPanel.setLayout(new GridLayout(4, 1, 20, 20));
        JPanel usernamePanel = new JPanel();
        JTextField usernameField = new JTextField(/*mainThread.getUsername(),*/ 12);
        usernamePanel.add(new JLabel("Username:"));
        usernamePanel.add(usernameField);
        JPanel passwordPanel = new JPanel();
        JPasswordField passwordField = new JPasswordField(15);
        passwordPanel.add(new JLabel("Password:"));
        passwordPanel.add(passwordField);
        JPanel panel03 = new JPanel();

        // when click the login in button
        JButton loginBtn = new JButton("Log in");
        loginBtn.setActionCommand("login");
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message="";
                if(usernameField.getText()==null){
                    message+="Please enter username.\n";
                }
                if(passwordField.getPassword().length==0){
                    message+="Please enter password.";
                }
                if(message==null){
                    JOptionPane.showMessageDialog(loginPanel,message,"warning",JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try{
                    String passwordLoginInput=new String(passwordField.getPassword());
                    //byte[] bytesOfString=passwordLoginInput.getBytes(StandardCharsets.UTF_8);
                    //MessageDigest md=MessageDigest.getInstance("MD5");
                    //byte[] digestRes=md.digest(bytesOfString);
                    String digestString=DigestUtils.md5Hex(passwordLoginInput);

                    JSONObject res=requester.login(usernameField.getText(),new String(digestString));
                    if(!res.getBoolean("success")){
                        JOptionPane.showMessageDialog(loginPanel,res.getString("message"),"login failed",JOptionPane.WARNING_MESSAGE);
                        passwordField.setText("");
                        passwordPanel.repaint();
                    }else{
                        frame.dispose();
                        UserInfo userInfo=new UserInfo(usernameField.getText(),
                                res.getJSONObject("data").getIntValue("prestigeValue"),
                                res.getJSONObject("data").getIntValue("point"));
//                        MainThread mainThread=new MainThread(requester, userInfo);
//                        init(mainThread);
                        JFrame mainframe=new MainFrame(userInfo, requester);
                    }
                }catch (Exception exception){
                    JOptionPane.showMessageDialog(frame, "Connection failed.", "connection failed", JOptionPane.ERROR_MESSAGE);
                    exception.printStackTrace();
                }
            }
        });
        panel03.add(loginBtn);

        // panel for sign up
        JPanel panel04=new JPanel();
        JButton registerPageBtn=new JButton("Sign up");
        registerPageBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                //register panel
                frame.remove(loginPanel);
                JPanel registerPanel=new JPanel();
                registerPanel.setLayout(new GridLayout(6,1,20,20));

                //1, username register panel
                JPanel usernameRegisterPanel=new JPanel();
                JTextField usernameRegisterTextField=new JTextField(15);
                usernameRegisterPanel.add(new JLabel("Username: "));
                usernameRegisterPanel.add(usernameRegisterTextField);

                //2, username tips
                JPanel usernameTipPanel=new JPanel();
                usernameTipPanel.add(new JLabel("The length of the username must be between 6-20, containing only letters and digits. "));

                //3, password register panel
                JPanel passwordRegisterPanel=new JPanel();
                JPasswordField passwordRegisterField=new JPasswordField(15);
                passwordRegisterPanel.add(new JLabel("Password: "));
                passwordRegisterPanel.add(passwordRegisterField);

                //4, password tips
                JPanel passwordTipPanel=new JPanel();
                passwordTipPanel.add(new JLabel("The length of the password must be between 8-20."));

                //5, password confirm
                JPanel confirmPasswordPanel=new JPanel();
                JPasswordField passwordConfirmField=new JPasswordField(15);
                confirmPasswordPanel.add(new JLabel("Confirm password: "));
                confirmPasswordPanel.add(passwordConfirmField);

                //6. submit buttons
                JPanel registerSubmitPanel=new JPanel();
                JButton registerSubmitBtn=new JButton("Register");

                // when click the register button
                registerSubmitBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        // check whether the use name is valid
                        String usernameInput=usernameRegisterTextField.getText();
                        if(usernameInput.length()>20||usernameInput.length()<6){
                            JOptionPane.showMessageDialog(frame,"The length of the username must be between 6-20.","Invalid username",JOptionPane.ERROR_MESSAGE);
                            usernameRegisterTextField.setText("");
                            usernameRegisterPanel.repaint();
                            return;
                        }
                        for(int i=0;i<usernameInput.length();i++){
                            if(!Character.isLetterOrDigit(usernameInput.charAt(i))){
                                JOptionPane.showMessageDialog(frame,"The username must contain only letters and digits.","Invalid username",JOptionPane.ERROR_MESSAGE);
                                usernameRegisterTextField.setText("");
                                usernameRegisterPanel.repaint();
                                return;
                            }
                        }

                        // check whether the password is valid
                        String passwordInput=new String(passwordRegisterField.getPassword());
                        String passwordConfirm=new String(passwordConfirmField.getPassword());
                        if(passwordInput.length()<8||passwordInput.length()>20){
                            JOptionPane.showMessageDialog(frame,"The length of the password must be between 8-20.","Invalid password",JOptionPane.ERROR_MESSAGE);
                            passwordRegisterField.setText("");
                            passwordConfirmField.setText("");
                            passwordRegisterPanel.repaint();
                            confirmPasswordPanel.repaint();
                            return;
                        }
                        if(!passwordInput.equals(passwordConfirm)){
                            JOptionPane.showMessageDialog(frame,"The two passwords you entered are not identical.","Invalid password",JOptionPane.ERROR_MESSAGE);
                            passwordRegisterField.setText("");
                            passwordConfirmField.setText("");
                            passwordRegisterPanel.repaint();
                            confirmPasswordPanel.repaint();
                            return;
                        }


                        try{

                            // encode the password
//                            byte[] bytesOfString=passwordInput.getBytes(StandardCharsets.UTF_8);
//                            MessageDigest md=MessageDigest.getInstance("MD5");
//                            byte[] digestRes=md.digest(bytesOfString);
//                            System.out.println("md5:"+new String(digestRes,StandardCharsets.UTF_8));
                            // send register request

                            JSONObject res=requester.register(usernameInput,DigestUtils.md5Hex(passwordInput));
                            if(!res.getBoolean("success")){
                                JOptionPane.showMessageDialog(frame,res.get("message").toString(),"Register failed",JOptionPane.INFORMATION_MESSAGE);
                                usernameRegisterTextField.setText("");
                                passwordRegisterField.setText("");
                                passwordConfirmField.setText("");
                                registerPanel.repaint();
                            }
                            else{
                                JOptionPane.showMessageDialog(frame,"Register success","Register success",JOptionPane.INFORMATION_MESSAGE);
                                frame.remove(registerPanel);
                                frame.add(loginPanel);
                                usernameField.setText(usernameInput);
                                loginPanel.repaint();
                            }
                        }catch (Exception exception){
                            JOptionPane.showMessageDialog(frame, "Connection failed.", "connection failed", JOptionPane.ERROR_MESSAGE);
                            exception.printStackTrace();
                        }
                    }
                });
                JButton returnBtn= new JButton("Return");
                returnBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        frame.remove(registerPanel);
                        frame.add(loginPanel);
                        frame.repaint();
                    }
                });
                registerSubmitPanel.add(registerSubmitBtn);
                registerSubmitPanel.add(returnBtn);
                registerPanel.add(usernameRegisterPanel);
                registerPanel.add(usernameTipPanel);
                registerPanel.add(passwordRegisterPanel);
                registerPanel.add(passwordTipPanel);
                registerPanel.add(confirmPasswordPanel);
                registerPanel.add(registerSubmitPanel);
                frame.getContentPane().add(registerPanel);
                frame.repaint();
                frame.setVisible(true);
            }
        });
        panel04.add(new JLabel("Not registered?"));
        panel04.add(registerPageBtn);
        loginPanel.add(usernamePanel);
        loginPanel.add(passwordPanel);
        loginPanel.add(panel03);
        loginPanel.add(panel04);
        frame.getContentPane().add(loginPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        //JFrame mainFrame=new MainFrame(new UserInfo("user",100,100),new ServerRequester(null));

    }

    //initializes the state of the application, recover the data.
    public static void init(MainThread mainThread){
        Thread mainT=new Thread(mainThread);
        mainT.start();
    }


}