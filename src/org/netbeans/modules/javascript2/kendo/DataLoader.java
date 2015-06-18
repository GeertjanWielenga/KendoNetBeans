package org.netbeans.modules.javascript2.kendo;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.markdown4j.Markdown4jProcessor;
import org.openide.util.Exceptions;

/**
 *
 * @author Geertjan Wielenga
 */
public class DataLoader {

    private static final Logger LOGGER = Logger.getLogger(DataLoader.class.getName());

    private static Map<String, Collection<KendoUIDataItem>> result = new HashMap<String, Collection<KendoUIDataItem>>();

    private static HTMLDocument htmlDoc = new HTMLDocument();

    public static Map<String, Collection<KendoUIDataItem>> getData(List<File> files) {
        result.clear();
        for (File file : files) {
            try {
                String html = new Markdown4jProcessor().process(file);
                try {
                    long start = System.currentTimeMillis();
                    HTMLEditorKit.Parser parser = new HTMLParse().getParser();
                    htmlDoc.setParser(parser);
                    parser.parse(new StringReader(html), new HTMLParseLister(html), true);
                    long end = System.currentTimeMillis();
                    LOGGER.log(Level.FINE, "Loading data from file took {0}ms ", (end - start)); //NOI18N
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return result;
    }

    static class HTMLParseLister extends HTMLEditorKit.ParserCallback {

        private boolean inHeader = false;
        int startPos = -1;
        private int level = 0;
        private final String html;
        private static String lineSeparator
                = System.getProperty("line.separator", "\r\n");
        private char[] text;

        private HTMLParseLister(String html) {
            this.html = html;
        }

        @Override
        public void handleStartTag(HTML.Tag tag,
                MutableAttributeSet attributes, int position) {
            int newLevel = 0;
            if (tag == HTML.Tag.H1) {
                newLevel = 1;
            } else if (tag == HTML.Tag.H2) {
                newLevel = 2;
            } else if (tag == HTML.Tag.H3) {
                newLevel = 3;
            } else if (tag == HTML.Tag.H4) {
                newLevel = 4;
            } else if (tag == HTML.Tag.H5) {
                newLevel = 5;
            } else if (tag == HTML.Tag.H6) {
                newLevel = 6;
            } else if (tag == HTML.Tag.PRE) {
                newLevel = 7;
            } else {
                return;
            }
            this.inHeader = true;
            this.level = newLevel;
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int position) {
            if (tag == HTML.Tag.H1 || tag == HTML.Tag.H2
                    || tag == HTML.Tag.H3 || tag == HTML.Tag.H4
                    || tag == HTML.Tag.H5 || tag == HTML.Tag.H6 || tag == HTML.Tag.PRE) {
                inHeader = false;
            }
        }

        Set<KendoUIDataItem> items = new HashSet<KendoUIDataItem>();
        String componentName = null;
        String attributeName = null;
        String explanation = null;
        String code = null;

        @Override
        public void handleText(char[] text, int position) {
            this.text = text;
            if (inHeader && level == 1) {
                componentName = new String(text).replace(".ui.", "");
            }
            if (inHeader && level == 3) {
                attributeName = new String(text);
            }
            if (level == 4) {
                explanation = "<h3>"+new String(text)+"</h3>";
            }
            if (level == 7) {
                code = escapeHTML(new String(text));
                KendoUIDataItem item = new KendoUIDataItem(attributeName, null, 
                        explanation+"<p>"+code, null);
                if (!items.contains(item)
                        && attributeName != null
                        && !attributeName.startsWith("(")
                        && !Character.isUpperCase(attributeName.charAt(0))) {
                    items.add(item);
                }
            }
            result.put(componentName, items);
        }

        //http://stackoverflow.com/questions/9580684/how-to-retrieve-title-of-a-html-with-the-help-of-htmleditorkit
        public static String escapeHTML(String s) {
            StringBuilder out = new StringBuilder(Math.max(16, s.length()));
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                    out.append("&#");
                    out.append((int) c);
                    out.append(';');
                } else {
                    out.append(c);
                }
            }
            String addBreak = out.toString().replace("\n", "<br />\n");;
            return addBreak;
        }

    }

    public static class HTMLParse extends HTMLEditorKit {
        @Override
        public HTMLEditorKit.Parser getParser() {
            return super.getParser();
        }
    }

}
