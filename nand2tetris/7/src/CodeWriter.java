import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {

    private BufferedWriter bufferedWriter;
    private String fileName;
    private int labelFlag;

    /**
     * @param filePath the path of the .vm file to be translated
     * creates a new file with .asm suffix,
     * save the name of the file without suffix into the variable fileName
     * and create a bufferWriter to write with to the .asm file the translation.
     */
    public CodeWriter(String filePath) {
        try {
            labelFlag = 0;
            File file = new File(filePath.replace(".vm", ".asm"));
            String tempFileName = file.getName();
            fileName = tempFileName.substring(0, tempFileName.lastIndexOf('.'));
            bufferedWriter = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param currentCommand the command that translated write now.
     * check which arithmetic word it is and behave as needed.
     * for each operation write the operation as a comment,
     * then writes the translation.
     */
    public void writeArithmeticCommand(String currentCommand) {
        try {
            bufferedWriter.newLine();
            switch (currentCommand) {
                case "add":
                    bufferedWriter.write("//add");
                    bufferedWriter.newLine();
                    binaryOperation1("+");
                    break;
                case "sub":
                    bufferedWriter.write("//sub");
                    bufferedWriter.newLine();
                    binaryOperation1("-");
                    break;
                case "neg":
                    bufferedWriter.write("//neg");
                    bufferedWriter.newLine();
                    negOperation();
                    break;
                case "eq":
                    bufferedWriter.write("//eq");
                    bufferedWriter.newLine();
                    binaryOperation1("-");
                    binaryOperation2("=");
                    break;
                case "gt":
                    bufferedWriter.write("//gt");
                    bufferedWriter.newLine();
                    binaryOperation1("-");
                    binaryOperation2(">");
                    break;
                case "lt":
                    bufferedWriter.write("//lt");
                    bufferedWriter.newLine();
                    binaryOperation1("-");
                    binaryOperation2("<");
                    break;
                case "and":
                    bufferedWriter.write("//and");
                    bufferedWriter.newLine();
                    binaryOperation1("&");
                    break;
                case "or":
                    bufferedWriter.write("//or");
                    bufferedWriter.newLine();
                    binaryOperation1("|");
                    break;
                case "not":
                    bufferedWriter.write("//not");
                    bufferedWriter.newLine();
                    notOperation();
                    break;
                default:
                    throw new RuntimeException("UnExcepted arithmetic");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param commandType the type of the current command.
     * @param segment the type of the variable.
     * @param index the index to be pushed/popped.
     * push or pop the index due to the segment.
     */
    public void writePushPop(Parser.CommandTypes commandType, String segment, int index) {
        try {
            bufferedWriter.newLine();
            if (commandType == Parser.CommandTypes.C_PUSH) {
                bufferedWriter.write("//push " + segment + " " + index);
                bufferedWriter.newLine();
                switch (segment) {
                    case "constant":
                        pushConstant("SP", index);
                        break;
                    case "local":
                        pushLATT("LCL", index);
                        break;
                    case "argument":
                        pushLATT("ARG", index);
                        break;
                    case "this":
                        pushLATT("THIS", index);
                        break;
                    case "that":
                        pushLATT("THAT", index);
                        break;
                    case "temp":
                        pushTemp(index);
                        break;
                    case "pointer":
                        pushPointer(index);
                        break;
                    case "static":
                        pushStatic(index);
                        break;
                }
            } else if (commandType == Parser.CommandTypes.C_POP) {
                bufferedWriter.write("//pop " + segment + " " + index);
                bufferedWriter.newLine();
                switch (segment) {
                    case "local":
                        popLATT("LCL", index);
                        break;
                    case "argument":
                        popLATT("ARG", index);
                        break;
                    case "this":
                        popLATT("THIS", index);
                        break;
                    case "that":
                        popLATT("THAT", index);
                        break;
                    case "temp":
                        popTemp(index);
                        break;
                    case "pointer":
                        popPointer(index);
                        break;
                    case "static":
                        popStatic(index);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * translate the not operation as follow.
     */
    private void notOperation() {
        try {
            //SP--
            decrementSP();
            //*SP
            AP("SP");
            //*SP = ! *SP
            bufferedWriter.write("M=!M");
            bufferedWriter.newLine();
            //SP++
            incrementSP();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * translate the negative operation as follow.
     */
    private void negOperation() {
        try {
            notOperation();
            //SP--
            decrementSP();
            //*SP
            AP("SP");
            //*SP = *SP + 1
            bufferedWriter.write("M=M+1");
            bufferedWriter.newLine();
            //SP++
            incrementSP();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param operator the current operator to translate.
     * pop the last value in the stack and save it into a variable D,
     * then doing the operation between D and the current last value in the stack.
     */
    private void binaryOperation1(String operator) {
        try {
            decrementSP();
            DEAP("SP");
            decrementSP();
            bufferedWriter.write("A=M");
            bufferedWriter.newLine();
            bufferedWriter.write("M=M" + operator + "D");
            bufferedWriter.newLine();
            incrementSP();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void binaryOperation2(String operator) {
        try {
            //true:0, false:-1
            int a = 0, b = 0, c = 0;
            switch (operator) {
                case "=":
                    a = -1;
                    break;
                case ">":
                    b = -1;
                    break;
                case "<":
                    c = -1;
                    break;
            }
            String flag = "LABELFLAG_";
            String label1 = flag + labelFlag++;
            String label2 = flag + labelFlag++;
            String label3 = flag + labelFlag++;
            String labelEnd = flag + labelFlag++;

            //SP--
            decrementSP();
            //D = *SP
            DEAP("SP");
            bufferedWriter.write("@" + label1);
            bufferedWriter.newLine();
            bufferedWriter.write("D;JEQ");
            bufferedWriter.newLine();
            bufferedWriter.write("@" + label2);
            bufferedWriter.newLine();
            bufferedWriter.write("D;JGT");
            bufferedWriter.newLine();
            bufferedWriter.write("@" + label3);
            bufferedWriter.newLine();
            bufferedWriter.write("D;JLT");
            bufferedWriter.newLine();

            bufferedWriter.write("(" + label1 + ")");
            bufferedWriter.newLine();

            AP("SP");
            bufferedWriter.write("M=" + a);
            bufferedWriter.newLine();
            JMP(labelEnd);

            bufferedWriter.write("(" + label2 + ")");
            bufferedWriter.newLine();

            AP("SP");
            bufferedWriter.write("M=" + b);
            bufferedWriter.newLine();
            JMP(labelEnd);

            bufferedWriter.write("(" + label3 + ")");
            bufferedWriter.newLine();

            AP("SP");
            bufferedWriter.write("M=" + c);
            bufferedWriter.newLine();
            JMP(labelEnd);

            bufferedWriter.write("(" + labelEnd + ")");
            bufferedWriter.newLine();
            //SP++
            incrementSP();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //*pointer
    private void AP(String pointer) {
        try {
            bufferedWriter.write("@" + pointer);
            bufferedWriter.newLine();
            bufferedWriter.write("A=M");
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * translate the jump.
     */
    private void JMP(String label) {
        try {
            bufferedWriter.write("@" + label);
            bufferedWriter.newLine();
            bufferedWriter.write("0;JMP");
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //D = *p
    private void DEAP(String pointer) {
        try {
            bufferedWriter.write("@" + pointer);
            bufferedWriter.newLine();
            bufferedWriter.write("A=M");
            bufferedWriter.newLine();
            bufferedWriter.write("D=M");
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //D = *(pointer+i)
    private void DEAPPlusi(String pointer, int index) {
        try {
            bufferedWriter.write("@" + pointer);
            bufferedWriter.newLine();
            bufferedWriter.write("D=M");
            bufferedWriter.newLine();
            bufferedWriter.write("@" + index);
            bufferedWriter.newLine();
            bufferedWriter.write("A=D+A");
            bufferedWriter.newLine();
            bufferedWriter.write("D=M");
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //*p = D
    private void APED(String pointer) {
        try {
            bufferedWriter.write("@" + pointer);
            bufferedWriter.newLine();
            bufferedWriter.write("A=M");
            bufferedWriter.newLine();
            bufferedWriter.write("M=D");
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param x the current index to be
     * and doing D = x.
     */
    private void DEX(int x) {
       String temp = String.valueOf(x);
        try {
            bufferedWriter.write("@" + temp);
            bufferedWriter.newLine();
            bufferedWriter.write("D=A");
            bufferedWriter.newLine();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    /**
     * @param x the current index to be
     * and doing D = x.
     */
    private void MED(String x) {
        try {
            bufferedWriter.write("@" + x);
            bufferedWriter.newLine();
            bufferedWriter.write("M=D");
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param address the current index to be
     * and doing D = address.
     */
    private void DEM(String address) {
        try {
            bufferedWriter.write("@" + address);
            bufferedWriter.newLine();
            bufferedWriter.write("D=M");
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * increment the stack pointer in 1.
     */
    private void incrementSP() {
        try {
            bufferedWriter.write("@SP");
            bufferedWriter.newLine();
            bufferedWriter.write("M=M+1");
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * decrement the stack pointer in 1.
      */
    private void decrementSP() {
        try {
            bufferedWriter.write("@SP");
            bufferedWriter.newLine();
            bufferedWriter.write("M=M-1");
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param segment the current segment .
     * @param index the place
     */
    private void pushConstant(String segment, int index) {
        //*pointer = index

        //D = index
        DEX(index);
        //*pointer = D
        APED(segment);
        //SP++
        incrementSP();
    }

    private void pushLATT(String pointer, int index) {
        //push local 8 => *SP = *(LCL + 8),SP++

        //D = *(LCL + 8)
        DEAPPlusi(pointer, index);
        //*SP = D
        APED("SP");
        //SP++
        incrementSP();
    }

    private void pushTemp(int index) {
        //push temp i => *SP = M[R(5+i)];,SP++

        //D = M[R(5+i)]
        DEM("R" + (index + 5));
        //*SP = D
        APED("SP");
        //SP++
        incrementSP();
    }

    private void pushPointer(int index) {
        //push pointer 0/1 => *SP = THIS/THAT,SP++

        //D = THIS/THAT
        if (index == 0) {
            DEM("THIS");
        } else {
            DEM("THAT");
        }
        //*SP = D
        APED("SP");
        //SP++
        incrementSP();
    }

    private void pushStatic(int index) {
        //push static i => D = M[fileName.i], *SP = D, SP++

        //D = M[fileName.i]
        DEM(fileName + "." + index);
        //*SP = D
        APED("SP");
        //SP++
        incrementSP();
    }

    private void popTemp(int index) {
        //pop temp i => SP--, M[R(5+i)] = *SP

        //SP--
        decrementSP();
        //D = *SP
        DEAP("SP");
        MED("R" + (5 + index));
    }

    private void popPointer(int index) {
        //pop pointer 0/1 => SP--, THIS/THAT = *SP

        //SP--
        decrementSP();
        //D = *SP
        DEAP("SP");
        //THIS/THAT = D
        if (index == 0) {
            MED("THIS");
        } else {
            MED("THAT");
        }
    }

    private void popLATT(String pointer, int index) {
        try {
            //address = pointer + i
            bufferedWriter.write("@" + pointer);
            bufferedWriter.newLine();
            bufferedWriter.write("D=M");
            bufferedWriter.newLine();
            bufferedWriter.write("@" + index);
            bufferedWriter.newLine();
            bufferedWriter.write("D=D+A");
            bufferedWriter.newLine();
            bufferedWriter.write("@addr");
            bufferedWriter.newLine();
            bufferedWriter.write("M=D");
            bufferedWriter.newLine();
            //SP--
            decrementSP();
            //*addr = *SP

            //D = *SP
            DEAP("SP");
            //*addr = D
            APED("addr");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * pop static variable as follow.
     */
    private void popStatic(int index) {
        //pop static i => D = stack.pop, @fileName.i, M = D

        //SP--
        decrementSP();
        //D = *SP
        DEAP("SP");
        //@fileName.i, M = D
        MED(fileName + "." + index);
    }

    /**
     * finish the program by an infinite loop.
     */
    public void close() {
        try {
            bufferedWriter.newLine();
            bufferedWriter.write("//end");
            bufferedWriter.newLine();
            bufferedWriter.write("(END)");
            bufferedWriter.newLine();
            bufferedWriter.write("@END");
            bufferedWriter.newLine();
            bufferedWriter.write("0;JMP");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
