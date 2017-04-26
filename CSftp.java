import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.IllegalFormatException;
import java.net.SocketException;
import java.net.UnknownHostException;

//
// This is an implementation of a simplified version of a command
// line ftp client. The program always takes two arguments
//


public class CSftp
{
  static final int MAX_LEN = 255;
  static final int ARG_CNT = 2;

  public static void main(String [] args)

  {

    byte cmdString[] = new byte[MAX_LEN];

    // Get command line arguments and connected to FTP
    // If the arguments are invalid or there aren't enough of them
    // then exit.

    if (args.length != ARG_CNT) {
      System.out.print("Usage: cmd ServerAddress ServerPort\n");
      return;
    }

    String hostName = args[0];
    int portNumber = Integer.parseInt(args[1]);



    try {

      Socket prySocket = new Socket(hostName, portNumber);

      PrintWriter out =
      new PrintWriter(prySocket.getOutputStream(), true);
      BufferedReader pryIn =
      new BufferedReader(
        new InputStreamReader(prySocket.getInputStream()));

      BufferedReader stdIn =
      new BufferedReader(
        new InputStreamReader(System.in));
      String fromPryServer;
      String fromUser;
      String fromSecServer;

      fromPryServer = pryIn.readLine();
      System.out.println("<-- " + fromPryServer);


      Socket secSocket;


      String firstWord = null;
      String secondWord = null;

      int len = 1;

      while (len > 0) {
        firstWord = null;
        secondWord = null;
//      for (int len = 1; len > 0;) {
        System.out.print("csftp> ");
//        len = System.pryIn.read(cmdString);
//        if (len <= 0)
//          break;
        // Start processing the command here.
        fromUser = stdIn.readLine();
        if(fromUser.length() == 0) continue;
        if (Character.toString(fromUser.charAt(0)).equals("#") && fromUser.length()<= 255) {
          continue;
        }

        int spaces = fromUser.length() - fromUser.replace(" ", "").length();
        if (spaces > 1) {
          print901();
          continue;
        }

        if(fromUser.contains(" ")){
          firstWord= fromUser.substring(0, fromUser.indexOf(" "));
          secondWord = fromUser.substring(fromUser.indexOf(" ")+1, fromUser.length());
        } else {
          firstWord = fromUser;
          secondWord = "";
        }

        switch (firstWord) {
          case "user":
            if(secondWord != null && !secondWord.trim().isEmpty()) {
              System.out.println("--> " + fromUser);
              out.println("USER " + secondWord);
              fromPryServer = pryIn.readLine();
              System.out.println("<-- " + fromPryServer);
            } else {
              print901();
            }
            break;
          case "pw":
            if(secondWord != null&& !secondWord.trim().isEmpty()) {
              System.out.println("--> " + fromUser);
              out.println("PASS " + secondWord);
              fromPryServer = pryIn.readLine();
              System.out.println("<-- " + fromPryServer);
            } else {
              print901();
            }
            break;
          case "quit":
              System.out.println("--> " + fromUser);
              out.println("QUIT");
              fromPryServer = pryIn.readLine();
              System.out.println("<-- " + fromPryServer);
              len = 0;
            break;
          case "get":
            if(secondWord != null&& !secondWord.trim().isEmpty()) {
              System.out.println("--> " + fromUser);
              out.println("PASV");
              fromPryServer = pryIn.readLine();
              secSocket = openSecondConnection(fromPryServer);
              if (secSocket!=null) {
                  out.println("RETR " + secondWord);
                  InputStream secIn = secSocket.getInputStream();

                      OutputStream fileWriter;
                      fileWriter = new FileOutputStream(secondWord);
                      byte[] bytes = new byte[1024*1024*1024];

                      int count;
                      while ((count = secIn.read(bytes)) > 0) {
                          fileWriter.write(bytes, 0, count);
                      }
                  fileWriter.close();
                  secIn.close();
                  secSocket.close();
                  fromPryServer = pryIn.readLine();

                  StringBuilder list = new StringBuilder();
//                  while ((fromPryServer = pryIn.readLine()) != null) {
//                      list.append(fromPryServer);
//                      list.append('\n');
//                  }
                  System.out.println("<--" + fromPryServer);
                  fromPryServer = pryIn.readLine();
                  System.out.println("<--" + fromPryServer);
              } else {
                  System.out.println("<--" + fromPryServer);
              }

            } else {
              print901();
            }
            break;
          case "cd":
            if(secondWord != null&& !secondWord.trim().isEmpty()) {
              System.out.println("--> " + fromUser);
              out.println("CWD " + secondWord);
              fromPryServer = pryIn.readLine();
              System.out.println("<-- " + fromPryServer);
            } else {
              print901();
            }
            break;
          case "dir":
              System.out.println("--> " + fromUser);
              out.println("PASV");
              fromPryServer = pryIn.readLine();
//              System.out.println("<-- " + fromPryServer);
              secSocket = openSecondConnection(fromPryServer);
              if (secSocket!=null){
                  BufferedReader secIn =
                          new BufferedReader(
                                  new InputStreamReader(secSocket.getInputStream()));

                  out.println("LIST");
                  fromPryServer = pryIn.readLine();

                  StringBuilder list = new StringBuilder();
                  while ((fromSecServer = secIn.readLine()) != null) {
                      list.append(fromSecServer);
                      list.append('\n');
                  }
//              fromSecServer = secIn.readLine();
                  fromPryServer = pryIn.readLine();
                  System.out.println("<-- " + list.toString());
              } else {
                  System.out.println("<-- " + fromPryServer);
              }

            break;
          default:
              System.out.println("900 Invalid command");
              break;
        }

        if (fromPryServer.equals("Bye."))
          break;
      }
    } catch(UnknownHostException e) {
      print920(hostName, Integer.toString(portNumber));
    } catch (IllegalFormatException exception) {
      print998();
    } catch (IOException e) {
      print999(e.getMessage());
    }
  }

  public static Socket openSecondConnection(String response) throws IOException {
    Socket socket = null;
    String numbers = response.replaceAll("[^0-9]+", " ");
    String[] s = numbers.split("\\s+");
    List<Integer> myList = new LinkedList<Integer>();
    if (s.length > 2) {
        for(int index = 1 ; index < 7 ; index++) {
            myList.add(Integer.parseInt(s[index]));
        }
//    for (int i = 0; i < 6; i++) {
//      System.out.println(myList.get(i));
//    }
        String addr = myList.get(0).toString()+ "."+
                myList.get(1).toString()+"."+
                myList.get(2).toString()+"."+
                myList.get(3).toString();
        InetAddress ip = InetAddress.getByName(addr);
        int port = myList.get(4)*256 + myList.get(5);
//      System.out.println("address:" + ip.toString() + "   port: " + port);

        socket = null;
        try {socket = new Socket(ip,port);
        } catch (IOException e) {
          print925();
        }

    }

      return socket;
  }

  public static void print901() {
    System.out.println("901 Incorrect number of arguments");
  }

  public static void print910(String fileName) {
    String text = String.format("910 Access to local file %s denied", fileName);
    System.out.println(text);
  }

  public static void print920(String host, String port) {
    String text = String.format("920 Control connection to %s on port %s failed to open", host, port);
    System.out.println(text);
  }

  public static void print925() {
    System.err.println("925 Control connection I/O error, closing control connection");
  }

  public static void print930(String host, String port) {
    String text = String.format("930 Data transfer connection to %s on port %s failed to open", host, port);
    System.out.println(text);
  }

  public static void print935() {
    System.out.println("935 Data transfer connection I/O error, closing data connection");
  }

  public static void print998() {
    System.err.println("998 Input error while reading commands, terminating");
  }

  public static void print999(String error) {
    String text = String.format("999 Processing error. %s", error);
    System.out.println("999 Processing error. yyyy");
  }
}
