/**
 * Rules are comparable on the operation name, to make it easier for the
 * throttler to find the appropriate rule for a given operation.
 * 
 * @author dsuskin
 * 
 */
public class Rule implements Comparable<Rule> {
    private final String operationName;
    private final int tokenBucketRefilledTokensPerSecond;
    private final int tokenBucketCapacity;

    public Rule(String operationName, int tokenBucketRefilledTokensPerSecond,
            int tokenBucketCapacity) {
        super();
        this.operationName = operationName;
        this.tokenBucketRefilledTokensPerSecond = tokenBucketRefilledTokensPerSecond;
        this.tokenBucketCapacity = tokenBucketCapacity;
    }

    public String getOperationName() {
        return operationName;
    }

    public int getTokenBucketRefilledTokensPerSecond() {
        return tokenBucketRefilledTokensPerSecond;
    }

    public int getTokenBucketCapacity() {
        return tokenBucketCapacity;
    }

    @Override
    public int compareTo(Rule arg0) {
        if (this.equals(arg0)) {
            return 0;
        } else {
            return operationName.compareTo(arg0.operationName);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((operationName == null) ? 0 : operationName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Rule other = (Rule) obj;
        if (operationName == null) {
            if (other.operationName != null)
                return false;
        } else if (!operationName.equals(other.operationName))
            return false;
        return true;
    }

}
