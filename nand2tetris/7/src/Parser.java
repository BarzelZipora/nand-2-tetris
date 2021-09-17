import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    /**
     * the types of the commands in vm language
     */
    enum CommandTypes {
        C_ARITHMETIC,
        C_POP,
        C_PUSH,
    }

    /**
     * contain all the arithmetic words
     */
    private List<String> arithmeticList;
    /**
     * contain the commands only, without spaces and comments
     */
    private List<String> pureCommandList;
    private int commandPointer;
    private String thisCommand;


//    /**
//     *
//     */
//    public Parser() {
//    }

    /**
     * initialize everything needed to start translating:
     * arithmeticList, commandPointer and pureCommandList;
     * @param filePath the path of the .vm file to be translated
     */
    public Parser(String filePath) {
        initArithmeticList();
        initCommandPointer();
        pureCommandList = new ArrayList();
        String line;
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            line = in.readLine();
            while (line != null) {
                System.out.println(line);
                if (line.equals("") || line.charAt(0) == '/' && line.charAt(1) == '/') {
                    line = in.readLine();
                    continue;
                }
                String[] splitRes = line.split("//");
                pureCommandList.add(splitRes[0]);
                line = in.readLine();
            }
            System.out.println();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * advance the pointer and update the current command.
     */
    public void advance() {
        commandPointer++;
        this.thisCommand = pureCommandList.get(commandPointer);
    }

    /**
     * checks whether the command is arithmetic word or not.
     * if it's an arithmetic word returns it
     * else returns the segment to be pushed/pop.
     */
    public String firstArgument() {
        if (checkCommandType() == CommandTypes.C_ARITHMETIC) {
            return thisCommand;
        }
        return thisCommand.split(" ")[1];
    }

    /**
     * @return the index to be pushed/pop.
     */
    public int secondArgument() {
        return Integer.parseInt(thisCommand.split(" ")[2]);
    }

    /**
     * initialize the arithmetic list with all the arithmetic words.
     */
    private void initArithmeticList() {
        arithmeticList = Arrays.asList("add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not");
    }

    /**
     * checks if there are more commands
     * @return true/false
     */
    public Boolean hasMoreCommands() {
        return commandPointer < pureCommandList.size() - 1;
    }

    /**
     *
     * @return the type of the command.
     */
    public CommandTypes checkCommandType() {
        if (arithmeticList.contains(thisCommand)) {
            return CommandTypes.C_ARITHMETIC;
        } else if (thisCommand.startsWith("push")) {
            return CommandTypes.C_PUSH;
        } else if (thisCommand.startsWith("pop")) {
            return CommandTypes.C_POP;
        }
        return null;
    }

    /**
     * initialize the command pointer to the start of the command list.
     */
    public void initCommandPointer() {
        commandPointer = -1;
    }
}
