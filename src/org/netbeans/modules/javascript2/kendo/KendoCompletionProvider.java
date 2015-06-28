package org.netbeans.modules.javascript2.kendo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

@CompletionProvider.Registration(priority = 20)
public class KendoCompletionProvider implements CompletionProvider {

    private static final List<File> demoPropertyFiles = new ArrayList<File>();

    private static synchronized List<File> getDataFiles() {
        File demoDataFolder = InstalledFileLocator.getDefault().locate(
                "docs/api/javascript/ui",
                "org.netbeans.modules.javascript2.kendo",
                false);
        for (FileObject fo : FileUtil.toFileObject(demoDataFolder).getChildren()) {
            String name = fo.getNameExt();
            demoPropertyFiles.add((InstalledFileLocator.getDefault().locate(
                    "docs/api/javascript/ui/" + name,
                    "org.netbeans.modules.javascript2.kendo",
                    false)));
        }
        return demoPropertyFiles;
    }

    private synchronized static Set<KendoDataItem> getComponents() {
        return DataLoader.getData(getDataFiles(), 1);
    }

    private synchronized static Set<KendoDataItem> getAttributes() {
        return DataLoader.getData(getDataFiles(), 2);
    }

    @Override
    public List<CompletionProposal> complete(
            CodeCompletionContext ccContext,
            CompletionContext jsCompletionContext,
            String prefix) {
        List<CompletionProposal> result = new ArrayList<CompletionProposal>();
        if (jsCompletionContext == CompletionContext.OBJECT_PROPERTY) {
            int caretOffset = ccContext.getCaretOffset();
            Set<KendoDataItem> components = getComponents();
            for (KendoDataItem item : components) {
                if (item.getName().startsWith(prefix)) {
                    result.add(KendoCompletionProposal.createDemoItem(item, caretOffset, prefix));
                }
            }
        } else if (jsCompletionContext == CompletionContext.OBJECT_PROPERTY_NAME) {
            TokenHierarchy<?> th = ccContext.getParserResult().getSnapshot().getTokenHierarchy();
            if (th == null) {
                return Collections.EMPTY_LIST;
            }
            int carretOffset = ccContext.getCaretOffset();
            int eOffset = ccContext.getParserResult().getSnapshot().getEmbeddedOffset(carretOffset);
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, eOffset);
            if (ts == null) {
                return Collections.EMPTY_LIST;
            }
            ts.move(eOffset);
            if (!ts.moveNext() && !ts.movePrevious()) {
                return Collections.EMPTY_LIST;
            }
            Token<? extends JsTokenId> token = null;
            JsTokenId tokenId;
            //find the begining of the object literal
            int balance = 1;
            while (ts.movePrevious() && balance > 0) {
                token = ts.token();
                tokenId = token.id();
                if (tokenId == JsTokenId.BRACKET_RIGHT_CURLY) {
                    balance++;
                } else if (tokenId == JsTokenId.BRACKET_LEFT_CURLY) {
                    balance--;
                }
            }
            if (token == null || balance != 0) {
                return Collections.EMPTY_LIST;
            }
            // now we should be at the beginning of the object literal. 
            token = LexUtilities.findPreviousToken(ts, Arrays.asList(JsTokenId.IDENTIFIER));
            tokenId = token.id();
            StringBuilder sb = new StringBuilder(token.text());
            while ((tokenId == JsTokenId.IDENTIFIER || tokenId == JsTokenId.OPERATOR_DOT) && ts.movePrevious()) {
                token = ts.token();
                tokenId = token.id();
                if (tokenId == JsTokenId.IDENTIFIER) {
                    sb.insert(0, token.text());
                }
            }
            String currentComponent = sb.toString();
            Set<KendoDataItem> attributes = getAttributes();
            int caretOffset = ccContext.getCaretOffset();
            for (KendoDataItem attribute : attributes) {
                if (attribute.getParent().equals(currentComponent)) {
                    result.add(KendoCompletionProposal.createDemoItem(attribute, caretOffset, prefix));
                }
            }
        } else {
            return Collections.EMPTY_LIST;
        }
        return result;
    }

    @Override
    public String getHelpDocumentation(
            ParserResult pr,
            ElementHandle eh) {
        if (eh != null && eh instanceof KendoElementHandle) {
            return ((KendoElementHandle) eh).getDocumentation();
        }
        return null;
    }

}
