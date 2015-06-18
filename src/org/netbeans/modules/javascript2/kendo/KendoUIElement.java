package org.netbeans.modules.javascript2.kendo;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class KendoUIElement implements ElementHandle {

        private final String name;
        private final ElementKind kind;
        private final String documentation;

        public KendoUIElement(String name, String documentation, ElementKind kind) {
            this.name = name;
            this.kind = kind;
            this.documentation = documentation;
        }
        
        
        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return "";
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getIn() {
            return "";
        }

        @Override
        public ElementKind getKind() {
            return kind;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.<Modifier>emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }

        public String getDocumentation() {
            return documentation;
        }
        
    }
