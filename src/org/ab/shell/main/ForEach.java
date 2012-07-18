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

import static ab.json.shell.Utils.FILE;
import static ab.json.shell.Utils.HELP;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ab.json.shell.Utils.ReturnValue;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @author axel
 *
 */
public class ForEach {
  private static final String COMMAND = "--cmd=";
  File script = null;
  String scriptLine = null;
  private InputStream inputStream = System.in;

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
        while ((s = in.readLine()) != null || sb.length() != 0) {
          sb.append(s);
        }
        objectReferenc = new GsonBuilder().create().fromJson(sb.toString(), new TypeToken< List<Map<String, String>>>(){}.getType());
        for (Map<String, String> obj : objectReferenc) {
          System.out.println(obj.toString());
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
  private ReturnValue parseArguments(final String[] args) {
    ReturnValue returnValue = ReturnValue.OK;
    for (String arg : args) {
      if (arg.startsWith(COMMAND)) {
        scriptLine = arg.substring(COMMAND.length());
      } else if (arg.startsWith(HELP)) {
        //printHelp(); TODO
        returnValue = ReturnValue.HELP;
      } else if (arg.startsWith(FILE)) {} 
    }
    return returnValue;
  }
  /**
   * @param args
   */
  public static void main(String[] args) {
    System.exit(new ForEach().init(args));
  }

}
