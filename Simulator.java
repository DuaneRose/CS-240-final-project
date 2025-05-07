import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class Simulator {
    private static final ArrayList<String> assembly = new ArrayList<>();
    private static final HashMap<String, String> registers = new HashMap<>();
    private static final HashMap<String, String> strData = new HashMap<>();
    private static final HashMap<String, Integer> numData = new HashMap<>();
    private static BufferedReader br;
    private static String line = "";
    private static int i;

    private static void setRegisters(){
        registers.put("$zero", "0");
        registers.put("$v0", "");
        registers.put("$v1", "");
        registers.put("$a0", "");
        registers.put("$a1", "");
        registers.put("$a2", "");
        registers.put("$a3", "");
        registers.put("$t0", "");
        registers.put("$t1", "");
        registers.put("$t2", "");
        registers.put("$t3", "");
        registers.put("$t4", "");
        registers.put("$t5", "");
        registers.put("$t6", "");
        registers.put("$t7", "");
        registers.put("$t8", "");
        registers.put("$t9", "");
        registers.put("$s0", "");
        registers.put("$s1", "");
        registers.put("$s2", "");
        registers.put("$s3", "");
        registers.put("$s4", "");
        registers.put("$s5", "");
        registers.put("$s6", "");
        registers.put("$s7", "");
        registers.put("$k0", "");
        registers.put("$k1", "");
        registers.put("$gp", "");
        registers.put("$sp", "");
        registers.put("$fp", "");
        registers.put("$ra", "");
    }

    public Simulator(String fileName) throws IOException{
        System.out.println("Getting Data reader:\n");
        try{
            br = new BufferedReader(new FileReader(fileName));
            System.out.println("Assembly file opened");
        }catch (IOException e){
            System.out.println("Couldn't open C file");
        }

        String line = "";
        while((line = br.readLine()) != null && !line.equals(".data")){
            if(line.length() == 0 || line.contains("#")) continue;
            assembly.add(line);
        }
        while((line = br.readLine()) != null){
            strData.put(line.substring(0,line.indexOf(":")), line.substring(1 + line.indexOf("\""),line.length() -1));
        }

        setRegisters();

        System.out.println("data uploaded:\n\nRunning Simulation:\n___________________\n\n");
        run();
    }

    private static void addi(String r1, String r2, String num){
        int value1 = Integer.parseInt(registers.get(r2));
        int value2 = Integer.parseInt(num);

        registers.replace(r1, String.valueOf(value1 + value2));
    }

    private static void addn(String r1, String num){
        int value1 = Integer.parseInt(registers.get(r1));
        int value2 = Integer.parseInt(num);

        registers.replace("$t1", String.valueOf(value1 + value2));
    }

    private static void inc(String r1){
        int value1 = Integer.parseInt(registers.get(r1));

        registers.replace("$t1", String.valueOf(value1 + 1));
    }

    private static void dec(String r1){
        int value1 = Integer.parseInt(registers.get(r1));

        registers.replace("$t1", String.valueOf(value1 - 1));
    }

    private static void add(String r1, String r2, String r3){
        int value1 = Integer.parseInt(registers.get(r2));
        int value2 = Integer.parseInt(registers.get(r3));

        registers.replace(r1, String.valueOf(value1 + value2));
    }

    private static void addr(String r1, String r2){
        int value1 = Integer.parseInt(registers.get(r1));
        int value2 = Integer.parseInt(registers.get(r2));

        registers.replace("$t1", String.valueOf(value1 + value2));
    }

    private static void sub(String r1, String r2, String r3){
        int value1 = Integer.parseInt(registers.get(r2));
        int value2 = Integer.parseInt(registers.get(r3));

        registers.replace(r1, String.valueOf(value1 - value2));
    }

    private static void subn(String r1, String num){
        int value1 = Integer.parseInt(registers.get(r1));
        int value2 = Integer.parseInt(num);

        registers.replace("$t1", String.valueOf(value1 - value2));
    }

    private static void subr(String r1, String r2){
        int value1 = Integer.parseInt(registers.get(r1));
        int value2 = Integer.parseInt(registers.get(r2));

        registers.replace("$t1", String.valueOf(value1 - value2));
    }

    private static void multi(String r1, String r2){
        int value1 = Integer.parseInt(registers.get(r1));
        int value2 = Integer.parseInt(registers.get(r2));

        registers.replace("$t1", String.valueOf(value1 * value2));
    }

    private static void divi(String r1, String r2){
        int value1 = Integer.parseInt(registers.get(r1));
        int value2 = Integer.parseInt(registers.get(r2));

        registers.replace("$t1", String.valueOf(value1 / value2));
    }

    private static void mdl(String r1, String r2){
        int value1 = Integer.parseInt(registers.get(r1));
        int value2 = Integer.parseInt(registers.get(r2));

        registers.replace("$t1", String.valueOf(value1 % value2));
    }

    private static void lw(String r1, String r2){
        registers.replace(r1, String.valueOf(numData.get(registers.get(r2))));
    }

    private static void ld(){
        registers.replace("$t1", String.valueOf(numData.get(registers.get("$t0"))));
    }

    private static void la(String r1, String address){
        registers.replace(r1, address);
    }

    private static void sw(String r1, String r2){
        if(numData.containsKey(registers.get(r2))){
            numData.replace(registers.get(r2),Integer.valueOf(registers.get(r1)));
        } else {
            numData.put(registers.get(r2),Integer.valueOf(registers.get(r1)));
        }
    }

    private static void swap(String r1, String r2){
        String temp = registers.get(r1);

        registers.replace(r1, registers.get(r2));
        registers.replace(r2, temp);
    }

    private static void beq(String r1, String r2, String jump){
        if(Integer.parseInt(registers.get(r1)) == Integer.parseInt(registers.get(r2))){
            i = assembly.indexOf(jump + ":");
        }
    }

    private static void bne(String r1, String r2, String jump){
        if(Integer.parseInt(registers.get(r1)) != Integer.parseInt(registers.get(r2))){
            i = assembly.indexOf(jump + ":");
        }
    }

    private static void blez(String r1, String jump){
        if(Integer.parseInt(registers.get(r1)) <= 0){
            i = assembly.indexOf(jump + ":");
        }
    }

    private static void bltz(String r1, String jump){
        if(Integer.parseInt(registers.get(r1)) < 0){
            i = assembly.indexOf(jump + ":");
        }
    }

    private static void bgez(String r1, String jump){
        if(Integer.parseInt(registers.get(r1)) >= 0){
            i = assembly.indexOf(jump + ":");
        }
    }

    private static void bgtz(String r1, String jump){
        if(Integer.parseInt(registers.get(r1)) > 0){
            i = assembly.indexOf(jump + ":");
        }
    }

    private static void bgt(String r1, String r2, String jump){
        if(Integer.parseInt(registers.get(r1)) > Integer.parseInt(registers.get(r2))){
            i = assembly.indexOf(jump + ":");
        }
    }

    private static void blt(String r1, String r2, String jump){
        if(Integer.parseInt(registers.get(r1)) < Integer.parseInt(registers.get(r2))){
            i = assembly.indexOf(jump + ":");
        }
    }

    private static void bge(String r1, String r2, String jump){
        if(Integer.parseInt(registers.get(r1)) >= Integer.parseInt(registers.get(r2))){
            i = assembly.indexOf(jump + ":");
        }
    }

    private static void ble(String r1, String r2, String jump){
        if(Integer.parseInt(registers.get(r1)) <= Integer.parseInt(registers.get(r2))){
            i = assembly.indexOf(jump + ":");
        }
    }

    private static void pow(String r1, String r2){
        int value1 = Integer.parseInt(registers.get(r1));
        int value2 = Integer.parseInt(registers.get(r2));

        registers.replace("$t1", String.valueOf(Math.pow(value1,value2)));
    }

    private static void rand(){
        Random rand = new Random();

        registers.replace("$t1", String.valueOf(rand.nextInt(Integer.parseInt(registers.get("$t2")) + 1)));
    }

    private static void fr(String r1, String r2, String increment){
        registers.replace("$s0", registers.get(r1));
        registers.replace("$s1", registers.get(r2));
        registers.replace("$s2", String.valueOf(increment));
        registers.replace("$ra", String.valueOf(i));
    }

    private static void frj(){
        if(Integer.parseInt(registers.get("$s0")) == Integer.parseInt(registers.get("$s1"))){
            i = Integer.parseInt(registers.get("$ra"));
            int value = Integer.valueOf(registers.get("$s0")) + Integer.valueOf(registers.get("$s2"));
            registers.replace("$s0", String.valueOf(value));
        }
    }

    private static void jump(String jump){
        i = assembly.indexOf(jump + ":");
    }

    private static void prnt(String r1, int num){
        if(num == 1){
            pause(2);
            System.out.print(registers.get(r1));
            pause(2);
        }else if(num == 4){
            String holder = strData.get(registers.get(r1));
            if(holder.contains("\\n")) System.out.println(holder.substring(0,holder.length()-2));
            else System.out.print(holder);
            loading();
            pause(2);
        }
    }

    private static void readLine(){
        Scanner scanner = new Scanner(line);
        String fragments = scanner.next();
        switch (fragments) {
            case "addi" -> addi(scanner.next().replace(",", ""), scanner.next().replace(",", ""), scanner.next());
            case "add" -> add(scanner.next().replace(",", ""), scanner.next().replace(",", ""), scanner.next());
            case "sub" -> sub(scanner.next().replace(",", ""), scanner.next().replace(",", ""), scanner.next());
            case "multi" -> multi(scanner.next().replace(",", ""), scanner.next());
            case "mdl" -> mdl(scanner.next().replace(",", ""), scanner.next());
            case "lw" -> lw(scanner.next().replace(",", ""), line.substring(1 + line.indexOf("("), line.length() - 1));
            case "sw" -> sw(scanner.next().replace(",", ""), line.substring(1 + line.indexOf("("), line.length() - 1));
            case "la" -> la(scanner.next().replace(",", ""), line.substring(2 + line.indexOf(","), line.length()));
            case "swap" -> swap(scanner.next().replace(",", ""), scanner.next());
            case "beq" -> beq(scanner.next().replace(",", ""), scanner.next().replace(",", ""), scanner.next());
            case "bne" -> bne(scanner.next().replace(",", ""), scanner.next().replace(",", ""), scanner.next());
            case "blez" -> blez(scanner.next().replace(",", ""), line.substring(2 + line.indexOf(","), line.length()));
            case "bltz" -> bltz(scanner.next().replace(",", ""), line.substring(2 + line.indexOf(","), line.length()));
            case "bgez" -> bgez(scanner.next().replace(",", ""), line.substring(2 + line.indexOf(","), line.length()));
            case "bgtz" -> bgtz(scanner.next().replace(",", ""), line.substring(2 + line.indexOf(","), line.length()));
            case "j" -> jump(line.substring(2, line.length()));
            case "prnt" -> prnt(scanner.next().replace(",", ""), scanner.nextInt());
            case "addn" -> addn(scanner.next().replace(",", ""), scanner.next());
            case "addr" -> addr(scanner.next().replace(",", ""), scanner.next());
            case "subn" -> subn(scanner.next().replace(",", ""), scanner.next());
            case "subr" -> subr(scanner.next().replace(",", ""), scanner.next());
            case "ld" -> ld();
            case "divi" -> divi(scanner.next().replace(",", ""), scanner.next());
            case "inc" -> inc(scanner.next());
            case "dec" -> dec(scanner.next());
            case "bgt" -> bgt(scanner.next().replace(",", ""), scanner.next().replace(",", ""), scanner.next());
            case "bgl" -> blt(scanner.next().replace(",", ""), scanner.next().replace(",", ""), scanner.next());
            case "bge" -> bge(scanner.next().replace(",", ""), scanner.next().replace(",", ""), scanner.next());
            case "ble" -> ble(scanner.next().replace(",", ""), scanner.next().replace(",", ""), scanner.next());
            case "rand" -> rand();
            case "pow" -> pow(scanner.next().replace(",", ""), scanner.next());
            case "frj" -> frj();
            case "fr" -> fr(scanner.next().replace(",", ""), scanner.next().replace(",", ""), scanner.next());
        }
    }

    private static void run(){
        for(i = 0; i < assembly.size(); i++){
            line = assembly.get(i);
            readLine();
        }

        System.out.print("done");
    }

    private static void loading(){
        char[] ldng = {'L', 'o','a','d','i','n','g'};
        for(int i = 0; i < ldng.length; i++){
            pause(2);
            System.out.print(ldng[i]);
        }
        for(int i = 0; i < ldng.length; i++){
            pause(2);
            System.out.print("\b");
            System.out.print("");
        }
    }

    private static void pause(int x){
        try {
            Thread.sleep(x * 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


