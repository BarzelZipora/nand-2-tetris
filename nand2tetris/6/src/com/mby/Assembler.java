package com.mby;

import java.io.*;

public class Assembler {
    private File assembly;
    private BufferedWriter codeMachine;
    private Code encoder = new Code();
    private SymbolTable symbolTable = new SymbolTable();

    public Assembler(File source, File target) {
        this.assembly = source;

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(target);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.codeMachine = new BufferedWriter(fileWriter);

    }

    public void translate() throws IOException {
        this.recordLabelAddress();
        this.parse();
    }

    private void recordLabelAddress() throws IOException {
        Parser parser = new Parser(this.assembly);
        while (parser.hasMoreCommands()) {
            try {
                parser.advance();
            } catch (IOException e) {
                e.printStackTrace();
            }

            CommandType commandType = parser.commandType();

            if (commandType.equals(CommandType.L_COMMAND)) {
                String symbol = parser.symbol();
                int address = this.symbolTable.getProgramAddress();
                this.symbolTable.addEntry(symbol, address);
            } else {
                this.symbolTable.incrementProgramAddress();
            }
        }
        parser.close();
    }

    private void parse() throws IOException {
        Parser parser = new Parser(this.assembly);
        while (parser.hasMoreCommands()) {
            try {
                parser.advance();
            } catch (IOException e) {
                e.printStackTrace();
            }

            CommandType commandType = parser.commandType();
            String command = null;

            if (commandType.equals(CommandType.A_COMMAND)) {
                String symbol = parser.symbol();
                Character firstCharacter = symbol.charAt(0);
                boolean isSymbol = (!Character.isDigit(firstCharacter));

                String address = null;
                if (isSymbol) {
                    boolean symbolExists = this.symbolTable.contains(symbol);

                    if (!symbolExists) {
                        int dataAddress = this.symbolTable.getDataAddress();
                        this.symbolTable.addEntry(symbol, dataAddress);
                        this.symbolTable.incrementDataAddress();
                    }

                    address = Integer.toString(
                            this.symbolTable.getAddress(symbol));
                } else {
                    address = symbol;
                }

                command = this.formatAcommand(address);
            } else if (commandType.equals(CommandType.C_COMMAND)) {
                String comp = parser.comp();
                String dest = parser.dest();
                String jump = parser.jump();
                command = this.formatCcommand(comp, dest, jump);
            }

            if (!commandType.equals(CommandType.L_COMMAND)) {
                try {
                    this.codeMachine.write(command);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    this.codeMachine.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        parser.close();
        try {
            this.codeMachine.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.codeMachine.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatAcommand(String address) {
        String formattedNumber = this.encoder.formatNumberAsBinary(address);
        return "0" + formattedNumber;
    }

    // Machine-format a C-command.
    private String formatCcommand( String comp, String dest, String jump) {
        StringWriter command = new StringWriter();
        command.append("111");
        command.append(this.encoder.comp(comp));
        command.append(this.encoder.dest(dest));
        command.append(this.encoder.jump(jump));
        return command.toString();
    }
}
