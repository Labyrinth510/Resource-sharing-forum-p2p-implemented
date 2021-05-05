package gui;

import usage.UserInfo;

import javax.swing.*;
import java.awt.*;

public class ProfilePanel extends JPanel {

    private JLabel usernameLabel,prestigeLabel,pointLabel;

    public ProfilePanel(UserInfo userInfo){
        super();

        this.setLayout(new GridLayout(3,1,20,20));

        usernameLabel=new JLabel(userInfo.getUsername());
        prestigeLabel=new JLabel(Integer.toString(userInfo.getPrestigeValue()));
        pointLabel=new JLabel(Integer.toString(userInfo.getPoint()));

        JPanel panel01=new JPanel();
        JPanel panel02=new JPanel();
        JPanel panel03=new JPanel();

        JPanel panel001=new JPanel();
        JPanel panel002=new JPanel();
        JPanel panel003=new JPanel();
        panel001.add(new JLabel("Username: "));
        panel001.add(usernameLabel);
        panel002.add(new JLabel("Prestige value: "));
        panel002.add(prestigeLabel);
        panel003.add(new JLabel("Point value: "));
        panel003.add(pointLabel);
        this.add(panel01);
        this.add(panel02);
        this.add(panel03);
        panel02.setLayout(new GridLayout(3,1,20,20));
        panel02.add(panel001);
        panel02.add(panel002);
        panel02.add(panel003);


    }

    public void updatePanel(UserInfo userInfo){
        prestigeLabel.setText(Integer.toString(userInfo.getPrestigeValue()));
        pointLabel.setText(Integer.toString(userInfo.getPoint()));
        this.repaint();
    }
}
