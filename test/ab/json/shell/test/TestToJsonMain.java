/**
 * 
 */
package ab.json.shell.test;

import static org.junit.Assert.*;

import org.junit.Test;

import ab.json.shell.ToJsonMain;

/**
 * @author Axel Bengtsson
 * 
 */
public class TestToJsonMain {

  @Test
  public void testCommonSpaceSplit() {
    final String[] args = { "--file=test-files/test-common.txt", "a.b", "a.c",
        "a.d.e", "b" };
    assertTrue(new ToJsonMain().init(args) == 0);
  }

  @Test
  public void testRegexSplit() {
    final String[] args = { "--file=test-files/test-regex.txt",
        "--regex=.*par1=\"(.+)\" par2=\"(.+)\" par3=\"(.+)\".*", "a.b", "a.c",
        "a.d" };
    assertTrue(new ToJsonMain().init(args) == 0);
  }

  @Test
  public void testPackages() {
    final String[] args = { "--file=test-files/test-packages.txt",
        "--newline=", "--regex=.*Package\\: (.+) Version\\: (.+?) .*", "name",
        "version" };
    assertTrue(new ToJsonMain().init(args) == 0);
  }
}
