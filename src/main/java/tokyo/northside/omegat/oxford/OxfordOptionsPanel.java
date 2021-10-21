package tokyo.northside.omegat.oxford;

import javax.swing.*;
import java.awt.*;

public class OxfordOptionsPanel extends JPanel {
    JTextField appIdField;
    JTextField appKeyField;

    public OxfordOptionsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        JPanel idPanel = new JPanel();
        JPanel keyPanel = new JPanel();
        appIdField = new JTextField();
        appKeyField = new JTextField();
        appIdField.setPreferredSize(new Dimension(250,24));
        appKeyField.setPreferredSize(new Dimension(250,24));
        JLabel appIdLabel = new JLabel();
        JLabel appKeyLabel = new JLabel();
        appIdLabel.setText("OD API App ID : ");
        appKeyLabel.setText("OD API AppKey : ");
        idPanel.add(appIdLabel);
        idPanel.add(appIdField);
        keyPanel.add(appKeyLabel);
        keyPanel.add(appKeyField);
        add(idPanel);
        add(keyPanel);
    }
}
