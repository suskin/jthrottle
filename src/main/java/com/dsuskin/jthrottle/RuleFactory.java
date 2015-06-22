package com.dsuskin.jthrottle;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Parsing rules from a file can be a pain, so we want some code to make it
 * easier.
 * 
 * @author dsuskin
 * 
 */
public class RuleFactory {

    /**
     * Parses and indexes rules so you don't have to!
     * 
     * @param in
     * @return
     */
    public static ConcurrentNavigableMap<String, Rule> parseRules(InputStream in) {
        ObjectMapper om = new ObjectMapper();

        List<Rule> parsedRules = null;

        try {
            parsedRules = om.readValue(in, new TypeReference<List<Rule>>() {
            });
        } catch (JsonParseException e) {
            throw new IllegalStateException(e);
        } catch (JsonMappingException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        ConcurrentNavigableMap<String, Rule> indexedRules = new ConcurrentSkipListMap<String, Rule>();

        if (parsedRules != null) {
            for (Rule parsedRule : parsedRules) {
                indexedRules.put(parsedRule.getOperationName(), parsedRule);
            }
        }

        return indexedRules;
    }
}
