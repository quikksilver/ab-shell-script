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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.GsonBuilder;

import ab.json.shell.Utils.ReturnValue;

public class ToStruct {
  private static final String REGEX = "--regex=";
  private static final String NEWLINE = "--newline=";
  private static final String SPLIT = "--split=";

  private InputStream inputStream = System.in;
  private Pattern regex = null;
  private String newline = null;
  private String split = null;

  /**
   * Main method.
   * 
   * @param args
   * @return
   */
  public int init(final String[] args) {
    final List<Map<String, String>> objectReferenc = new LinkedList<Map<String, String>>();
    // Read the arguments
    ReturnValue returnValue = parseArguments(args);
    if (returnValue == ReturnValue.OK) {
      // Start reading from stdin.
      BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
      String s;
      final StringBuilder sb = new StringBuilder("");
      try {
        while ((s = in.readLine()) != null) {
          if (newline != null) {
            if (s == null || s.equals(newline)) {
              s = sb.toString();
            } else {
              sb.append(" " + s);
              continue;
            }
          }
          if (!s.isEmpty()) {
            objectReferenc.add(addObj(s));
          }
        }
        System.out.println(new GsonBuilder().create().toJson(objectReferenc));
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
   * Add key value pair to the Obj hash structure.
   * 
   * @param root
   * @param key
   * @param value
   */
  private Map<String, String> addObj(final String value) {
    final Map<String, String> ret = new HashMap<String, String>();
    if (split != null) {
      final String[] valueArray = value.split(split);
      for (int i = 0; i < valueArray.length; i++) {
        ret.put(String.valueOf(i), valueArray[i]);
      }//TODO; regex
    }
    return ret;
  }

  /**
   * Parse the argument and save them in global variable.
   * 
   * @param args
   * @param objectReferenc
   * @return
   */
  private ReturnValue parseArguments(final String[] args) {
    ReturnValue returnValue = ReturnValue.OK;
    for (String arg : args) {
      if (arg.startsWith(REGEX)) {
        regex = Pattern.compile(arg.substring(REGEX.length()));
      } else if (arg.startsWith(HELP)) {
        //printHelp(); TODO
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
      } else if (arg.startsWith(SPLIT)) {
        split = arg.substring(SPLIT.length());
      }
    }
    if (split != null && regex != null) {
      returnValue = ReturnValue.ERROR;
      System.err.println("Cannot define both split and regex");
    } else {
      split = " ";
    }
    return returnValue;
  }
  public static void main(String[] args) {
    System.exit(new ToStruct().init(args));
  }
}
