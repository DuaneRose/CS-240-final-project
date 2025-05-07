import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashMap;

public class Compiler extends IOException {
    private static BufferedWriter bw;
    private static BufferedReader br;
    private static String line;
    private static HashMap<String, Integer> variables = new HashMap<String, Integer>();
    private static int address = 5000;
    private static Scanner scnr;
    private static int afterCount= 0;
    private static int elseCount = 0;
    private static int loopCount = 0;
    private static String buf = "";
    private static String branch = "";
    private static String body;
    private static int bodyCount = 0;
    private static String data = "\n.data\nnewLine: .asciiz \"\\n\"\n";
    private static int printNum = 0;

    private static void print(){
        line = line.substring(1+ line.indexOf("\""), line.lastIndexOf(")"));

        if(line.contains("%d")){
            String num = line.substring(1 + line.indexOf(","), line.length());
            num = num.replace("\s","");
            write("addi $t0, $zero, " + variables.get(num)+ "\n");
            write("lw $t1, 0($t0)\n");
            write("prnt $t1, 1\n");

            if(line.contains("\\n")){
                write("la $t0, newLine\n");
                write("prnt $t0, 4\n");
            }
        } else{
            line = line.substring(0, line.lastIndexOf("\""));
            write("la $t0, print" + printNum + "\n");
            write("prnt $t0, 4\n");

            data = data.concat("print" + printNum + ": .asciiz \"" + line + "\"\n");
        }
        printNum++;
    }

    private static void loops() throws IOException {
        int bodyTracker = bodyCount;
        bodyCount++;

        int currentLoop = loopCount;
        int currentAfter = afterCount;
        afterCount++;
        loopCount++;

        if(line.contains("for")){
            write("# creating for loop header\n");
            line = line.substring(1 + line.indexOf("("), line.indexOf(")"));
            translate();
            String increment = "";

            line = line.substring(1 + line.indexOf(";"), line.length());
            if(line.contains("<=")){
                write("while" + currentLoop + ":\n");
                expression(line.substring(0, line.indexOf("<")));
                write("swap $t4, $t1\n\n");
                expression(line.substring(2 + line.indexOf("<"), 1 + line.indexOf(";") + line.substring(1 + line.indexOf(";")).indexOf(";")));
                write("swap $t5, $t1\n\n");

                write("sub $t1, $t5, $t4\nbltz $t1, after" + currentAfter + "\n");
            }else if(line.contains(">=")){
                write("while" + currentLoop + ":\n");
                expression(line.substring(0, line.indexOf(">")));
                write("swap $t4, $t1\n\n");
                expression(line.substring(2 + line.indexOf(">"), 1 + line.indexOf(";") + line.substring(1 + line.indexOf(";")).indexOf(";")));
                write("swap $t5, $t1\n\n");

                write("sub $t1, $t5, $t4\nbgtz $t1, after" + currentAfter + "\n");
            }else if(line.contains("<")){
                write("while" + currentLoop + ":\n");
                expression(line.substring(0, line.indexOf("<")));
                write("swap $t4, $t1\n\n");
                expression(line.substring(1 + line.indexOf("<"), 1 + line.indexOf(";") + line.substring(1 + line.indexOf(";")).indexOf(";")));
                write("swap $t5, $t1\n\n");

                write("sub $t1, $t5, $t4\nblez $t1, after" + currentAfter + "\n");
            }else if(line.contains(">")){
                write("while" + currentLoop + ":\n");
                expression(line.substring(0, line.indexOf(">")));
                write("swap $t4, $t1\n\n");
                expression(line.substring(2 + line.indexOf(">"), 1 + line.indexOf(";") + line.substring(1 + line.indexOf(";")).indexOf(";")));
                write("swap $t5, $t1\n\n");

                write("sub $t1, $t5, $t4\nbgez $t1, after" + currentAfter + "\n");
            }
            write("# for loop header has been set\n\n");

            //incrementing part of for loop
            line = line.substring(1 + line.indexOf(";"), line.length());
            line = line.replace("\s","");
            if (line.contains("++")){
                line = line.substring(0, line.indexOf("+"));
                increment = increment.concat("# setting increment\n");
                increment = increment.concat("addi $t0, $zero, " + variables.get(line) + "\n");
                increment = increment.concat("lw $t1, 0($t0)\n");
                increment = increment.concat("addi $t1, $t1, 1\n");
                increment = increment.concat("sw $t1, 0($t0)\n");
                increment = increment.concat("# increment set\n\n");
            }else if(line.contains("--")){
                line = line.substring(0, line.indexOf("-"));
                increment = increment.concat("# setting decrement\n");
                increment = increment.concat("addi $t0, $zero, " + variables.get(line) + "\n");
                increment = increment.concat("lw $t1, 0($t0)\n");
                increment = increment.concat("addi $t2, $zero, 1\n");
                increment = increment.concat("sub $t1, $t1, $t2\n");
                increment = increment.concat("sw $t1, 0($t0)\n");
                increment = increment.concat("# decrement set\n\n");
            }

            //loop body
            while(!line.contains("}") || (bodyTracker + 1 == bodyCount)){
                line = br.readLine();
                if(line.length() == 0) continue;
                if(line.contains("}") && (bodyTracker + 1 == bodyCount) && !line.contains("else")){
                    bodyCount--;
                    continue;
                }
                translate();
            }

            buf = buf.concat(increment);

            write("j while" + currentLoop + "\n# for loop body done\n\nafter"  + currentAfter + ":\n");


        }else if(line.contains("while")){
            write("# creating while loop header\n");
            if(line.contains("==")){
                write("while" + currentLoop + ":\n");
                expression(line.substring(1 + line.indexOf("("), line.indexOf("=")));
                write("swap $t4, $t1\n\n");
                expression(line.substring(2 + line.indexOf("="), line.indexOf(")")));
                write("swap $t5, $t1\n\n");
                write( "\nbne $t4, $t5, after" + currentAfter + "\n");
                branch = "bne";
            }else if(line.contains("!=")){
                write("while" + currentLoop + ":\n");
                expression(line.substring(1 + line.indexOf("("), line.indexOf("!")));
                write("swap $t4, $t1\n\n");
                expression(line.substring(2 + line.indexOf("!"), line.indexOf(")")));
                write("swap $t5, $t1\n\n");
                write("\nbeq $t4, $t5, after" + currentAfter + "\n");
                branch = "beq";
            }
            write("# while loop header created\n\n# Creating While loop body\n");

            while(!line.contains("}") || (bodyTracker + 1 == bodyCount)){
                line = br.readLine();
                if(line.length() == 0) continue;
                if(line.contains("}") && (bodyTracker + 1 == bodyCount) && !line.contains("else")){
                    bodyCount--;
                    continue;
                }
                translate();
            }
            write("j while" + currentLoop + "\n# while loop body done\n\nafter"  + currentAfter + ":\n");
        }
    }

    private static void ifElse() throws IOException {
        int bodyTracker = bodyCount;
        bodyCount++;
        int currentElse = elseCount;
        int currentAfter = afterCount;
        elseCount++;

        write(branch + " $t4, $t5, else"  + currentElse + "\n");
        write("# if statement set\n\n");
        bw.write(buf);
        buf = "";
        bw.write(body);
        bw.flush();
        body = "";
        write("j after" + (currentAfter - 1) + "\n\n" + "else" + currentElse+ ":\n");

        write("# making the else-if statement and body\n");
        String branch = "";

        line = line.substring(line.indexOf("("),line.indexOf(")")+1);
        branch = settingIf(branch);

        //we have the body of the else-if statemnet placed in a var called body
        do{
            line = br.readLine();
            if(line.length() == 0) continue;
            if(line.contains("}") && (bodyTracker + 1 == bodyCount)){
                bodyCount--;
                continue;
            }
            translate();
        }while(!line.contains("}") || (bodyTracker + 1 == bodyCount));
        body = buf;
        buf = "";

        br.mark(100);

        if(!line.contains("else") && (line = br.readLine()) != null){
            while (line.length() == 0 && line != null){
                line = scnr.nextLine();
            }
        }

        if(line.contains("if") && line.contains("else")) ifElse();
        else if (!line.contains("else")){
            write(branch + " $t4, $t5, after"  + (currentAfter - 1) + "\n");
            write("# if statement set\n\n");
        }
    }

    private static String settingIf(String branch) throws IOException {
        if(line.contains("&&")){
            if(line.substring(0,line.indexOf("&")).contains("==")){
                expression(line.substring(1 + line.indexOf("("), line.indexOf("=")));
                write("swap $t4, $t1\n\n");
                expression(line.substring(2 + line.indexOf("="), line.indexOf("&")));
                write("swap $t5, $t1\n\n");
                branch = "bne";
            }else if (line.substring(0,line.indexOf("&")).contains("!=")){
                expression(line.substring(1 + line.indexOf("("), line.indexOf("!")));
                write("swap $t4, $t1\n\n");
                expression(line.substring(2 + line.indexOf("!"), line.indexOf("&")));
                write("swap $t5, $t1\n\n");
                branch = "beq";
            }
            String jumpTo;


            write(branch + " $t4, $t5, else" + elseCount + "\n");

            line = line.substring(1 + line.indexOf("&"),line.length());
            if(line.substring(line.indexOf("&"),line.length()).contains("==")){
                expression(line.substring(1 + line.indexOf("&"), line.indexOf("=")));
                write("swap $t4, $t1\n\n");
                expression(line.substring(2 + line.indexOf("="), line.indexOf(")")));
                write("swap $t5, $t1\n\n");
                branch = "bne";
            }else if (line.substring(line.indexOf("&"),line.length()).contains("!=")){
                expression(line.substring(1 + line.indexOf("&"), line.indexOf("!")));
                write("swap $t4, $t1\n\n");
                expression(line.substring(2 + line.indexOf("!"), line.indexOf(")")));
                write("swap $t5, $t1\n\n");
                branch = "beq";
            }
        }else{
            if(line.contains("==")){
                expression(line.substring(1 + line.indexOf("("), line.indexOf("=")));
                write("swap $t4, $t1\n\n");
                expression(line.substring(2 + line.indexOf("="), line.indexOf(")")));
                write("swap $t5, $t1\n\n");
                branch = "bne";
            }else if(line.contains("!=")){
                expression(line.substring(1 + line.indexOf("("), line.indexOf("!")));
                write("swap $t4, $t1\n\n");
                expression(line.substring(2 + line.indexOf("!"), line.indexOf(")")));
                write("swap $t5, $t1\n\n");
                branch = "beq";
            }
        }

        bw.write(buf);
        buf = "";
        return branch;
    }

    private static void ifStatements(String comment) throws IOException {
        int bodyTracker = bodyCount;
        bodyCount++;
        int currentElse = elseCount;
        int currentAfter = afterCount;
        afterCount++;
        elseCount++;

        write(comment);

        line = line.substring(line.indexOf("("),line.indexOf(")")+1);
        branch = settingIf(branch);

        //we have the body of the if statemnet placed in a var called body
        while(!line.contains("}") || (bodyTracker + 1 == bodyCount) && !line.contains("else")){
            line = br.readLine();
            if(line.length() == 0) continue;
            if(line.contains("}") && (bodyTracker + 1 == bodyCount)){
                bodyCount--;
                continue;
            }
            translate();
        }
        body = buf;
        buf = "";

        boolean hasElse = false;
        br.mark(100);

        //looks for else-if/else statements
        if(!line.contains("else") && scnr.hasNextLine()){
            do{
                line = scnr.nextLine();
            }while (line.length() == 0);
        }

        if(line.contains("if") && line.contains("else")){
            ifElse();
            hasElse = true;
            br.reset();
        }

        if(line.contains("else") && !line.contains("if")){
            bodyCount++;
            //write("# making an else statement\n");
            write(branch + " $t4, $t5, else"  + currentElse + "\n");
            write("# if statement set\n\n");
            bw.write(buf);
            buf = "";
            bw.write(body);
            body = "";
            write("j after" + currentAfter + "\n\nelse" + currentElse +":\n");

            do{
                line = br.readLine();
                if(line.length() == 0) continue;
                if(line.contains("}") && (bodyTracker + 1 == bodyCount) && !line.contains("else")){
                    bodyCount--;
                    continue;
                }
                translate();
            }while(!line.contains("}") || (bodyTracker + 1 == bodyCount));

            br.mark(100);
            hasElse = true;
        }

        if(!hasElse) write(branch + " $t4, $t5, after"  + currentAfter + "\n# if statement set\n\n");

        bw.write(buf);
        bw.write(body);
        buf = "";
        body = "";

        write("# end of the if body\n");
        write("after" + (currentAfter) + ":\n");
        bw.flush();
        br.reset();
    }

    private static void declaring(String varName) throws IOException {
        //add new variable to hashMap
        if(varName.contains(";")) varName = varName.substring(0,varName.length()-1);
        System.out.println(varName);
        variables.put(varName, address++);

        //write to Assembly
        write("#" + varName + " has memory location\n");
        write("addi $t0, $zero, " + variables.get(varName) + "\n\n");

        //check for any assignments
        if(scnr.hasNext() && line.contains("=")) assignment(varName);
    }

    private static void expression(String expression) throws IOException {
        Scanner expr = new Scanner(expression);
        String element = expr.next();
        if(element.contains(";")) element = element.substring(0, element.length() - 1);

        //handle random numbers
        if(line.contains("rand()")){//handles rand
            element = expr.next();
            element = expr.next();
            if(element.contains(";")) element = element.substring(0,element.length()-1);

            if(variables.containsKey(element)){
                write("addi $t3, $zero, " + variables.get(element) + "\n");
                write("lw $t2, 0($t3)\n");
                write("rand\n\n");
            }else{
                write("addi $t2, $zero, " + element + "\n");
                write("rand\n\n");
            }
            return;
        }

        if(variables.containsKey(element)){
            write("addi $t2, $zero, " + variables.get(element) + "\n");
            write("lw $t1, 0($t2)\n");
        }else{
            write("addi $t1, $zero, " + element + "\n\n");
        }

        while(expr.hasNext()){
            element = expr.next();
            if(element.equals(";")) break;

            //addition(var/immediate)
            if(element.equals("+")){//handles addtion
                element = expr.next();
                if(element.contains(";")) element = element.substring(0,element.length()-1);

                if(variables.containsKey(element)){
                    write("addi $t3, $zero, " + variables.get(element) + "\n");
                    write("lw $t2, 0($t3)\n");
                    write("add $t1, $t1, $t2\n\n");
                }else{
                    write("addi $t1, $t1, " + element + "\n\n");
                }
            }else if(element.equals("-")){//handles subtraction
                element = expr.next();
                if(element.contains(";")) element = element.substring(0,element.length()-1);

                if(variables.containsKey(element)){
                    write("addi $t3, $zero, " + variables.get(element) + "\n");
                    write("lw $t2, 0($t3)\n");
                    write("sub $t1, $t1, $t2\n\n");
                }else{
                    write("addi $t2, $zero, " + element + "\n");
                    write("sub $t1, $t1, $t2\n\n");
                }
            }else if(line.contains("*")){//handles multiplication
                element = expr.next();
                if(element.contains(";")) element = element.substring(0,element.length()-1);

                if(variables.containsKey(element)){
                    write("addi $t3, $zero, " + variables.get(element) + "\n");
                    write("lw $t2, 0($t3)\n");
                    write("multi $t1, $t2\n\n");
                }else{
                    write("addi $t2, $zero, " + element + "\n");
                    write("multi $t1, $t2\n\n");
                }
            }else if(line.contains("%")){//handles modulo
                element = expr.next();
                if(element.contains(";")) element = element.substring(0,element.length()-1);

                if(variables.containsKey(element)){
                    write("addi $t3, $zero, " + variables.get(element) + "\n");
                    write("lw $t2, 0($t3)\n");
                    write("mdl $t1, $t2\n\n");
                }else{
                    write("addi $t2, $zero, " + element + "\n");
                    write("mdl $t1, $t2\n\n");
                }
            }

            bw.flush();
        }
    }

    private static void assignment(String varName) throws IOException {
        String element = scnr.next();
        write("#preparing $t0 to hold the final value of " + varName + "\n");
        write("addi $t0, $zero, " + variables.get(varName) + "\n");
        element = scnr.next();

        expression(line.substring(1 + line.indexOf("=")));

        write("sw $t1, 0($t0)\n");
        write("#" +varName + " has been set\n\n");
    }

    private static void translate() throws IOException {
        scnr = new Scanner(line);

        String thing = scnr.next();
        if(thing.equals(";") || thing.contains("}")) return;
        else if(line.contains("rand")) assignment(thing);
        else if (line.contains("()")) write(line.substring(2 + line.indexOf("t"), line.indexOf("(")) + ":\n");
        else if (line.contains("printf")) print();
        else if(thing.equals("int")) declaring(scnr.next());
        else if(line.contains("if")) ifStatements("# this is making the if statement\n");
        else if(line.contains("for") || line.contains("while")) loops();
        else if(line.contains("=")) assignment(thing);
        bw.flush();
    }
    
    private static void write(String statement){
        buf = buf.concat(statement);
    }

    private static boolean openFile(String fileName) throws FileNotFoundException {
        //tries to open C file
        try{
            br = new BufferedReader(new FileReader(fileName));
            System.out.println("C file opened");
        }catch (IOException e){
            System.out.println("Couldn't open C file");
            return false;
        }

        //try to open an assembly file
        try{
            File file = new File("Assembly.txt");
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            bw = new BufferedWriter(fileWriter);
            System.out.println("Assembly file opened");
        }catch (IOException e){
            System.out.println("couldn't open C file");
            return false;
        }

        return true;
    }

    private static void closeFile() throws IOException {
        bw.close();
        br.close();
    }

    public static void main(String[] args) throws IOException {
        openFile("/Users/duanegennaro/IdeaProjects/Compiler/C.txt");
        while((line = br.readLine()) != null){
            if(line.length() == 0) continue;
            translate();
            bw.write(buf);
            buf = "";
        }
        bw.write(data);
        bw.flush();
        Simulator sim = new Simulator("/Users/duanegennaro/IdeaProjects/Compiler/Assembly.txt");
        closeFile();
    }
}