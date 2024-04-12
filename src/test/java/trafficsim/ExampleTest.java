package trafficsim;

// JUnit5
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

public class ExampleTest {

  @Test
  @DisplayName("JUnit can run a test")
  void shouldRunATest() {
    assertEquals(true, true, "Test should pass");
  }
}

/*
// JUnit4
import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExampleTest {

  @Test
  public void testPasses() throws Exception {
    assertEquals("The test should pass", true, true);
  }
}
*/
