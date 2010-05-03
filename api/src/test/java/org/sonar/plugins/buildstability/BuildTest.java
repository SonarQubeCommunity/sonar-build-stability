package org.sonar.plugins.buildstability;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Evgeny Mandrikov
 */
public class BuildTest {
  @Test
  public void testParse() throws Exception {
    Build build = Build.fromString("num=1;time=1272891187240;duration=60;res=ok");

    assertThat(build.getNumber(), is(1));
    assertThat(build.getTimestamp(), is(1272891187240L));
    assertThat(build.isSuccessful(), is(true));
    assertThat(build.getDuration(), is(60 * 1000d));
  }

  @Test
  public void testIncorrect() {
    // TODO
  }
}
