/**
 * Copyright 2012 Axel Bengtsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

  *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
