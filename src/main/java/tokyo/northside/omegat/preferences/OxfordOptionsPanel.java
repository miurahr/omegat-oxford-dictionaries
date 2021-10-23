package tokyo.northside.omegat.preferences;

import javafx.scene.control.CheckBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OxfordOptionsPanel extends JPanel {
    JTextField appIdField;
    JTextField appKeyField;
    JCheckBox enableOption;
    JCheckBox queryMonolingual;
    JCheckBox queryBilingual;

    public OxfordOptionsPanel() {
        initGui();
        enableOption.addActionListener(e -> {
            appIdField.setEnabled(enableOption.isSelected());
            appKeyField.setEnabled(enableOption.isSelected());
        });
        appIdField.setInputVerifier(new OxfordInputVerifier());
        appKeyField.setInputVerifier(new OxfordInputVerifier());
    }

    class OxfordInputVerifier extends javax.swing.InputVerifier implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextField field = (JTextField) e.getSource();
            shouldYieldFocus(field);
            field.selectAll();
        }

        @Override
        public boolean verify(JComponent input) {
            if (input == appIdField) {
                String str = appIdField.getText();
                appIdField.setText(str.replaceAll("\\s|\\t", ""));
                return true;
            } else if (input == appKeyField) {
                String str = appKeyField.getText();
                appKeyField.setText(str.replaceAll("\\s|\\t", ""));
                return true;
            } else {
                return true;
            }
        }
    }

    private void initGui() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        JPanel cbPanel = new JPanel();
        JPanel idPanel = new JPanel();
        JPanel keyPanel = new JPanel();
        JPanel queryPanel = new JPanel();
        enableOption = new JCheckBox("Enable Oxford Dictionaries");
        queryMonolingual = new JCheckBox("Query monolingual dicitionary");
        queryBilingual = new JCheckBox("Query bilingual dictionary");
        appIdField = new JTextField();
        appKeyField = new JTextField();
        appIdField.setPreferredSize(new Dimension(300,30));
        appKeyField.setPreferredSize(new Dimension(300,30));
        appIdField.setHorizontalAlignment(JTextField.LEFT);
        appKeyField.setHorizontalAlignment(JTextField.LEFT);
        JLabel appIdLabel = new JLabel();
        JLabel appKeyLabel = new JLabel();
        appIdLabel.setText("OD API App ID : ");
        appKeyLabel.setText("OD API AppKey : ");
        cbPanel.add(enableOption);
        cbPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        idPanel.add(appIdLabel);
        idPanel.add(appIdField);
        idPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        keyPanel.add(appKeyLabel);
        keyPanel.add(appKeyField);
        keyPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.PAGE_AXIS));
        queryPanel.add(queryMonolingual);
        queryPanel.add(queryBilingual);
        queryPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        add(cbPanel);
        add(idPanel);
        add(keyPanel);
        add(queryPanel);
    }
}
