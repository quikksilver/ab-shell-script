/**
 * 
 */
package ab.json.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Axel Bengtsson (axel.bengtsson@gmail.com)
 *
 */
public class ReadMain {

	private static final String REGEX = "--regex=";
	private static final String HELP = "--help";


	public int init(final String[] args) {
		// Read the arguments
		final List<String> objectReferenc = new ArrayList<String>();
		Pattern regex = null;
		for (String arg : args) {
			if (arg.startsWith(REGEX)) {
				regex = Pattern.compile(arg.substring(REGEX.length()));
			} else if (arg.startsWith(HELP)){
				System.out.println("Usage:");
				System.out.println("echo foo baa | tojson n1 n2");
				System.out.println("{n1 : \"foo\", n2 : \"baa\"}");
				System.out.println("echo '<foo name=\"baa\" age=\"12\"' | tojson --regex=.*name=(.+).*age=(.+).* name age");
				System.out.println("{name : \"baa\", age : \"12\"}");
			} else {
				objectReferenc.add(arg);
			}
		}
		// Start reading from stdin.
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    String s;
    try {
    	while ((s = in.readLine()) != null) {
				StringBuilder output = new StringBuilder();
				output.append('{');
    		if (regex != null) {
    			Matcher m = regex.matcher(s);
    			if (m.matches()) {
    				for (int i = 0 ; i < objectReferenc.size() ; ++i) {
    					output.append(objectReferenc.get(i) + " : \"" + m.group(i + 1)+ "\", ");
    				}
    			}
    		} else {
    			String[] groups = s.split(" ");
   				for (int i = 0 ; i < objectReferenc.size() ; ++i) {
  					output.append(objectReferenc.get(i) + " : \"" + groups[i]+ "\", ");
  				}
    		}
				output.deleteCharAt(output.length() - 2);
				output.deleteCharAt(output.length() - 1);
				output.append('}');
				System.out.println(output.toString());
    	}
	  } catch (IOException e) {
	  	e.printStackTrace(System.err);
	  	return 1;
	  } finally {
	  	try {
	      in.close();
      } catch (IOException e) {
	      e.printStackTrace(System.err);
      }
	  }
		return 0;
	}

	public static void main(String[] args) {
		System.exit(new ReadMain().init(args));
	}

}
