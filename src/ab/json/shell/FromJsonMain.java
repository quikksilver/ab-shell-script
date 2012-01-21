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
package ab.json.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static ab.json.shell.Utils.FILE;
import static ab.json.shell.Utils.HELP;
import ab.json.shell.Utils.Obj;
import ab.json.shell.Utils.ReturnValue;


/**
 * @author Axel Bengtsson (axel.bengtsson@gmail.com)
 *
 */
public class FromJsonMain {

  /**
   * Main thread.
   * @param args - argument from the commandline.
   * @return exit value.
   */
  public int init(final String[] args) {
    ReturnValue returnValue = ReturnValue.OK;
    // Append the args
    StringBuilder output = new StringBuilder();
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    try {
      for (String arg : args) {
        if (arg.startsWith(FILE)) {
          in = new BufferedReader(new InputStreamReader(new FileInputStream(
              new File(arg.substring(FILE.length())))));
        } else if (arg.startsWith(HELP)) {
          printHelp();
          returnValue = ReturnValue.HELP;
        } else {
          output.append(arg + " ");
        }
      }
      if (returnValue == ReturnValue.OK) {
        // Start reading from stdin or file.
        String s;
        while ((s = in.readLine()) != null) {
          final Obj root = Utils.stringToObj(s);
          final StringBuilder changedOutput = new StringBuilder(
              output.toString());
          final Map<String, String> keyMap = new HashMap<String, String>();
          objToKeyMap(root, keyMap, "");
          int start;
          for (Map.Entry<String, String> entry : keyMap.entrySet()) {
            start = changedOutput.indexOf(entry.getKey());
            if (start >= 0) {
              changedOutput.replace(start, entry.getKey().length() + start,
                  entry.getValue());
            }
          }
          System.out.println(changedOutput.toString());
        }
      }
    } catch (IOException e) {
      e.printStackTrace(System.err);
      returnValue = ReturnValue.ERROR;
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return returnValue.toInt();
  }

  private void printHelp() {
    System.out.println("Usage: <options> key1, key2, ...");
    System.out.println("Example:");
    System.out.println("  $echo {name : \"pkg1\",version : \"1.0\"} | fromjson do name version");
    System.out.println("  do pkg1 1.0");
    System.out.println("  $echo {b : \"4\",a : {b : \"1\",c : \"2\",d : {e : \"3\"}}} | fromjson b a.d.e");
    System.out.println("  4 3");
    System.out.println("Options: ");
    System.out.println("  " + FILE + "<file to read instead of standard in>");
    System.out.println("  " + HELP + " Show this");
  }

  /**
   * Convert a Obj tree structure to a map<key.key..., value>.
   * @param root
   * @param keyMap
   * @param prefix
   */
  private void objToKeyMap(final Obj root, final Map<String, String> keyMap, final String prefix) {
    for (Entry<String, String> entry : root.values.entrySet()) {
      keyMap.put(prefix  + (prefix.length() == 0 ? "" : ".") + entry.getKey(), entry.getValue());
    }
    for (Entry<String, Obj> entry : root.valuesObj.entrySet()) {
      objToKeyMap(entry.getValue(), keyMap, prefix + (prefix.length() == 0 ? "" : ".") + entry.getKey());
    }
  }

  /**
   * Main method.
   * @param args - commandline arguments.
   */
  public static void main(String[] args) {
    System.exit(new FromJsonMain().init(args));
  }
}
