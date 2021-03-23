
import java.lang.System;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class CSdict {

	static List<String> commandList = Arrays.asList("open", "dict", "set", "define", "match", "prefixmatch", "close", "quit");
	static PrintWriter out;
	static BufferedReader in;
	static BufferedReader stdIn;
	static String dictionary;

    private static String command;
    private static String[] arguments;


    static final int MAX_LEN = 255;
    static Boolean debugOn = false;
    private static final int PERMITTED_ARGUMENT_COUNT = 1;

    //main program
    public static void main(String [] args) {

        while (true) {

            byte cmdString[] = new byte[MAX_LEN];

            // Verify command line arguments
            if (args.length == PERMITTED_ARGUMENT_COUNT) {
                debugOn = args[0].equals("-d");
                if (debugOn) {
                  //System.err.println("Debugging output enabled");
                } else {
                    System.err.println("997 Invalid command line option - Only -d is allowed.");
                    return;
                }
            } else if (args.length > PERMITTED_ARGUMENT_COUNT) {
                System.err.println("996 Too many command line options - Only -d is allowed.");
                return;
            }

            try {

            	//read user input
                System.out.print("csdict> ");
                System.in.read(cmdString);
                String inputString = new String(cmdString, "ASCII");
                parseInput(inputString);

                //if user enters empty or commented input
                if(blankOrHashtag(command)) {
                	continue;
                }


                //if supplied command is not expected at the time
            	if (commandList.contains(command) && !command.equals("open") && !command.equals("quit")) {
                    System.err.println("903 Supplied command not expected at this time.");
                    continue;
            	}

                //user enters a command
                if (checkCommand(command,arguments)) {
                    switch (command) {
                        case "open":
                            openCommand(arguments[0], Integer.parseInt(arguments[1]));
                            break;
                        case "quit":
                            quitCommand();
                            break;
                        default:
                            System.err.println("999 Processing error. Unknown Command.");
                    	}
                	}

            	} catch (IOException exception) {
            		System.err.println("998 Input error while reading commands, terminating.");
            		System.exit(-1);
            	}
        	}
    }

    //parses user input from main program into commands and arguments
    private static void parseInput(String input) {
        String[] inputs = input.trim().split("( |\t)+");
        command = inputs[0].toLowerCase().trim();
        arguments = Arrays.copyOfRange(inputs, 1, inputs.length);

    }

    //terminates the main program
    private static void quitCommand() {
    	System.exit(0);
    }


    //when client types the 'close' command
    private static void closeCommand() throws IOException {
    	String close = "QUIT";
    	out.println(close);
    	if(debugOn) {
            System.out.println("> " + close);
     	 }
    }

    //when client types 'dict' command
    private static void dictCommand() {
    	String dict = "SHOW DB";
        out.println(dict);
    	if(debugOn) {
        	System.out.println("> " + dict);
    	}
    }

    //when client types 'define' command
    private static void defineCommand(String word, String dictionary) {
    	String define = "DEFINE " + dictionary + " " + word;
        out.println(define);
        if(debugOn) {
        	System.out.println("> " + define);
    	}
    }

    //when client types 'match' command
    private static void matchCommand(String word, String dictionary, String option) {
        String match = "MATCH " + dictionary + " " + option + " " + word;
        out.println(match);
        if(debugOn) {
        	System.out.println("> " + match);
    	}
    }


    //provides the header output for found definitions using 'define' command
    private static void parseDefinition(String def) {
    	String [] splitDefinition = def.split("( |\\t)", 4);
    	System.out.println("@ " + splitDefinition[2] + " " + splitDefinition[3]);
    }


    //opens a new Socket connecting client to dictionary server
    private static void openCommand(String hostname, int portnumber) {

        Socket dictSocket;
        dictionary = "*";

		try {

			 //create socket connection to server
			 InetSocketAddress address = new InetSocketAddress(hostname, portnumber);
			 dictSocket = new Socket();
			 dictSocket.connect(address, 30000);

			 out = new PrintWriter(dictSocket.getOutputStream(), true);
			 in = new BufferedReader(new InputStreamReader(dictSocket.getInputStream()));
			 stdIn = new BufferedReader(new InputStreamReader(System.in));

			 try {
				 //successful connection status code
				 String connectionSuccessString = in.readLine();
				 if(debugOn) {
					 System.out.println("<-- " + connectionSuccessString);
				 }
			 } catch(IOException e) {
				 System.err.println("925 Control connection I/O error, closing control connection.");
				 dictSocket.close();
				 return;
			 }

			 loop: while (true) {
	                 try {

	                	  //get user input
					 	  System.out.print("csdict> ");
	                	  String userInput = "";
	                	  userInput = stdIn.readLine();
	                      parseInput(userInput);

	                      //if user enters blank or commented input
	                      if(blankOrHashtag(command)) {
	                     	 continue;
	                      }

	                      //if user tries the 'open' command while connection is active
	                      if (command.equals("open")) {
	                          System.err.println("903 Supplied command not expected at this time.");
	                          continue;
	                  	  }

	                      if (checkCommand(command,arguments)) {
	                          switch (command) {
	                              case "dict":
	                                  dictCommand();
	                                  retrieveResponse("dict");
	                                  break;
	                              case "quit":
	                            	  closeCommand();
	                            	  retrieveResponse("close");
	                                  quitCommand();
	                                  break;
	                              case "close":
	                                  closeCommand();
	                                  retrieveResponse("close");
	                                  break loop;
	                              case "define":
	                                  defineCommand(arguments[0], dictionary);
	                                  retrieveResponse("define");
	                                  break;
	                              case "set":
	                                  dictionary = arguments[0];
	                                  break;
	                              case "match":
	                                  matchCommand(arguments[0], dictionary, "exact");
	                                  retrieveResponse("match");
	                                  break;
	                              case "prefixmatch":
	                                  matchCommand(arguments[0], dictionary, "prefix");
	                                  retrieveResponse("prefixmatch");
	                                  break;
	                          }
	                      }
			        	  } catch(IOException e) {
			        			System.err.println("925 Control connection I/O error, closing control connection.");
			        			dictSocket.close();
			        			return;
			        		}
	             }
			} catch (IOException e) {
				System.err.println("920 Control connection to " + hostname + " on port " + portnumber + " failed to open." );
			} catch (Exception e) {
				System.err.println("999 Processing error. Socket failed to connect.");
			}
    }

   //retrieves response provided by server
    static void retrieveResponse(String commandString) throws IOException {

        String response = "";
	    try {
	        switch (commandString) {
	        	case "dict":
	                    while ((response = in.readLine()) != null) {

	                    	String[] responses = response.trim().split("( |\t)+");
	                    	if(responses[0].startsWith("110")) {
	                    		if(debugOn) {
	                    			System.out.println("<-- " + response);
	                    		}
	                    		continue;
	                    	} else if(responses[0].startsWith("554")) {
	                    		if(debugOn) {
	                    			System.out.println("<-- " + response);
	                    		}
	                    		break;
	                    	} else if(response.equals(".")) {
	                    		if(debugOn) {
	                    			System.out.println("<-- " + in.readLine());
	                    		}
	                    		System.out.println(response);
	                    		break;
	                    	} else {
	                        	System.out.println(response);
	                    	}
	                    }
	        		break;
	        	case "close":
	        		if(debugOn) {
	        			System.out.println("<-- " + in.readLine());
	        		}
	        		break;
	        	case "define":
	                    while ((response = in.readLine()) != null) {

	                    	String[] responses = response.trim().split("( |\t)+");
	                    	if(responses[0].startsWith("151")) {
	                    		if(debugOn) {
	                    			System.out.println("<-- " + response);
	                    		}
	                    		parseDefinition(response);
	                    		continue;
	                    	} else if(responses[0].startsWith("552") || responses[0].startsWith("550")) {
	                    		if(debugOn) {
	                    			System.out.println("<-- " + response);
	                    		}
	                    		System.out.println("***No definition found***");

	                    		//try a match using server's default matching strategy
	                    		matchCommand(arguments[0], dictionary, ".");
	                    		String subResponse = in.readLine();
	                    		String[] subResponses = subResponse.trim().split("( |\t)+");

	                    		if(subResponses[0].startsWith("552") || subResponses[0].startsWith("550")){
	                    			if(debugOn) {
	                        			System.out.println("<-- " + response);
	                        		}
	                    			System.out.println("****No matches found****");
	                    		} else {
	                    			retrieveResponse("match");
	                    		}
	                    		break;
	                    	} else if(responses[0].startsWith("150")) {
	                    		if(debugOn) {
	                    			System.out.println("<-- " + response);
	                    		}
	                    		continue;
	                    	} else if(response.startsWith("250")) {
	                    		if(debugOn) {
	                    			System.out.println("<-- " + response);
	                    		}
	                    		break;
	                    	} else {
	                    		System.out.println(response);
	                    	}
	                    }
	        		break;
	        	case "match":
	                    while ((response = in.readLine()) != null) {

	                    	String[] responses = response.trim().split("( |\t)+");
	                    	if(responses[0].startsWith("152")) {
	                    		if(debugOn) {
	                    			System.out.println("<-- " + response);
	                    		}
	                    		continue;
	                    	} else if(responses[0].startsWith("552") || responses[0].startsWith("550")) {
	                    		if(debugOn) {
	                    			System.out.println("<-- " + response);
	                    		}
	                    		System.out.println("*****No matching word(s) found*****");
	                    		break;
	                    	} else if(response.startsWith("250")) {
	                    		if(debugOn) {
	                    			System.out.println("<-- " + response);
	                    		}
	                    		break;
	                    	} else {
	                    		System.out.println(response);
	                    	}
	                    }
	        		break;
	        	case "prefixmatch":
	                    while ((response = in.readLine()) != null) {

	                    	String[] responses = response.trim().split("( |\t)+");
	                    	if(responses[0].startsWith("152")) {
	                    		if(debugOn) {
	                    			System.out.println("<-- " + response);
	                    		}
	                    		continue;
	                    	} else if(responses[0].startsWith("552") || responses[0].startsWith("550")) {
	                    		if(debugOn) {
	                    			System.out.println("<-- " + response);
	                    		}
	                    		System.out.println("****No matching word(s) found****");
	                    		break;
	                    	} else if(response.startsWith("250")) {
	                    		if(debugOn) {
	                    			System.out.println("<-- " + response);
	                    		}
	                    		break;
	                    	}else {
	                    		System.out.println(response);
	                    	}
	                    }
	        		break;
	         default:
	        	 System.err.println("999 Processing error. Faulty command string parameter.");
	        }
	    } catch(IOException e) {
	    	throw e;
	    }

    }

    //checks if the command has valid number of arguments and the type of arguments are correct
    static boolean checkCommand(String command, String[] args){
		switch (command) {
		case "open":
			if (args.length != 2) {
                System.err.println("901 Incorrect number of arguments.");
                return false;
            } else if (!isInt(args[1])) {
                System.err.println("902 Invalid argument.");
                return false;
            }
			break;
		case "set":
		case "define":
		case "match":
		case "prefixmatch":
			if(args.length != 1) {
                System.err.println("901 Incorrect number of arguments.");
                return false;
            }
			break;
			case "close":
            case "quit":
            case "dict":
			if(args.length != 0) {
                System.err.println("901 Incorrect number of arguments.");
                return false;
            }
			break;
            default:
                System.err.println("900 Invalid command.");
                return false;
		}

    	return true;
    }

    //checks if command is blank or starts with a pound symbol "#"
    static boolean blankOrHashtag(String command) {
    	if(command.isBlank() || command.substring(0,1).equals("#")) {
    		return true;
    	}
    	return false;
    }

    //checks if string is an integer value
    static boolean isInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

}
