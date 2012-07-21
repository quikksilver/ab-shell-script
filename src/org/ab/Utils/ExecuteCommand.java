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
package org.ab.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Execute commands.
 * @author axel
 */
public class ExecuteCommand {

  /**
   * Read stream and save it in a String.
   * @author axel
   */
  private static class ReadStream implements Runnable {
    final InputStream inputStream;
    final boolean print;
    final StringBuilder sb = new StringBuilder("");

    public ReadStream(InputStream inputStream, boolean print) {
      this.inputStream = inputStream;
      this.print = print;
    }
    @Override
    public void run() {
      BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
      String s;
      final StringBuilder sb = new StringBuilder("");
      try {
        while ((s = in.readLine()) != null) {
          if (print) {
            System.out.println(s);
            sb.append(s).append("\n");
          }
        }
      } catch (IOException e) {
       e.printStackTrace();
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace(System.err);
        }
      }
    }
    public String getStringStrream() {
      return sb.toString();
    }
  }

  /**
   * Execute command and get String from standard out.
   * @param cmd - THe command in a String array.
   * @return - A string of standard out.
   * @throws IOException
   * @throws InterruptedException
   */
  public static String execCmd(final String[] cmd) throws IOException, InterruptedException{
    Runtime rt = Runtime.getRuntime();
    Process pr = null;
    ReadStream std = null;
    try {
      pr = rt.exec(cmd);
      std = new ExecuteCommand.ReadStream(pr.getInputStream(), true);
      ReadStream err = new ExecuteCommand.ReadStream(pr.getErrorStream(), false);

      std.run();
      err.run();

      int exitValue = pr.waitFor();
      if (exitValue != 0) {
        throw new IOException(Arrays.toString(cmd) + " got exit code: "
            + exitValue + "\nError: " + err.getStringStrream());
      }
    } finally {
      if (pr != null)
      pr.destroy();
    }
    return std.getStringStrream();
  }
}
