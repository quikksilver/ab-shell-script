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
package org.ab.shell.main;

import static org.ab.Utils.Utils.FILE;
import static org.ab.Utils.Utils.HELP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.ab.Utils.ExecuteInThreads;
import org.ab.Utils.Utils.ExecuteCommandInThread;
import org.ab.Utils.Utils.ReturnValue;


import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @author axel
 *
 */
public class ForEach {
  private static final String BASH = "--bash=";
  private static final String SCRIPT = "--script=";
  private static final String JOBS = "--jobs=";
  private File scriptFile = null;
  private String bashLine = null;
  private InputStream inputStream = System.in;
  private int maxThreads = 1;
  private long timeoutinSeconds = 60 * 60;

  /**
   * Main thread.
   * @param args
   * @return
   */
  public int init(final String[] args) {
    List<Map<String, String>> objectReferenc = null;
    // Read the arguments
    ReturnValue returnValue = parseArguments(args);
    if (returnValue == ReturnValue.OK) {
      // Start reading from stdin.
      BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
      String s;
      final StringBuilder sb = new StringBuilder("");
      try {
        while ((s = in.readLine()) != null) {
          sb.append(s);
        }
        objectReferenc = new GsonBuilder().create().fromJson(sb.toString(),
            new TypeToken<List<Map<String, String>>>() {}.getType());
        List<Callable<ExecuteCommandInThread>> commands = new LinkedList<Callable<ExecuteCommandInThread>>();
        // Created excitable object for each object. 
        for (Map<String, String> obj : objectReferenc) {
          if (bashLine != null) {
            final String[] cmd = creatteBashCmd(obj, new String[] {"bash", "-c"}, getMaxArgumentNumberUsed(bashLine));
            if (cmd != null) {
              commands.add(new ExecuteCommandInThread(cmd));
            } 
          } else if (scriptFile != null) {
              final String[] cmd = creatteBashCmd(obj, new String[] {scriptFile.getAbsolutePath()}, -1);
              commands.add(new ExecuteCommandInThread(cmd));
          } else {
            System.out.println(obj.toString());
          }
        }
        // Run all objects
        if (!commands.isEmpty()) {
          ExecuteInThreads.executeInThreads(commands, maxThreads, timeoutinSeconds);
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

  private int getMaxArgumentNumberUsed(String bashLine2) {
    String[] bashArray = bashLine2.split("\\$");
    int max = -1;
    for (int i = 1; i < bashArray.length; i++) {
      StringBuffer sb = new StringBuffer();
      for (int j = 0; j < bashArray[i].length(); j++) {
        if (Character.isDigit(bashArray[i].charAt(j))) {
          sb.append(bashArray[i].charAt(j));
        } else {
          break;
        }
      }
      if (sb.length() > 0) {
        max = Math.max(max, Integer.valueOf(sb.toString()));
      }
    }
    return max;
  }

  private String[] creatteBashCmd(final Map<String, String> map,
      final String[] cmd, final int maxArgumentNumberUsed) {
    final List<String> cmdList = new LinkedList<String>();
    cmdList.addAll(Arrays.asList(cmd));
    if (bashLine != null) {
      cmdList.add(bashLine);
    }
    int i = 0;
    for (; i < map.size() ; i++) {
      cmdList.add(map.get(String.valueOf(i)));
    }
    if (i <= maxArgumentNumberUsed) {
      return null;
    }
    return cmdList.toArray(new String[cmdList.size()]);
  }

  /**
   * Parse the flags
   * @param args
   * @return
   */
  private ReturnValue parseArguments(final String[] args) {
    ReturnValue returnValue = ReturnValue.OK;
    for (String arg : args) {
      if (arg.startsWith(BASH)) {
        bashLine = arg.substring(BASH.length());
      } else if (arg.startsWith(HELP)) {
        printHelp();
        returnValue = ReturnValue.HELP;
      } else if (arg.startsWith(SCRIPT)) {
        scriptFile = new File(arg.substring(SCRIPT.length()));
      } else if (arg.startsWith(FILE)) {
        try {
          inputStream = new FileInputStream(arg.substring(FILE.length()));
        } catch (IOException e) {
          e.printStackTrace(System.err);
          returnValue = ReturnValue.ERROR;
        }
      } else if (arg.startsWith(JOBS)) {
        maxThreads = Integer.valueOf(arg.substring(JOBS.length()));
      } else {
        System.err.println("Not a recognized argument");
        returnValue = ReturnValue.ERROR;
      }
    }
    return returnValue;
  }

  /**
   * Print the help to standard out.
   */
  private void printHelp() {
    System.out.println("foreach [flags]");
    System.out.println("flags:");
    System.out.println("  --help  Print this.");
    System.out.println("  --bash=<bashline> Run this command for each object");
    System.out.println("                    Use $1 $2 ... to get the desire");
    System.out.println("  --script=<path to file> Run ths command for each object");
    System.out.println("                          Executed: script obj_1 obj_2 ...");
    System.out.println("  --file=<path to file> Read from this file instead of stdout");
    System.out.println();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.exit(new ForEach().init(args));
  }

}
