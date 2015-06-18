package org.netbeans.modules.javascript2.kendo;

/**
 *
 * @author Petr Pisl and Geertjan Wielenga
 */
public class KendoUIDataItem {
    
    private final String name;
    private final String type;
    private final String documentation;
    private final String template;

    public KendoUIDataItem(String name, String type, String documentation, String template) {
        this.name = name;
        this.type = type;
        this.documentation = documentation;
        this.template = template;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDocumentation() {
        return documentation;
    }

    public String getTemplate() {
        return template;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KendoUIDataItem other = (KendoUIDataItem) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
    
}
