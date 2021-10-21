package tokyo.northside.omegat.oxford;

import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.CredentialsManager;

import java.awt.*;

public class OxfordPreferencesController extends BasePreferencesController {

    private static final String OPTION_OXFORD_APPID = "dictionary_oxford_appid";
    private static final String OPTION_OXFORD_APPKEY = "dictionary_oxford_appkey";
    private OxfordOptionsPanel panel;

    static String getAppId() {
        return getCredential(OPTION_OXFORD_APPID);
    }

    static String getAppKey() {
        return getCredential(OPTION_OXFORD_APPKEY);
    }

    /**
     * Retrieve a credential with the given ID. First checks temporary system properties, then falls back to
     * the program's persistent preferences. Store a credential with
     * {@link #setCredential(String, String)}.
     *
     * @param id ID or key of the credential to retrieve
     * @return the credential value in plain text
     */
     protected static String getCredential(String id) {
        String property = System.getProperty(id);
        if (property != null) {
            return property;
        }
        return CredentialsManager.getInstance().retrieve(id).orElse("");
    }

    /**
     * Store a credential. Credentials are stored in temporary system properties
     * and in the program's persistent preferences encoded in
     * Base64. Retrieve a credential with {@link #getCredential(String)}.
     *
     * @param id        ID or key of the credential to store
     * @param value     value of the credential to store
     */
     protected static void setCredential(String id, String value) {
        System.setProperty(id, value);
        CredentialsManager.getInstance().store(id, value);
    }

    @Override
    protected void initFromPrefs() {
        panel.appIdField.setText(getCredential(OPTION_OXFORD_APPID));
        panel.appKeyField.setText(getCredential(OPTION_OXFORD_APPKEY));
    }

    @Override
    public String toString() {
        return "Oxford Dictionaries API";
    }

    @Override
    public Component getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public void persist() {
        setCredential(OPTION_OXFORD_APPID, panel.appIdField.getText());
        setCredential(OPTION_OXFORD_APPKEY, panel.appKeyField.getText());
    }

    @Override
    public void restoreDefaults() {
    }

    private void initGui() {
        panel = new OxfordOptionsPanel();
    }
}
