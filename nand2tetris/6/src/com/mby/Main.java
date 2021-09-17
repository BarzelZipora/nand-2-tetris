package com.mby;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import static java.lang.System.*;

public class Main {

    public static void main(String[] args) {
            if (args.length == 0) {
                err.println("No source file was specified.");
                exit(1);
            }

            File sourceFile = new File(args[0].trim());
            if (!sourceFile.exists()) {
                err.println("Specified source file could not be found.");
                exit(2);
            }

        int fileNameIndex = sourceFile.getAbsolutePath().indexOf(sourceFile.getName());
        String sourceAbsolutePath = sourceFile.getAbsolutePath();
        String fileName = sourceFile.getName();
        int fileNameExtensionIndex = fileName.lastIndexOf(".");
        String sourceDirectory = sourceAbsolutePath.substring(0, fileNameIndex);
        String fileNameNoExtension = fileName.substring(0, fileNameExtensionIndex);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(sourceDirectory);
        stringBuilder.append(fileNameNoExtension);
        stringBuilder.append(".hack");
        String outputFilePath =
                stringBuilder.toString();
            File outputFile = new File(outputFilePath);

            try {
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                outputFile.createNewFile();

                long startTime = currentTimeMillis();

                Assembler assembler = new Assembler(sourceFile, outputFile);
                assembler.translate();

                long endTime = currentTimeMillis();
                long elaspedTime = endTime - startTime;

                try (StringWriter status = new StringWriter()) {
                    Arrays.asList("Conversion has completed successfully on ",
                            sourceAbsolutePath,
                            " -> ",
                            outputFilePath,
                            " while ",
                            Long.toString(elaspedTime),
                            "ms.").forEach(status::append);
                    out.println(status.toString());
                }
            } catch (IOException e) {
                err.println("An unknown I/O error occurred.");
                exit(3);
            }
        }
    }
