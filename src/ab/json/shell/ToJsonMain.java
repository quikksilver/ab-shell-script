/**
 * 
 */
package ab.json.shell;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Axel Bengtsson (axel.bengtsson@gmail.com)
 * 
 */
public class ToJsonMain {

  private static final String REGEX = "--regex=";
  private static final String HELP = "--help";
  private static final String FILE = "--file=";
  private static final String NEWLINE = "--newline=";

  private InputStream inputStream = System.in;
  private Pattern regex = null;
  private String newline = null;

  private enum ReturnValue {
    OK, HELP, ERROR
  };

  private class Obj {
    Map<String, String> values = new HashMap<String, String>();
    Map<String, Obj> valuesObj = new HashMap<String, ToJsonMain.Obj>();

    @Override
    public String toString() {
      return values.toString() + " " + valuesObj.toString();
    }
  }

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
    return returnValue.ordinal();
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
        System.out.println("Usage:");
        System.out.println("echo foo baa | tojson n1 n2");
        System.out.println("{n1 : \"foo\", n2 : \"baa\"}");
        System.out
            .println("echo '<foo name=\"baa\" age=\"12\"' | tojson --regex=.*name=(.+).*age=(.+).* name age");
        System.out.println("{name : \"baa\", age : \"12\"}");
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

  public static void main(String[] args) {
    System.exit(new ToJsonMain().init(args));
  }

}
