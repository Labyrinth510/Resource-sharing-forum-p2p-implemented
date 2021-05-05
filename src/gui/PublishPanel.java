package gui;

import com.alibaba.fastjson.JSONObject;
import filetransmission.MainThread;
import usage.UserInfo;
import web.ServerRequester;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class PublishPanel extends JPanel {
    private ServerRequester requester;

    private JLabel fileSizeLabel;
    private JTextField fileField,tagsField;
    private UserInfo userInfo;
    private File file;
    private long fileSize;
    private MainThread mainThread;

    public PublishPanel(ServerRequester requester, UserInfo userInfo, MainThread mainThread){
        super();
        this.userInfo=userInfo;
        this.mainThread=mainThread;

        this.setLayout(new GridLayout(6, 1));

        // select file part
        JPanel pubPanel01=new JPanel();
        pubPanel01.add(new JLabel("File: "));
        fileField=new JTextField(30);
        fileField.setEditable(false);
        fileField.setText("select a file...");
        JButton selectFileButton=new JButton("Select file");
        selectFileButton.addActionListener(e -> {
            JFileChooser fileChooser=new JFileChooser();
            fileChooser.showOpenDialog(null);
            file =fileChooser.getSelectedFile();
            if(file !=null){
                fileField.setText(file.getAbsolutePath());
                fileSize= file.length();
                fileSizeLabel.setText(String.format("%.2f",(file.length()/1000000.0))+" MB");
                this.repaint();
            }
        });
        pubPanel01.add(fileField);
        pubPanel01.add(selectFileButton);

        // illustrate the file size
        JPanel pubPanel02=new JPanel();
        pubPanel02.add(new JLabel("File size:"));
        fileSizeLabel=new JLabel();
        fileSizeLabel.setText("");
        pubPanel02.add(this.fileSizeLabel);

        // inputs the tags part
        JPanel pubPanel03=new JPanel();
        pubPanel03.add(new JLabel("Tags: "));
        tagsField=new JTextField(40);
        tagsField.setText("Please enter tags here, each separated by a comma.");
        tagsField.addFocusListener(new FocusListener(){
            @Override
            public void focusGained(FocusEvent arg0){
                if(tagsField.getText().equals("Please enter tags here, each separated by a comma.")){
                    tagsField.setText("");
                    PublishPanel.this.repaint();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });
        pubPanel03.add(tagsField);

        // input the lowest prestige part
        JPanel pubPanel04=new JPanel();
        pubPanel04.add(new JLabel("Lowest prestige required: "));
        JTextField lowestPrestigeTextField=new JTextField(20);
        pubPanel04.add(lowestPrestigeTextField);

        // input the points per MB part
        JPanel pubPanel05=new JPanel();
        pubPanel05.add(new JLabel("Points required per MB:"));
        JTextField pointsRequiredField=new JTextField(20);
        pubPanel05.add(pointsRequiredField);

        // submit part
        JPanel pubPanel06=new JPanel();
        JButton publishButton=new JButton("submit");
        publishButton.addActionListener(e -> {

            // if file is not select
            if(fileField.getText().equals("select a file...")){
                JOptionPane.showMessageDialog(this,"Please select a file.","Warning",JOptionPane.WARNING_MESSAGE);
                return;
            }

            // tag should be separated by , space between tags is allowed
            //Pattern pattern=Pattern.compile("^\\w((\\s)*(\\w)+(\\s)*,(\\s)*)*(\\w)*$");
            Pattern pattern=Pattern.compile("^\\w((\\w)+,)*$");
            if(!pattern.matcher(tagsField.getText()).find()){
                JOptionPane.showMessageDialog(this,"Illegal tags input\ntag should be comprise of letters or digits\ntags should be separated by comma(,) space is not allowed",
                        "Warning",JOptionPane.WARNING_MESSAGE);
                return;
            }
            pattern=Pattern.compile("^[0-9]+$");
            if(!pattern.matcher(lowestPrestigeTextField.getText()).find()||!pattern.matcher(pointsRequiredField.getText()).find()) {
            	JOptionPane.showMessageDialog(this, "Illegal points or prestige value input,\n they must be nonnegative integers!","Warning",JOptionPane.WARNING_MESSAGE);
            }

            ArrayList<String> hashList=filetransmission.Verification.calculateHashList(file);
            JSONObject res=requester.publish(userInfo.getUsername(),
                            usage.TaskInfo.parseFileName(fileField.getText()),
                            new ArrayList<String>(Arrays.asList(tagsField.getText().split(","))),
                            Integer.parseInt(lowestPrestigeTextField.getText()),
                            Integer.parseInt(pointsRequiredField.getText()),
                            this.fileSize,
                            hashList);

            //!!! response to the publish
            if(!res.getBoolean("success")){
                JOptionPane.showMessageDialog(this,"Publish failed!","Warning",JOptionPane.WARNING_MESSAGE);

            }else{
                // publish success
                JOptionPane.showMessageDialog(this, "Publish successful!", "Publish result",JOptionPane.INFORMATION_MESSAGE);
                mainThread.newFileThread(res.getIntValue("fid"), file.getPath(), file.length(),hashList);
            }
        });
        pubPanel06.add(publishButton);

        this.add(pubPanel01);
        this.add(pubPanel02);
        this.add(pubPanel03);
        this.add(pubPanel04);
        this.add(pubPanel05);
        this.add(pubPanel06);
    }
}
