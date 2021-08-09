# Dictionary-Client

This is a simple dictionary client following the [RFC 2229](https://datatracker.ietf.org/doc/html/rfc2229). It can be used to connect to 
dictionary servers on a specific port to retrieve definitions of words. This program uses the command line to interact with the
connected server.

## Project Setup

1. Clone the repo.
2. Compile the `CSDict.java` file using the command `javac CSdict.java`
3. Run the program with the command `java -jar CSdict.jar`. You may add the optional `-d` flag to print status codes and responses.
4. Connect to a dictionary server and port using the `open` command. For example, `open dict.org 2628`.

## Available Commands

| Command | Description |
| --- | --- |
| `dict` | Prints the list of all dictionaries the server supports. |
| `set <DICTIONARY>` | Set the dictionary to retrieve definitions from. |
| `define <WORD>` | Retrieve and print all definitions of WORD. |
| `match <WORD>` | Retrieve and print all the exact matches for WORD.  |
| `prefixmatch <WORD>` | Retrieve and print all the prefix matches for WORD.  |
| `close` | Closes established connection. |
| `quit` | Closes established connection and quits the program. |
