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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ExecuteInThreads {

  /**
   * Execute in parallel. 
   * @param callables - A list of object to execute.
   * @param maxThreads - max threads executing in parallell.
   * @param seconds - timeout
   * @return - List of the result objects.
   * @throws IOException
   */
  public static <T> List<T> executeInThreads(final List<Callable<T>> callables, final int maxThreads, final long seconds) throws IOException {
    final List<Future<T>> threads = new ArrayList<Future<T>>(callables.size());
    final ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);
    final List<T> returnObject = new ArrayList<T>(callables.size());
    for (Callable<T> call : callables) {
      threads.add(executorService.submit(call));
    }

    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(seconds, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
        throw new IOException("Some threads hanged");
      }
    } catch (InterruptedException e) {
      new IOException("The thread timeout", e);
    }
    // Collect the result
    for (Future<T> thread : threads) {
      try {
        returnObject.add(thread.get());
      } catch (InterruptedException e) {
        e.printStackTrace(System.err);
      } catch (ExecutionException e) {
        e.printStackTrace(System.err);
      }
    }
    return returnObject;
  }
}
