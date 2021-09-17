package com.mby;

import java.io.*;

public class Parser {
    private BufferedReader buffer;
    private String thisLine;
    private String nextCommand;

    public Parser(File source) {
        if (source == null) {
            throw new NullPointerException("source");
        }
        if (!source.exists()) {
            try {
                throw new FileNotFoundException(source.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            this.buffer = new BufferedReader(new FileReader(source));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.thisLine = null;
        try {
            this.nextCommand = this.getNextCommand();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getNextCommand() throws IOException {
        String nextCommand;

        do {
            nextCommand = this.buffer.readLine();

            if (nextCommand == null) {
                return null;
            }
        } while (nextCommand.trim().isEmpty() || this.isComment(nextCommand));

        int commentIndex = nextCommand.indexOf("//");
        if (commentIndex != -1) {
            nextCommand = nextCommand.substring(0, commentIndex - 1);
        }

        return nextCommand;
    }

    private boolean isComment(String input) {
        return input.trim().startsWith("//");
    }

    @Override
    public void finalize() {
        this.close();
    }

    public void close() {
        try {
            this.buffer.close();
        } catch (IOException e) {
        }
    }

    public boolean hasMoreCommands() {
        return (this.nextCommand != null);
    }

    public void advance() throws IOException {
        this.thisLine = this.nextCommand;
        this.nextCommand = this.getNextCommand();
    }

    public CommandType commandType() {
        String netLine = this.thisLine.trim();

        if (netLine.startsWith("(") && netLine.endsWith(")")) {
            return CommandType.L_COMMAND;
        } else if (netLine.startsWith("@")) {
            return CommandType.A_COMMAND;
        } else {
            return CommandType.C_COMMAND;
        }
    }

    // Returns the symbol or decimal Xxx of the current command @Xxx or (Xxx).
    // Should be called only when commandType() is A_COMMAND or L_COMMAND.
    public String symbol() {
        String netLine = this.thisLine.trim();

        if (this.commandType().equals(CommandType.L_COMMAND)) {
            return netLine.substring(1, this.thisLine.length() - 1);
        } else if (this.commandType().equals(CommandType.A_COMMAND)) {
            return netLine.substring(1);
        } else {
            return null;
        }
    }

    public String dest() {
        String netLine = this.thisLine.trim();
        int destIndex = netLine.indexOf("=");

        if (destIndex == -1) {
            return null;
        } else {
            return netLine.substring(0, destIndex);
        }
    }

    public String comp() {
        String netLine = this.thisLine.trim();
        int destIndex = netLine.indexOf("=");
        if (destIndex != -1) {
            netLine = netLine.substring(destIndex + 1);
        }
        int compIndex = netLine.indexOf(";");

        if (compIndex == -1) {
            return netLine;
        } else {
            return netLine.substring(0, compIndex);
        }
    }

    public String jump() {
        String netLine = this.thisLine.trim();
        int compIndex = netLine.indexOf(";");

        if (compIndex == -1) {
            return null;
        } else {
            return netLine.substring(compIndex + 1);
        }
    }
}
