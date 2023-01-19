import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Assembler {
  static String returnRegAddress(String regName) {
    String str = "";
    if (regName.equalsIgnoreCase("$t0")) {
      str = "1";
    } else if (regName.equalsIgnoreCase("$t1")) {
      str = "2";
    } else if (regName.equalsIgnoreCase("$t2")) {
      str = "3";
    } else if (regName.equalsIgnoreCase("$t3")) {
      str = "4";
    } else if (regName.equalsIgnoreCase("$t4")) {
      str = "5";
    } else if (regName.equalsIgnoreCase("$t5")) {
      str = "6";
    } else if (regName.equalsIgnoreCase("$zero")) {
      str = "0";
    } else if (regName.equalsIgnoreCase("$sp")) {
      str = "7";
    }
    return str;
  }

  public static void main(String[] args) throws IOException {
    String inputFile = "/home/tanvir/Downloads/test.asm";
    String outputFile = "/home/tanvir/Downloads/instructions.hex";
    FileWriter fileWriter = null;
    HashMap<String, Integer> labelAddress = new HashMap<>();
    int line_count = 1;
    int instruction_count = 1;
    try {
      fileWriter = new FileWriter(outputFile);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    Scanner scanner2 = null;
    try {
      scanner2 = new Scanner(new File(inputFile));
    } catch (FileNotFoundException e) {
      System.out.println("File not found");
    }
    while (scanner2.hasNextLine()) {
      String ins = scanner2.nextLine();
      line_count++;
      StringTokenizer stringTokenizer = new StringTokenizer(ins, " ,");
      if (stringTokenizer.countTokens() == 2) {
        String op = stringTokenizer.nextToken();
        if (op.equalsIgnoreCase("push")) {
          if (stringTokenizer.nextToken().contains("(")) {
            line_count += 2;
          } else
            line_count++;
        } else if (op.equalsIgnoreCase("pop")) {
          line_count++;
        }
      } else if (stringTokenizer.countTokens() == 1) {
        line_count--;
        String labelName = stringTokenizer.nextToken();
        StringTokenizer sTokenizer = new StringTokenizer(labelName, ":");
        String label = sTokenizer.nextToken();
        labelAddress.put(label, line_count);
      }
      // line_count++;
    }
    scanner2.close();
    Scanner scanner = new Scanner(new File(inputFile));
    fileWriter.write("v2.0 raw\n");    
    while (scanner.hasNextLine()) {
      String instruction = scanner.nextLine();
      if (instruction.equals(""))
        continue;
      instruction_count++;
      StringTokenizer stringTokenizer = new StringTokenizer(instruction, ", ");
      int ins_count = stringTokenizer.countTokens();
      if (ins_count == 1)
        instruction_count--;
      else if (ins_count == 2) {
        String instruc = stringTokenizer.nextToken();
        if (instruc.equalsIgnoreCase("j")) {
          String labelName = stringTokenizer.nextToken();
          int jumpAddress = labelAddress.get(labelName);
          // System.out.println(jumpAddress);
          fileWriter.append("4");
          if (jumpAddress < 16)
            fileWriter.append("0" + Integer.toHexString(jumpAddress));
          else
            fileWriter.append(Integer.toHexString(jumpAddress));
          fileWriter.append("00\n");
        } 
        else if (instruc.equalsIgnoreCase("push")) {
          String regName = stringTokenizer.nextToken();
          // converting the push statement into multiple instructions
          if (regName.contains("(")) {
            StringTokenizer sTokenizer = new StringTokenizer(regName, "()");
            int offset = Integer.parseInt(sTokenizer.nextToken());
            String reg = sTokenizer.nextToken();
            fileWriter.append("2");
            fileWriter.append(returnRegAddress(reg));
            fileWriter.append(returnRegAddress("$t5"));
            if (offset < 16)
              fileWriter.append("0" + Integer.toHexString(offset) + "\n");
            else
              fileWriter.append(Integer.toHexString(offset) + "\n");
            fileWriter.append("9");
            fileWriter.append(returnRegAddress("$sp"));
            fileWriter.append(returnRegAddress("$t5"));
            fileWriter.append("00\n");
            fileWriter.append("5");
            fileWriter.append(returnRegAddress("$sp") + returnRegAddress("$sp"));
            fileWriter.append("01\n");
            instruction_count += 2;
          } else {
            fileWriter.append("9");
            fileWriter.append(returnRegAddress("$sp"));
            fileWriter.append(returnRegAddress(regName));
            fileWriter.append("00\n");
            fileWriter.append("5");
            fileWriter.append(returnRegAddress("$sp"));
            fileWriter.append(returnRegAddress("$sp"));
            fileWriter.append("01\n");
            instruction_count++;
          }
        } 
        else if (instruc.equalsIgnoreCase("pop")) {
          String regName = stringTokenizer.nextToken();
          // converting the pop statement into multiple instructions
          fileWriter.append("d");
          fileWriter.append(returnRegAddress("$sp"));
          fileWriter.append(returnRegAddress("$sp"));
          fileWriter.append("01\n");
          fileWriter.append("2");
          fileWriter.append(returnRegAddress("$sp"));
          fileWriter.append(returnRegAddress(regName));
          fileWriter.append("00\n");
          instruction_count++;
        }
      } 
      else if (ins_count == 3) {
        String op = stringTokenizer.nextToken();
        if (op.equalsIgnoreCase("lw")) {
          String destReg = stringTokenizer.nextToken();
          String string = stringTokenizer.nextToken();
          fileWriter.append("2");
          StringTokenizer sTokenizer = new StringTokenizer(string, "()");
          int offset = Integer.parseInt(sTokenizer.nextToken());
          String baseAddress = sTokenizer.nextToken();
          fileWriter.append(returnRegAddress(baseAddress));
          fileWriter.append(returnRegAddress(destReg));
          if (offset < 16)
            fileWriter.append("0" + Integer.toHexString(offset) + "\n");
          else
            fileWriter.append(Integer.toHexString(offset) + "\n");
        } 
        else {
          fileWriter.append("9");
          String srcReg = stringTokenizer.nextToken();
          String string2 = stringTokenizer.nextToken();
          StringTokenizer sTokenizer = new StringTokenizer(string2, "()");
          int offset = Integer.parseInt(sTokenizer.nextToken());
          String destAddress = sTokenizer.nextToken();
          fileWriter.append(returnRegAddress(destAddress));
          fileWriter.append(returnRegAddress(srcReg));
          if (offset < 16)
            fileWriter.append("0" + Integer.toHexString(offset) + "\n");
          else
            fileWriter.append(Integer.toHexString(offset) + "\n");
        }
      }
      else if (ins_count == 4) {
        String operation = stringTokenizer.nextToken();
        String destReg = stringTokenizer.nextToken();
        String srcReg1 = stringTokenizer.nextToken();
        if (operation.contains("i")) {
          int immediate = Integer.parseInt(stringTokenizer.nextToken());
          if (operation.equals("addi")) {
            fileWriter.append("d");
            fileWriter.append(returnRegAddress(srcReg1));
            fileWriter.append(returnRegAddress(destReg));
            if (immediate < 16)
              fileWriter.append("0" + Integer.toHexString(immediate) + "\n");
            else
              fileWriter.append(Integer.toHexString(immediate) + "\n");
          } else if (operation.equalsIgnoreCase("subi")) {
            fileWriter.append("5");
            fileWriter.append(returnRegAddress(srcReg1));
            fileWriter.append(returnRegAddress(destReg));
            if (immediate < 16)
              fileWriter.append("0" + Integer.toHexString(immediate) + "\n");
            else
              fileWriter.append(Integer.toHexString(immediate) + "\n");
          } else if (operation.equalsIgnoreCase("andi")) {
            fileWriter.append("3");
            fileWriter.append(returnRegAddress(srcReg1));
            fileWriter.append(returnRegAddress(destReg));
            if (immediate < 16)
              fileWriter.append("0" + Integer.toHexString(immediate) + "\n");
            else
              fileWriter.append(Integer.toHexString(immediate) + "\n");
          } else {
            fileWriter.append("b");
            fileWriter.append(returnRegAddress(srcReg1));
            fileWriter.append(returnRegAddress(destReg));
            if (immediate < 16)
              fileWriter.append("0" + Integer.toHexString(immediate) + "\n");
            else
              fileWriter.append(Integer.toHexString(immediate) + "\n");
          }
        } 
        else {
          if (operation.equalsIgnoreCase("add")) {
            String srcReg2 = stringTokenizer.nextToken();
            fileWriter.append("e");
            fileWriter.append(returnRegAddress(srcReg1));
            fileWriter.append(returnRegAddress(srcReg2));
            fileWriter.append(returnRegAddress(destReg) + "0\n");
          } 
          else if (operation.equalsIgnoreCase("sub")) {
            String srcReg2 = stringTokenizer.nextToken();
            fileWriter.append("a");
            fileWriter.append(returnRegAddress(srcReg1));
            fileWriter.append(returnRegAddress(srcReg2));
            fileWriter.append(returnRegAddress(destReg) + "0\n");
          } 
          else if (operation.equalsIgnoreCase("and")) {
            String srcReg2 = stringTokenizer.nextToken();
            fileWriter.append("1");
            fileWriter.append(returnRegAddress(srcReg1));
            fileWriter.append(returnRegAddress(srcReg2));
            fileWriter.append(returnRegAddress(destReg) + "0\n");
          } 
          else if (operation.equalsIgnoreCase("or")) {
            String srcReg2 = stringTokenizer.nextToken();
            fileWriter.append("c");
            fileWriter.append(returnRegAddress(srcReg1));
            fileWriter.append(returnRegAddress(srcReg2));
            fileWriter.append(returnRegAddress(destReg) + "0\n");
          } 
          else if (operation.equalsIgnoreCase("nor")) {
            String srcReg2 = stringTokenizer.nextToken();
            fileWriter.append("8");
            fileWriter.append(returnRegAddress(srcReg1));
            fileWriter.append(returnRegAddress(srcReg2));
            fileWriter.append(returnRegAddress(destReg) + "0\n");
          } 
          else if (operation.equalsIgnoreCase("beq")) {
            String targetAddress = stringTokenizer.nextToken();
            int offset = labelAddress.get(targetAddress) - instruction_count;
            // System.out.println(offset);
            fileWriter.append("7");
            fileWriter.append(returnRegAddress(destReg));
            fileWriter.append(returnRegAddress(srcReg1));
            if (offset < 0) {
              String negOff = Integer.toHexString(offset);
              String finalOffset = negOff.substring(6, 8);
              fileWriter.append(finalOffset);
            } else {
              if (offset < 16)
                fileWriter.append("0" + Integer.toHexString(offset) + "\n");
              else
                fileWriter.append(Integer.toHexString(offset));
            }
          } 
          else if (operation.equalsIgnoreCase("bneq")) {
            String targetAddress = stringTokenizer.nextToken();
            int offset = labelAddress.get(targetAddress) - instruction_count;
            // System.out.println(offset);
            fileWriter.append("f");
            fileWriter.append(returnRegAddress(destReg));
            fileWriter.append(returnRegAddress(srcReg1));
            if (offset < 0) {
              String negOff = Integer.toHexString(offset);
              String finalOffset = negOff.substring(6, 8);
              fileWriter.append(finalOffset);
            } else {
              if (offset < 16)
                fileWriter.append("0" + Integer.toHexString(offset) + "\n");
              else
                fileWriter.append(Integer.toHexString(offset));
            }
          } 
          else if (operation.equalsIgnoreCase("sll")) {
            int shiftAmount = Integer.parseInt(stringTokenizer.nextToken());
            fileWriter.append("0");
            fileWriter.append(returnRegAddress(srcReg1) + "0");
            fileWriter.append(returnRegAddress(destReg));
            fileWriter.append(Integer.toHexString(shiftAmount) + "\n");
          } 
          else if (operation.equalsIgnoreCase("srl")) {
            int shiftAmount = Integer.parseInt(stringTokenizer.nextToken());
            fileWriter.append("6");
            fileWriter.append(returnRegAddress(srcReg1) + "0");
            fileWriter.append(returnRegAddress(destReg));
            fileWriter.append(Integer.toHexString(shiftAmount) + "\n");
          }
        }
      }
    }
    fileWriter.close();
  }
}
