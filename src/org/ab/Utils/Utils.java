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

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * @author Axel Bengtsson (axel.bengtsson@gmail.com)
 *
 */
public final class Utils {
  public final static String FILE = "--file=";
  public static final String HELP = "--help";

  public enum ReturnValue {
    OK(0), HELP(0), ERROR(1);
    private final int exitValue;
    private ReturnValue(final int exitValue) {
      this.exitValue = exitValue;
    }
    public int toInt() {
      return exitValue;
    }
  };

  public static class ExecuteCommandInThread implements Callable<ExecuteCommandInThread> {
    final String[] cmd;
    String errorMessage  = null;
    String stdOut = null;

    public ExecuteCommandInThread(final String[] cmd) {
      this.cmd = cmd;
    }

    @Override
    public ExecuteCommandInThread call() throws Exception {
      try {
        stdOut = ExecuteCommand.execCmd(cmd);
      } catch (IOException e) {
        errorMessage = e.getMessage();
      }
      return this;
    }
  }
}
