package org.netbeans.modules.javascript2.kendo;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public class DataLoader {

    private static final Set<KendoDataItem> result = new HashSet<KendoDataItem>();
    //Title:
    private static final String titleExpression = "title: [A-Za-z]+";
    private static final Pattern titlePattern = Pattern.compile(titleExpression);
    //Attribute:
    private static final String attributeExpression = "(\\n###\\s)([A-Za-z]+)(\\s)";
    private static final Pattern attributePattern = Pattern.compile(attributeExpression, Pattern.DOTALL);

    public static Set<KendoDataItem> getData(List<File> files, int type) {
        result.clear();
        for (File file : files) {
            try {
                String fileContent = FileUtil.toFileObject(file).asText();
                Matcher titleMatcher = titlePattern.matcher(fileContent);
                Matcher attributeMatcher = attributePattern.matcher(fileContent);
                //Finders:
                //http://stackoverflow.com/questions/5516119/regular-expression-to-match-characters-at-beginning-of-line-only
                if (titleMatcher.find()) {
                    String formattedTitle = titleMatcher.group().replace("title: ", "kendo");
                    if (type == 1) {
                        result.add(new KendoDataItem(null, formattedTitle, null, escapeHTML(fileContent), null));
                    } else if (type == 2) {
                        boolean matches = attributeMatcher.find();
                        if (matches) {
                            do {
                                int start = attributeMatcher.start();

                                String attributeName = attributeMatcher.group(2);
                                matches = attributeMatcher.find();

                                int length = matches ? attributeMatcher.start() - start : fileContent.length() - start;

                                String attributeDescription = fileContent.substring(start + 1, start + length);
                                result.add(new KendoDataItem(formattedTitle, attributeName, null, attributeDescription, null));
                            } while (matches);
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return result;
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
