package org.netbeans.modules.javascript2.kendo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Petr Pisl and Geertjan Wielenga
 */
@CompletionProvider.Registration(priority = 21)
public class KendoUICodeCompletion implements CompletionProvider {

    private static final List<File> kendoPropertyFiles = new ArrayList<File>();

    private static synchronized List<File> getDataFiles() {
        File kendoUIFolder = InstalledFileLocator.getDefault().locate(
                "docs/api/javascript/ui",
                "org.netbeans.modules.javascript2.kendo",
                false);
        for (FileObject fo : FileUtil.toFileObject(kendoUIFolder).getChildren()) {
            String name = fo.getNameExt();
            kendoPropertyFiles.add((InstalledFileLocator.getDefault().locate(
                    "docs/api/javascript/ui/" + name,
                    "org.netbeans.modules.javascript2.kendo",
                    false))); //NOI18N
        }
        return kendoPropertyFiles;
    }

    private static HashMap<String, Collection<KendoUIDataItem>> ccData = null;

    private synchronized static Map<String, Collection<KendoUIDataItem>> getData() {
        return DataLoader.getData(getDataFiles());
    }

    @Override
    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        if (jsCompletionContext == CompletionContext.OBJECT_PROPERTY_NAME) {
            // find the object that can be configured
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
                if (tokenId == JsTokenId.OPERATOR_DOT) {
                    sb.insert(0, '.'); // NOI18N
                } else if (tokenId == JsTokenId.IDENTIFIER) {
                    sb.insert(0, token.text());
                }
            }

            String fqnWithDot = sb.toString();
            String fqn = fqnWithDot.substring(1).replace(".ui.", "");
            Map<String, Collection<KendoUIDataItem>> data = getData();
            Collection<KendoUIDataItem> items = data.get(fqn);
            int caretOffset = ccContext.getCaretOffset();
            if (items != null) {
                StatusDisplayer.getDefault().setStatusText(fqn + ": code completion shown.");
                List<CompletionProposal> result = new ArrayList<CompletionProposal>();
                for (KendoUIDataItem item : items) {
                    if (fqn.startsWith(prefix)) {
                        result.add(KendoUICompletionItem.createKendoUIItem(item, caretOffset));
                    }
                }
                return result;
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        if (element != null && element instanceof KendoUIElement) {
            return ((KendoUIElement) element).getDocumentation();
        }
        return null;
    }

}
