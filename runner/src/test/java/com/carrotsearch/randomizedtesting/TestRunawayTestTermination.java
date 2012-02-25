package com.carrotsearch.randomizedtesting;

import static com.carrotsearch.randomizedtesting.SysGlobals.SYSPROP_KILLATTEMPTS;
import static com.carrotsearch.randomizedtesting.SysGlobals.SYSPROP_KILLWAIT;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.carrotsearch.randomizedtesting.annotations.Timeout;

public class TestRunawayTestTermination extends WithNestedTestClass {
  @Timeout(millis = 1000)
  public static class Nested1 extends RandomizedTest {
    @Test
    public void spinning() throws Exception {
      assumeRunningNested();
      while (true) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // Ignore.
        }
      }
    }
  }
  
  public static class Nested2 extends RandomizedTest {
    @Test @Timeout(millis = 1000)
    public void sleeping() throws Exception {
      assumeRunningNested();
      while (true) {
        Thread.sleep(1000);
      }
    }    
  }
  
  public static class Nested3 extends RandomizedTest {
    @Test @Timeout(millis = 1000)
    public void bulletProof() throws Exception {
      assumeRunningNested();
      while (true) {
        try {
          while (true) {
            Thread.sleep(1000);
          }
        } catch (Throwable t) {
          // Ignore.
        }
      }
    }
  }

  @BeforeClass
  public static void speedup() {
    System.setProperty(SYSPROP_KILLATTEMPTS(), "1");
    System.setProperty(SYSPROP_KILLWAIT(), "100");
  }
  
  @Test
  public void spinning() throws Throwable {
    Result r = JUnitCore.runClasses(Nested1.class);
    Assert.assertEquals(1, r.getFailureCount());
  }

  @Test
  public void sleeping() throws Throwable {
    Result r = JUnitCore.runClasses(Nested2.class);
    for (Failure f : r.getFailures()) {
      System.out.println(f.toString());
    }
    Assert.assertEquals(1, r.getFailureCount());
  }

  @Test
  public void bulletProof() throws Throwable {
    Result r = JUnitCore.runClasses(Nested3.class);
    Assert.assertEquals(1, r.getFailureCount());
  }
}
