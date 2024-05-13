/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /*
     * TODO: your testing strategies for these methods should go here.
     * See the ic03-testing exercise for examples of what a testing strategy comment looks like.
     * Make sure you have partitions.
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testWrittenByMultipleTweetsSingleResult() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "alyssa");
        
        assertEquals("expected singleton list", 1, writtenBy.size());
        assertTrue("expected list to contain tweet", writtenBy.contains(tweet1));
    }
    
    @Test
    public void testInTimespanMultipleTweetsMultipleResults() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");
        
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(testStart, testEnd));
        
        assertFalse("expected non-empty list", inTimespan.isEmpty());
        assertTrue("expected list to contain tweets", inTimespan.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, inTimespan.indexOf(tweet1));
    }
    
    @Test
    public void testContaining() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("talk"));
        
        assertFalse("expected non-empty list", containing.isEmpty());
        assertTrue("expected list to contain tweets", containing.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, containing.indexOf(tweet1));
    }

    /*
     * Warning: all the tests you write here must be runnable against any Filter
     * class that follows the spec. It will be run against several staff
     * implementations of Filter, which will be done by overwriting
     * (temporarily) your version of Filter with the staff's version.
     * DO NOT strengthen the spec of Filter or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Filter, because that means you're testing a stronger
     * spec than Filter says. If you need such helper methods, define them in a
     * different class. If you only need them in this test class, then keep them
     * in this test class.
     */

    private boolean tweetEquals(Tweet t1, Tweet t2) {
        return t1.getId() == t2.getId()
                && t1.getAuthor().equals(t2.getAuthor())
                && t1.getText().equals(t2.getText())
                && t1.getTimestamp().equals(t2.getTimestamp());
    }

    @Test
    public void testWrittenByCaseInsensitive() {
        var tweets = Arrays.asList(
                new Tweet(12, "bob", "content", d1),
                new Tweet(13, "BoB", "more content", d2)
        );
        var filteredTweets = Filter.writtenBy(new ArrayList<>(tweets), "bOb");
        assertEquals(tweets, filteredTweets);
    }

    @Test
    public void testWrittenByIsPure() {
        var tweets = Arrays.asList(
                new Tweet(1, "alice", "content", d1),
                new Tweet(2, "bob", "content", d2),
                new Tweet(3, "BOB", "content", d2)
        );
        var tweetsClone = new ArrayList<>(tweets);
        Filter.writtenBy(tweetsClone, "bob");

        assertEquals("expected to not mutate input", tweets, tweetsClone);
    }

    @Test
    public void testInTimespanEmptyInput() {
        var filtered = Filter.inTimespan(new ArrayList<Tweet>(), new Timespan(d1, d2));
        assertTrue(filtered.isEmpty());
    }

    @Test
    public void testInTimespanEmptyOutput() {
        var filtered = Filter.inTimespan(Arrays.asList(
                new Tweet(1, "author", "content", d1),
                new Tweet(2, "author", "content", d1)
        ), new Timespan(d2, d2));
        assertTrue(filtered.isEmpty());
    }

    @Test
    public void testInTimespanSingleTimePoint() {
        var filtered = Filter.inTimespan(Arrays.asList(
                new Tweet(1, "author", "tweet 1", d1),
                new Tweet(2, "author", "tweet 2", d1),
                new Tweet(3, "author", "tweet 3", d2)
        ), new Timespan(d1, d1));

        assertEquals("expected to find 2 tweets", 2, filtered.size());
        assertEquals(filtered.get(0).getId(), 1);
        assertEquals(filtered.get(1).getId(), 2);
    }

    @Test
    public void testInTimespanIsPure() {
        var tweets = Arrays.asList(
                new Tweet(1, "author", "tweet 1", d1),
                new Tweet(2, "author", "tweet 2", d1),
                new Tweet(3, "author", "tweet 3", d2)
        );
        var tweetsClone = new ArrayList<>(tweets);
        Filter.inTimespan(tweetsClone, new Timespan(d1, d1));

        assertEquals("expected to not mutate input", tweets, tweetsClone);
    }
}
