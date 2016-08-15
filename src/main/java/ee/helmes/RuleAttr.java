package ee.helmes;

/**
 * Stores the enum of attributes required for parsing and filtering.
 */
public enum RuleAttr {
    NAME,
    TYPE,
    WEIGHT;

    /**
     * Returns string representation of {@code this.RuleAttr}.
     *
     * @return string representation of {@code this.RuleAttr}.
     */
    public String getValue() {
        switch (this) {
            case NAME:
                return "name";
            case TYPE:
                return "type";
            case WEIGHT:
                return "weight";
            default:
                return "";
        }
    }


}
