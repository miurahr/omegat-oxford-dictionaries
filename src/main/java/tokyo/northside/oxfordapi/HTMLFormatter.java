package tokyo.northside.oxfordapi;

import org.omegat.core.dictionaries.DictionaryEntry;
import tokyo.northside.oxfordapi.dtd.*;

import java.util.List;

public class HTMLFormatter {

    public static DictionaryEntry formatTranslations(final LexicalEntry lexicalEntry) {
        String title = lexicalEntry.getText();
        StringBuilder sb = new StringBuilder("<ol>");
        for (Entry entry : lexicalEntry.getEntries()) {
            for (Sense sense : entry.getSenses()) {
                if (sense.getTranslations() == null) continue;
                for (Translation translation : sense.getTranslations()) {
                    sb.append("<li>").append(translation.getText()).append("</li>");
                }
            }
        }
        sb.append("</ol>");
        return new DictionaryEntry(title, sb.toString());
    }

    public static DictionaryEntry formatDefinitions(final LexicalEntry lexicalEntry) {
        String title = lexicalEntry.getText();
        String category = lexicalEntry.getLexicalCategory().getText();
        StringBuilder sb = new StringBuilder("[").append(category).append("]&nbsp;");
        for (Entry entry : lexicalEntry.getEntries()) {
            List<Pronunciation> pronunciations = entry.getPronunciations();
            if (pronunciations != null) {
                sb.append("<span>");
                for (Pronunciation pron: pronunciations) {
                    if (pron.getAudioFile() != null) {
                        sb.append("<a href=\"").append(pron.getAudioFile()).append("\">");
                    }
                    sb.append("[").append(pron.getPhoneticSpelling()).append("]");
                    if (pron.getAudioFile() != null) {
                        sb.append("</a>");
                    }
                }
                sb.append("</span>&nbsp;");
            }
            List<String> etymologies = entry.getEtymologies();
            if (etymologies != null) {
                sb.append("<span>");
                for (String etymology : etymologies) {
                    sb.append(etymology);
                }
                sb.append("</span>");
            }
            sb.append("<ol>");
            for (Sense sense : entry.getSenses()) {
                if (sense.getDefinitions() == null) continue;
                for (String text : sense.getDefinitions()) {
                    sb.append("<li>").append(text).append("</li>");
                }

                List<Example> examples = sense.getExamples();
                if (examples != null) {
                    sb.append("<ul>");
                    for (Example ex : examples) {
                        sb.append("<li>").append(ex.getText()).append("</li>");
                    }
                    sb.append("</ul>");
                }
            }
            sb.append("</ol>");
        }
        return new DictionaryEntry(title, sb.toString());
    }
}
