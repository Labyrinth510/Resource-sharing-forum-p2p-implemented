package gui;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import filetransmission.MainThread;
import usage.ResourceInfo;
import usage.TaskInfo;
import usage.UserInfo;
import web.ServerRequester;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class SearchPanel extends JPanel{
    private ServerRequester requester;
    private JTable tableOfRes;
    private JTableHeader tableHeader;
    private JScrollPane searchResScrollPane;
    private JPanel searchPanel02;
    private Object[][] fileListData;
    private UserInfo userInfo;
    private MainThread mainThread;
    private String downloadPath;
    private JTextField locationField;

    public SearchPanel(ServerRequester requester, UserInfo userinfo, MainThread mainThread){
        this.requester=requester;
        this.userInfo=userinfo;
        this.mainThread=mainThread;

        this.setLayout(new BorderLayout());

        // the search text bar and search button
        JPanel searchPanel01=new JPanel();
        JTextField searchTextField=new JTextField(30);
        JButton searchBtn=new JButton("Search");
        //when search button is clicked
        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //search content
                String searchString=searchTextField.getText();
                JSONObject result;

                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                // !!! lack regular expression check !!!
                Pattern pattern=Pattern.compile("^(([\\w\\.])+,)|((\\w+,\\w+)(,\\w)*)$");
                if(!pattern.matcher(searchTextField.getText()).find()){
                    JOptionPane.showMessageDialog(SearchPanel.this,"Illegal search string input!", "Warning", JOptionPane.ERROR_MESSAGE);
                }
                if(searchString.equals("")){
                    JOptionPane.showMessageDialog(SearchPanel.this,"Please enter valid string to search.","Warning",JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if(searchString.indexOf(",")==searchString.length()-1){
                    try {
                        result = requester.searchbyname(searchString.substring(0, searchString.length() - 1));
                        updateResultList(result);
                    }catch (Exception exception){
                        exception.printStackTrace();
                    }
                }else{
                    try{
                        result=requester.searchbytags(searchString.split(","));
                        updateResultList(result);
                    }catch (Exception exception){
                        exception.printStackTrace();
                    }
                }
            }
        });
        searchPanel01.add(searchTextField);
        searchPanel01.add(searchBtn);
        this.add(searchPanel01,BorderLayout.NORTH);

        // the search result list
        searchPanel02=new JPanel();
        searchResScrollPane=new JScrollPane();
        tableOfRes=new JTable();
        searchResScrollPane.add(tableOfRes);
        searchPanel02.add(searchResScrollPane);
        this.add(searchPanel02,BorderLayout.CENTER);

        JPanel downloadPart=new JPanel(new GridLayout(2,1));

        // select download path
        // select file part
        JPanel selectLocationPanel=new JPanel();
        selectLocationPanel.add(new JLabel("Download Location: "));
        locationField=new JTextField(30);
        locationField.setEditable(false);
        locationField.setText("select a path...");
        JButton selectFileButton=new JButton("Select Download Location");
        selectFileButton.addActionListener(e -> {
            JFileChooser fileChooser=new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.showOpenDialog(null);
            if(fileChooser.getSelectedFile()!=null)
                downloadPath =fileChooser.getSelectedFile().getAbsolutePath();
            if(downloadPath !=null){
                locationField.setText(downloadPath);
                this.repaint();
            }
        });
        selectLocationPanel.add(locationField);
        selectLocationPanel.add(selectFileButton);
        downloadPart.add(selectLocationPanel);

        // the download button
        JPanel searchPanel03=new JPanel();
        JButton downloadBtn=new JButton("Download");

        // when click the download button
        downloadBtn.addActionListener(e->{

            // when one of the result list is selected
            if(tableOfRes.getSelectedRow()!=-1){
                try{
                    Object[] selectedRow=fileListData[tableOfRes.getSelectedRow()];
                    int fileID=Integer.parseInt(selectedRow[0].toString());
                    String fileName=selectedRow[1].toString();

                    JSONObject res=requester.download(fileID, userInfo.getUsername());

                    //!!! download response not success, show response message
                    if(!res.getBoolean("success")){

                        JOptionPane.showMessageDialog(this,res.get("message").toString(),"Download request failed",JOptionPane.INFORMATION_MESSAGE);

                    }else{
                        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        //!!! add to select download path
//                        DownloadPathSelectionFrame downloadPathSelectionFrame=new DownloadPathSelectionFrame(this);
//                        downloadPathSelectionFrame.setVisible(true);

                        if(downloadPath!=null){
                            JOptionPane.showMessageDialog(this,"Add to task successfully","Result",JOptionPane.INFORMATION_MESSAGE);

                            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                            // !!!!parse JSPONArray to arraylist !!!!
                            ArrayList<String> hashList=new ArrayList<String>();
                            hashList=(ArrayList<String>) res.getJSONObject("data").getJSONArray("hashList").toJavaList(String.class);
                            mainThread.newFileThread(fileID, downloadPath+ File.separator+fileName,
                                    Long.parseLong(selectedRow[7].toString()), //file size
                                    hashList);
//                            mainThread.startFile(fileID);
                        }else{
                            JOptionPane.showMessageDialog(this, "Not select the download location","Warning",JOptionPane.WARNING_MESSAGE);
                        }

                    }



                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        });
        searchPanel03.add(downloadBtn);
        downloadPart.add(searchPanel03);

        this.add(downloadPart,BorderLayout.SOUTH);
        this.repaint();
    }

    public void updateResultList(JSONObject result){
        if (!result.getBoolean("success")) {
            JOptionPane.showMessageDialog(SearchPanel.this, "No files satisfying the condition were found.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        } else {
            searchPanel02.remove(searchResScrollPane);
            JSONArray filesRes = result.getJSONArray("data");
            ResourceInfo[] listOfFiles = new ResourceInfo[filesRes.size()];
            for (int i = 0; i < filesRes.size(); i++) {
                listOfFiles[i] = JSONObject.parseObject(JSON.toJSONString(filesRes.getJSONObject(i)), ResourceInfo.class);
            }
            fileListData = new Object[filesRes.size()][8];
            for (int i = 0; i < filesRes.size(); i++) {
                fileListData[i][0] = Integer.toString(listOfFiles[i].getF_id());
                fileListData[i][1] = listOfFiles[i].getFilename();
                fileListData[i][2] = TaskInfo.toDisplaySize(listOfFiles[i].getFileSize());
                int loopCount = Math.min(listOfFiles[i].getTags().length, 5);
                String targetStr = "";
                int j;
                for (j = 0; j < loopCount - 1; j++) {
                    targetStr += listOfFiles[i].getTags()[j];
                    targetStr += ",";
                }
                targetStr += listOfFiles[i].getTags()[j];
                fileListData[i][3] = targetStr;
                fileListData[i][4] = Integer.toString(listOfFiles[i].getLowestPrestige());
                fileListData[i][5] = Integer.toString(listOfFiles[i].getPointsRequiredPerUnit());
                fileListData[i][6] = listOfFiles[i].getGroupMemberNumber();
                fileListData[i][7] = listOfFiles[i].getFileSize();
            }
            String[] titles = {"F_id", "Name", "Size", "Tags", "Prestige Required", "Point Required per MB", "Number of group member","Detailed Size"};
            //给table重新赋值
            tableOfRes = new JTable(fileListData, titles){
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            //not allow select col, only allow select one row
            tableOfRes.setColumnSelectionAllowed(false);
            tableOfRes.setSelectionMode(SINGLE_SELECTION);

            //hide the column "fid".
            TableColumn tableColumn=tableOfRes.getColumnModel().getColumn(0);
            tableColumn.setMaxWidth(0);
            tableColumn.setMinWidth(0);
            tableOfRes.getTableHeader().getColumnModel().getColumn(0).setMaxWidth(0);
            tableOfRes.getTableHeader().getColumnModel().getColumn(0).setMinWidth(0);

            //hide the column "detailed size".
            TableColumn sizeColumn=tableOfRes.getColumnModel().getColumn(7);
            sizeColumn.setMaxWidth(0);
            sizeColumn.setMinWidth(0);
            tableOfRes.getTableHeader().getColumnModel().getColumn(7).setMaxWidth(0);
            tableOfRes.getTableHeader().getColumnModel().getColumn(7).setMinWidth(0);

            tableHeader = tableOfRes.getTableHeader();
            tableHeader.setReorderingAllowed(false);
            searchResScrollPane.getViewport().add(tableOfRes);
            searchPanel02.add(searchResScrollPane);
            searchPanel02.revalidate();
            searchPanel02.repaint();

        }
    }
    public void setDownloadPath(String downloadPath){
        this.downloadPath=downloadPath;
    }
}