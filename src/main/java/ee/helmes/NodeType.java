package ee.helmes;

/**
 * Stores the enum of {@code "type"} attribute value.
 */
public enum NodeType {
    ROOT, SUB, CHILD;

    /**
     * Returns integer representation of {@code NodeType} associated with the given string parameter.
     *
     * @param nodeType {@code string} representation of {@link ee.helmes.NodeType}.
     * @return integer representation of {@code this.NodeType} associated with the given string parameter.
     */
    public static int getType(String nodeType){
        nodeType = nodeType.toUpperCase();
        if(nodeType.equals(ROOT.toString())){
            return 1;
        }else{
            if(nodeType.equals(SUB.toString())){
                return 2;
            }else {
                if(nodeType.equals(CHILD.toString())){
                    return 3;
                }
            }
        }
        return 0;
    }


}
