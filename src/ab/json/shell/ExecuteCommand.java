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
import java.io.OutputStream;
import java.util.List;

/**
 * @author axel
 *
 */
public class ExecuteCommand {

  private static class ReadStream implements Runnable {
    InputStream inputStream = null;
    boolean print;
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
          }
        }
      } catch (IOException e) {
       e.printStackTrace();
      }
    }
    
  }

  public static List<String> execCmd(final String[] cmd) throws IOException, InterruptedException{
    List<String> stdOut = null;
    Runtime rt = Runtime.getRuntime();
    Process pr = null;
    try {
      pr = rt.exec(cmd);
      ReadStream std = new ExecuteCommand.ReadStream(pr.getInputStream(), true);
      ReadStream err = new ExecuteCommand.ReadStream(pr.getErrorStream(), false);

      std.run();
      err.run();
      
      //TODO: Start two threads that reads stdOut and errOut.
      int exitValue = pr.waitFor();
      if (exitValue != 0) {
        throw new IOException(cmd + " got exit code: " + exitValue);
      }
    } finally {
      if (pr != null)
      pr.destroy();
    }
    return stdOut;
  }
}
