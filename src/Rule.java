import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Rules are comparable on the operation name, to make it easier for the
 * throttler to find the appropriate rule for a given operation.
 * 
 * TODO may want to have equality compare all fields, and have a separate
 * comparator for checking just the operation name. Rule comparison shouldn't be
 * linked to how they are used in a single case.
 * 
 * @author dsuskin
 * 
 */
public class Rule implements Comparable<Rule> {
    private final String operationName;
    private final int tokenBucketRefilledTokensPerSecond;
    private final int tokenBucketCapacity;

    /**
     * Apparently the 'required' field does not do what it's supposed to do yet;
     * see http://jira.codehaus.org/browse/JACKSON-767
     * 
     * @param operationName
     * @param tokenBucketRefilledTokensPerSecond
     * @param tokenBucketCapacity
     */
    @JsonCreator
    public Rule(
            @JsonProperty(value = "operation", required = true) String operationName,
            @JsonProperty(value = "refillRate", required = true) int tokenBucketRefilledTokensPerSecond,
            @JsonProperty(value = "capacity", required = true) int tokenBucketCapacity) {
        super();
        this.operationName = operationName;
        this.tokenBucketRefilledTokensPerSecond = tokenBucketRefilledTokensPerSecond;
        this.tokenBucketCapacity = tokenBucketCapacity;
    }

    @JsonGetter("operation")
    public String getOperationName() {
        return operationName;
    }

    @JsonGetter("refillRate")
    public int getTokenBucketRefilledTokensPerSecond() {
        return tokenBucketRefilledTokensPerSecond;
    }

    @JsonGetter("capacity")
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
