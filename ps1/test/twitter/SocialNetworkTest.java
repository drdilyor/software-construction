/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class SocialNetworkTest {

    /*
     * TODO: your testing strategies for these methods should go here.
     * See the ic03-testing exercise for examples of what a testing strategy comment looks like.
     * Make sure you have partitions.
     */

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testGuessFollowsGraphEmpty() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(new ArrayList<>());
        assertTrue("expected empty graph", followsGraph.isEmpty());
    }
    
    @Test
    public void testInfluencersEmpty() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        List<String> influencers = SocialNetwork.influencers(followsGraph);
        
        assertTrue("expected empty list", influencers.isEmpty());
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * SocialNetwork class that follows the spec. It will be run against several
     * staff implementations of SocialNetwork, which will be done by overwriting
     * (temporarily) your version of SocialNetwork with the staff's version.
     * DO NOT strengthen the spec of SocialNetwork or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in SocialNetwork, because that means you're testing a
     * stronger spec than SocialNetwork says. If you need such helper methods,
     * define them in a different class. If you only need them in this test
     * class, then keep them in this test class.
     */

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");

    private void assertEdge(Map<String, Set<String>> graph, String from, String to) {
        assertTrue("expected edge " + from + " -> " + to, graph.get(from).contains(to));
    }

    private Map<String, Set<String>> normalizeGraph(Map<String, Set<String>> graph) {
        Map<String, Set<String>> res = new HashMap<>();
        for (var from : graph.keySet()) {
            assertFalse("duplicate key " + from, res.containsKey(from));
            res.put(from.toLowerCase(), graph.get(from).stream().map(String::toLowerCase).collect(Collectors.toSet()));
            assertEquals("duplicate value in edges from " + from,
                    graph.get(from).size(), res.get(from.toLowerCase()).size());
            assertFalse("user can't follow themself", res.get(from.toLowerCase()).contains(from.toLowerCase()));
        }
        return res;
    }

    @Test
    public void testGuessFollowsSparse() {
        var graph = normalizeGraph(SocialNetwork.guessFollowsGraph(List.of(
                new Tweet(1, "alice", "hello @bob", d1),
                new Tweet(2, "bob", "hello", d1),
                new Tweet(3, "candice", "Hi everyone", d1),
                new Tweet(4, "dave", "Nice to meet you @candice", d1)
        )));
        assertEdge(graph, "alice", "bob");
        assertEdge(graph, "dave", "candice");
    }

    @Test
    public void testGuessFollowsCycle() {
        var graph = normalizeGraph(SocialNetwork.guessFollowsGraph(List.of(
                new Tweet(1, "alice", "hello @bob", d1),
                new Tweet(2, "bob", "Good evening @candice", d1),
                new Tweet(3, "candice", "Hi @alice", d1)
        )));
        assertEdge(graph, "alice", "bob");
        assertEdge(graph, "bob", "candice");
        assertEdge(graph, "candice", "alice");
    }

    @Test
    public void testGuessFollowsLoopAndMultiEdge() {
        var graph = normalizeGraph(SocialNetwork.guessFollowsGraph(List.of(
                new Tweet(1, "alice", "@alice @bob @bob", d1),
                new Tweet(2, "bob", "@bob @alice @bob @alice", d1)
        )));
        assertEdge(graph, "alice", "bob");
        assertEdge(graph, "bob", "alice");
    }

    @Test
    public void testGuessFollowsDense() {
        var graph = normalizeGraph(SocialNetwork.guessFollowsGraph(List.of(
                new Tweet(1, "alice", "hello @bob @candice", d1),
                new Tweet(2, "bob", "Good evening @alice @candice", d1),
                new Tweet(3, "candice", "Hi everyone! @alice @bob", d1)
        )));
        assertEdge(graph, "alice", "bob");
        assertEdge(graph, "alice", "candice");
        assertEdge(graph, "bob", "alice");
        assertEdge(graph, "bob", "candice");
        assertEdge(graph, "candice", "alice");
        assertEdge(graph, "candice", "bob");
    }
}
