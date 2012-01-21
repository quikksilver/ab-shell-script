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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ab.json.shell.Utils.FILE;
import static ab.json.shell.Utils.HELP;
import ab.json.shell.Utils.Obj;
import ab.json.shell.Utils.ReturnValue;

/**
 * @author Axel Bengtsson (axel.bengtsson@gmail.com)
 * 
 */
public class ToJsonMain {

  private static final String REGEX = "--regex=";
  private static final String NEWLINE = "--newline=";

  private InputStream inputStream = System.in;
  private Pattern regex = null;
  private String newline = null;

  /**
   * Main method.
   * 
   * @param args
   * @return
   */
  public int init(final String[] args) {
    final List<String> objectReferenc = new ArrayList<String>();
    // Read the arguments
    ReturnValue returnValue = parseArguments(args, objectReferenc);
    if (returnValue == ReturnValue.OK) {
      // Start reading from stdin.
      BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
      String s;
      StringBuilder sb = new StringBuilder("");
      Obj root;
      try {
        while ((s = in.readLine()) != null || sb.length() != 0) {
          root = new Obj();
          if (newline != null) {
            if (s == null || s.equals(newline)) {
              s = sb.toString();
            } else {
              sb.append(" " + s);
              continue;
            }
          }
          if (regex != null) {
            Matcher m = regex.matcher(s);
            if (m.matches()) {
              for (int i = 0; i < objectReferenc.size(); ++i) {
                addObj(root, objectReferenc.get(i), m.group(i + 1));
              }
            }
          } else {
            String[] groups = s.split(" ");
            for (int i = 0; i < objectReferenc.size(); ++i) {
              addObj(root, objectReferenc.get(i), groups[i]);
            }
          }
          // Print Json
          StringBuilder output = new StringBuilder();
          createOutput(root, output);
          System.out.println(output.toString());
          sb = new StringBuilder("");
        }
      } catch (IOException e) {
        e.printStackTrace(System.err);
        returnValue = ReturnValue.ERROR;
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace(System.err);
        }
      }
    }
    return returnValue.toInt();
  }

  /**
   * Create the Json string of the Obj structure.
   * 
   * @param root
   * @param output
   */
  private void createOutput(final Obj root, final StringBuilder output) {
    output.append('{');
    for (Entry<String, String> entry : root.values.entrySet()) {
      output.append(entry.getKey() + " : \"" + entry.getValue() + "\",");
    }
    for (Entry<String, Obj> entry : root.valuesObj.entrySet()) {
      output.append(entry.getKey() + " : ");
      createOutput(entry.getValue(), output);
      output.append(",");
    }
    if (output.length() > 1) {
      output.deleteCharAt(output.length() - 1);
    }
    output.append('}');
  }

  /**
   * Add key value pair to the Obj hash structure.
   * 
   * @param root
   * @param key
   * @param value
   */
  private void addObj(final Obj root, final String key, final String value) {
    Obj current = root;
    final String[] ladder = key.split("\\.");
    for (int i = 0; i < ladder.length; i++) {
      if (i + 1 < ladder.length) {
        if (!current.valuesObj.containsKey(ladder[i])) {
          current.valuesObj.put(ladder[i], new Obj());
        }
        current = current.valuesObj.get(ladder[i]);
      } else {
        current.values.put(ladder[i], value);
      }
    }
  }

  /**
   * Parse the argument and save them in global variable.
   * 
   * @param args
   * @param objectReferenc
   * @return
   */
  private ReturnValue parseArguments(final String[] args,
      final List<String> objectReferenc) {
    ReturnValue returnValue = ReturnValue.OK;
    for (String arg : args) {
      if (arg.startsWith(REGEX)) {
        regex = Pattern.compile(arg.substring(REGEX.length()));
      } else if (arg.startsWith(HELP)) {
        printHelp();
        returnValue = ReturnValue.HELP;
      } else if (arg.startsWith(FILE)) {
        try {
          inputStream = new FileInputStream(arg.substring(FILE.length()));
        } catch (IOException e) {
          e.printStackTrace(System.err);
          returnValue = ReturnValue.ERROR;
        }
      } else if (arg.startsWith(NEWLINE)) {
        newline = arg.substring(NEWLINE.length());
      } else {
        objectReferenc.add(arg);
      }
    }
    return returnValue;
  }

  private void printHelp() {
    System.out.println("Usage: <options> key1, key2, ...");
    System.out.println("Example:");
    System.out.println("  $echo foo baa | tojson n1 n2");
    System.out.println("  {n1 : \"foo\", n2 : \"baa\"}");
    System.out.println("  $echo '<foo name=\"baa\" age=\"12\"' | tojson --regex=.*name=(.+).*age=(.+).* name age");
    System.out.println("  {name : \"baa\", age : \"12\"}");
    System.out.println("  $cat Packages | tojson '--newline=' '--regex=.*Package\\: (.+) Version\\: (.+?) .*' name version");
    System.out.println("  {name : \"pkg3\",version : \"3.0\"} ");
    System.out.println("Options: ");
    System.out.println("  " + FILE + "<file to read instead of standard in>");
    System.out.println("  " + REGEX + "<Java regex> (instead of having \" \" as seperator)");
    System.out.println("  " + HELP + " Show this");
    System.out.println("  " + NEWLINE + "<Instead of read to newline read to this line>");
  }

  public static void main(String[] args) {
    System.exit(new ToJsonMain().init(args));
  }

}
