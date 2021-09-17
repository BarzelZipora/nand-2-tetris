
public class Main {
    public static void main(String[] args) {
        Parser parser = new Parser(args[0]);
        CodeWriter codeWriter = new CodeWriter(args[0]);
        while (parser.hasMoreCommands()) {
            parser.advance();
            switch (parser.checkCommandType()) {
                case C_ARITHMETIC:
                    codeWriter.writeArithmeticCommand(parser.firstArgument());
                    break;
                case C_PUSH:
                    codeWriter.writePushPop(Parser.CommandTypes.C_PUSH,parser.firstArgument(),parser.secondArgument());
                    break;
                case C_POP:
                    codeWriter.writePushPop(Parser.CommandTypes.C_POP,parser.firstArgument(),parser.secondArgument());
                    break;
                default:
                    return;
            }
        }
        codeWriter.close();
    }
}

