package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class DownloadPathSelectionFrame extends JFrame {
    private File filepath;
    private SearchPanel searchPanel;
    public DownloadPathSelectionFrame(SearchPanel searchPanel){
        super();
        this.searchPanel=searchPanel;
        this.setLocationRelativeTo(null);
        this.setSize(100,100);
        JPanel selectionPanel=new JPanel();
        selectionPanel.add(new JLabel("Download Path: "));
        JTextField pathField=new JTextField("Select a path...",20);
        selectionPanel.add(pathField);
        JButton pathSelectBtn=new JButton("Select");
        pathSelectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser=new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showOpenDialog(null);
                if(fileChooser.getSelectedFile()!=null){
                    DownloadPathSelectionFrame.this.filepath=fileChooser.getSelectedFile();
                    pathField.setText(DownloadPathSelectionFrame.this.filepath.getAbsolutePath());
                    DownloadPathSelectionFrame.this.repaint();
                }
            }
        });
        selectionPanel.add(pathSelectBtn);

        JButton confirmBtn=new JButton("Confirm");
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(DownloadPathSelectionFrame.this.filepath==null){
                    JOptionPane.showMessageDialog(DownloadPathSelectionFrame.this,"Please select a valid download path.","Warning",JOptionPane.WARNING_MESSAGE);
                    return;
                }
                searchPanel.setDownloadPath(DownloadPathSelectionFrame.this.filepath.getAbsolutePath());
                DownloadPathSelectionFrame.this.dispose();
            }
        });
        JPanel confirmPanel=new JPanel();
        confirmPanel.add(confirmBtn);
        this.add(selectionPanel);
        this.add(confirmPanel);
        this.repaint();
    }
}
