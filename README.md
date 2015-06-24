#jthrottle

Rule-based rate limiting

## What

jthrottle provides a simple implementation of
[token bucket rate limiting](https://en.wikipedia.org/wiki/Token_bucket).

The basic premise is as follows. You can think of each request "bucket" as
having a maximum burst rate (known as the bucket's **capacity**), and a
maximum sustained rate (known as the bucket's **refill rate**). These are
per second. Each request uses one token from its bucket. If the bucket
has no tokens left, the request is throttled.

## Why

Request throttling is a common problem within service-oriented scalable
systems. A mature service must eventually be able to shed excess load.
Sometimes it's useful to be able to do this at an application level;
this is where jthrottle comes in.

## Key properties

  * Sliding windows. Since the buckets are constantly refilling, you
    don't need to worry about users hitting max burst at the end of one slice of
    time and again at the beginning of the next. This is more a property
    of the token bucket algorithm than jthrottle specifically, but shrug.
  * You don't need to know all of your operations ahead of time. Since
    jthrottle provides simple rule inheritance, you can throttle on
    things like operation and user id combinations.
  * Lightweight. The only dependency is on Jackson, for rule parsing.

## How

For basic use, jthrottle has three main classes:

  * Throttler
  * ThrottlerTick
  * RuleFactory

**Throttler** contains the main logic for creating buckets and
determining whether a request should be throttled. It takes in
rules, which can be created using a RuleFactory.

**ThrottlerTick** is a [TimerTask](http://docs.oracle.com/javase/7/docs/api/java/util/TimerTask.html)
which will tell a Throttler to add tokens to its buckets.

**RuleFactory** is a class which will create rules for you if you pass
it some JSON. These rules can then be handed to a Throttler.

Here's a short example of some code which uses a throttler:

```java
ConcurrentNavigableMap<String, Rule> parsedRules = RuleFactory
        .parseRules(new StringReader("[{\"operation\": \"myOperation\", \"refillRate\": 10, \"capacity\": 100}]");
Throttler throttler = new Throttler(parsedRules);
ThrottlerTick throttlerTick = new ThrottlerTick(throttler);
Timer timer = new Timer();
timer.scheduleAtFixedRate(throttlerTick, 0, 500);

// later . . .

throttler.throttle("myOperation/userId1");
// <- false
```

### Specifying rules

jthrottle provides somewhat flexible rule matching and inheritance; it's
all based on prefix. Every time you make a call to throttle, a new bucket
is created if it doesn't already exist. That bucket inherits rules from
the rule with the longest prefix matching the string passed to jthrottle.
I'll give an example.

Let's say I have specified the following as the rules:

```json
[
  {
    "operation": "myOperation",
    "capacity": 100,
    "refillRate": 10
  }
]

```

If I call into jthrottle and pass it "myOperation/userId1",
then a new bucket will be created for that string. It'll inherit a
capacity of 100 and a refill rate of 10. If I pass
"myOperation/userId2", a new, different bucket will be created, with the
same rules. If I pass it "myOperation", a third bucket.
"myOperation/userId1/someOtherThing", a fourth.

When created, new buckets are full.

## Assumptions

jthrottle makes a few assumptions:

  * Your traffic is evenly distributed.
    - jthrottle does not attempt to maintain any state which is shared
      across machines. This relies on the assumption that your traffic
      is distributed fairly evenly, like it would be if you used a
      round-robin approach.
    - However, jthrottle is lightweight and flexible enough that if
      you needed some other structure, you could probably use jthrottle
      to power it.
  * You'll use delimiters in your operation strings
    - Rule matching doesn't use a real Trie, so longest-prefix matching
      only works if you use delimiters in your rules. So if you have
      rules "a" and "ab", "a/c" and "a-c" will inherit from "a", but
      "ac" won't match anything. This use-case can be accommodated by
      having trickier rule-matching logic, but I hear about it being a
      legimate use-case I'm going to hold off on that.

## Potential bottlenecks

* Number of buckets (map size)
* Number of buckets (refilling overhead)

TODO some more details about bottlenecks

## License

BSD 3-clause
