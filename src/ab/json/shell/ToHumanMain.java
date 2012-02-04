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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ab.json.shell.Utils.ReturnValue;
import static ab.json.shell.Utils.ReturnValue.OK;
import static ab.json.shell.Utils.ReturnValue.ERROR;

/**
 * @author Axel Bengtsson (axel.bengtsson@gmail.com)
 *
 */
public class ToHumanMain {
  private InputStream inputStream = System.in;
  enum Byte {
    singlebyte (1L, "B"),
    kilobyte(1024L, "kB"),
    megabyte(1048576L, "MB"),
    gigabyte(1073741824L, "GB"),
    terabyte(1099511627776L, "TB"),
    petabyte (1125899906842624L, "PB");
    private long bytes;
    private String name;
    private Byte(final long bytes, final String name) {
      this.bytes = bytes;
      this.name = name;
    }
    public long getBytes() {return bytes;}
    public String getName() {return name;}
  }

  /**
   * Main thread.
   * @param args - commandline options.
   * @return - exit value.
   */
  private int init(final String[] args) {
    ReturnValue returnValue = OK;
    if (args.length == 1 && args[0].equals("--help")) {
      returnValue = ReturnValue.HELP;
      System.out.println("Option: --time | --byte | --help");
    } else {
      final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
      String s;
      long sum = 0;
      try {
        while ((s = in.readLine()) != null) {
          if (!s.equals("")) {
            sum = sum + Long.valueOf(s);
          }
        }
      } catch (IOException e) {
        e.printStackTrace(System.err);
        returnValue = ERROR;
      } catch (NumberFormatException e) {
        e.printStackTrace(System.err);
        returnValue = ERROR;
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace(System.err);
        }
      }
      if (returnValue == OK) {
        String result = null;
        if (args.length == 0) {
          result = Long.toString(sum);
        } else if (args[0].equals("--byte")) {
          result = printByte(sum);
        } else if (args[0].equals("--time")) {
          result = "TODO";
        }
        if (result != null) {
          System.out.println(result);
        } else {
          System.err
              .println("Not a regonized option. (--help for list the options)");
        }
      }
    }
    return returnValue.toInt();
  }

 /**
  * Print bytes as string.
  * @param sum - bytes
  * @return - as string
  */
  private String printByte(final long sum) {
    String ret;
    if (sum == 0) {
      ret = "0B";
    } else {
      int i = 0;
      while (sum / Byte.values()[i].getBytes() != 0) {
        i++;
      }
      --i;
      ret = (sum + (Byte.values()[i].getBytes() / 2))
          / Byte.values()[i].getBytes() + Byte.values()[i].getName();
    }
    return ret;
  }

  public static void main(String[] args) {
    System.exit(new ToHumanMain().init(args));
  }
}
