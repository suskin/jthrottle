import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Parsing should happen correctly
 * 
 * @author dsuskin
 * 
 */
public class RuleParsingTest {

    private String testRules;
    private List<Rule> sourceRules;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
        sourceRules = null;
        testRules = null;
    }

    private void initializeRules(int howMany) throws Exception {
        sourceRules = generateRules(howMany);
        testRules = new ObjectMapper().writeValueAsString(sourceRules);
    }

    /**
     * It turns out this case is not well supported by Jackson; the @JsonProperty
     * includes a field for this but it does not do anything. See
     * http://jira.codehaus.org/browse/JACKSON-767
     * 
     * Fortunately it's not a critical feature, but it does make debugging bad
     * configurations a little more manual.
     * 
     * @throws Exception
     */
    @Test(expected = IllegalStateException.class)
    public void shouldErrorIfFieldMissing() throws Exception {
        testRules = "[{\"operation\":\"foo/test0\",\"capacity\":0}]";
        RuleFactory.parseRules(IOUtils.toInputStream(testRules));
    }

    @Test
    public void shouldParseSingleRule() throws Exception {
        initializeRules(1);
        ConcurrentNavigableMap<String, Rule> parsedRules = RuleFactory
                .parseRules(IOUtils.toInputStream(testRules));

        assertParsedRulesMatchSourceRules(parsedRules, sourceRules);
    }

    @Test
    public void shouldParseMultipleRules() throws Exception {
        initializeRules(5);
        ConcurrentNavigableMap<String, Rule> parsedRules = RuleFactory
                .parseRules(IOUtils.toInputStream(testRules));

        assertParsedRulesMatchSourceRules(parsedRules, sourceRules);
    }

    private static void assertParsedRulesMatchSourceRules(
            ConcurrentNavigableMap<String, Rule> parsedRules,
            Collection<Rule> sourceRules) {
        for (Rule sourceRule : sourceRules) {
            assertTrue(parsedRules.containsKey(sourceRule.getOperationName()));
            Rule rule = parsedRules.get(sourceRule.getOperationName());

            assertEquals(rule.getOperationName(), sourceRule.getOperationName());
            assertEquals(rule.getTokenBucketCapacity(),
                    sourceRule.getTokenBucketCapacity());
            assertEquals(rule.getTokenBucketRefilledTokensPerSecond(),
                    sourceRule.getTokenBucketRefilledTokensPerSecond());
        }
    }

    private static List<Rule> generateRules(int howMany) {
        List<Rule> rules = new ArrayList<Rule>(howMany);

        for (int i = 0; i < howMany; i++) {
            String operationName = "foo/test" + i;
            int refillRate = i;
            int capacity = i;
            rules.add(new Rule(operationName, refillRate, capacity));
        }

        return rules;
    }
}
