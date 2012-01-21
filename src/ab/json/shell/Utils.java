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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Axel Bengtsson (axel.bengtsson@gmail,com)
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

  public static class Obj {
    public Map<String, String> values = new HashMap<String, String>();
    public Map<String, Obj> valuesObj = new HashMap<String, Obj>();

    @Override
    public String toString() {
      return values.toString() + " " + valuesObj.toString();
    }
  }
  
  private static Gson gson = null;
  private static JsonParser parser = null;

  public static Obj stringToObj(final String input) {
    if (gson == null) {
      gson = new Gson();
    }
    if (parser == null) {
      parser = new JsonParser();
    }
    Obj root = parser(input);
    return root;
  }
  private static Obj parser(final String stringObj) {
    JsonObject jObject = parser.parse(stringObj).getAsJsonObject();
    Obj current = null;
    if (jObject.isJsonObject()) {
      current = new Obj();
      for (Entry<String, JsonElement> entry : jObject.entrySet()) {
        if (entry.getValue().isJsonPrimitive()) {
          current.values.put(entry.getKey(), entry.getValue().getAsString());
        } else if (entry.getValue().isJsonObject()) {
          current.valuesObj.put(entry.getKey(),parser(entry.getValue().toString()));
        } else {
          System.err.println("Not supported get: " + entry.getValue().toString());
        }
      }
    }
    return current;
  }
}
