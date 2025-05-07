//
// Created by Nyell Yonatan on 4/30/25.
//
#include <iostream>
#include <fstream>
#include <sstream>
#include <unordered_map>
#include <vector>
#include <bitset>
#include <algorithm>

using namespace std;

unordered_map<string, string> registerMap = {
    {"$t0", "01000"},
    {"$t1", "01001"},
    {"$t2", "01010"},
    {"$t3", "01011"},
    {"$t4", "01100"},
    {"$t5", "01101"},
    {"$t9", "01111"},
    {"$ra", "11111"},
    {"$s0", "10000"},
    {"$s1", "10001"},
    {"$s2", "10010"},
    {"$zero", "00000"},
    {"$fizz", "11000"},
    {"$buzz", "11001"},
    {"$fizzbuzz", "11010"}
};

unordered_map<string, string> opcodeMap = {
    {"fr", "110000"},
    {"frj", "000010"},
    {"modulo", "000000"},
    {"prnt", "110001"},
    {"bgt", "110010"},
    {"blt", "110011"},
    {"bge", "110100"},
    {"ble", "110101"},
    {"pow", "000000"},
    {"rand", "000000"},
    {"addn", "001000"},
    {"addr", "000000"},
    {"subn", "001001"},
    {"subr", "000000"},
    {"swap", "000000"},
    {"ld", "101101"},
    {"multi", "000000"},
    {"divi", "000000"},
    {"inc", "001000"},
    {"dec", "001001"}
};

unordered_map<string, string> functMap = {
    {"modulo", "101101"},
    {"pow", "101110"},
    {"addr", "100000"},
    {"subr", "100010"},
    {"multi", "011000"},
    {"divi", "011010"},
    {"swap", "111000"}
};

string immToBin(const string& immStr) {
    int imm = stoi(immStr);
    bitset<16> bin(imm);
    return bin.to_string();
}

string assembleLine(const string& line) {
    istringstream iss(line);
    string instr, a1, a2, a3;
    iss >> instr >> a1 >> a2 >> a3;
    a1.erase(remove(a1.begin(), a1.end(), ','), a1.end());
    a2.erase(remove(a2.begin(), a2.end(), ','), a2.end());
    a3.erase(remove(a3.begin(), a3.end(), ','), a3.end());

    string bin;
    if (instr == "fr") {
        bin = opcodeMap[instr] + registerMap[a1] + registerMap[a2] + immToBin(a3);
    }
    else if (instr == "frj") {
        bin = opcodeMap[instr] + string(25,'0') + "1";
    }
    else if (instr == "modulo" || instr == "pow" || instr == "addr" ||
               instr == "subr" || instr == "multi" || instr == "divi" || instr == "swap") {
        bin = opcodeMap[instr] + registerMap[a1] + registerMap[a2] + registerMap["$t1"] + "00000" + functMap[instr];
    }
    else if (instr == "addn" || instr == "subn") {
        bin = opcodeMap[instr] + registerMap[a1] + registerMap["$t1"] + immToBin(a2);
    }
    else if (instr == "inc" || instr == "dec") {
        bin = opcodeMap[instr] + registerMap[a1] + registerMap["$t1"] + immToBin("1");
    }
    else if (instr == "prnt") {
        bin = opcodeMap[instr] + registerMap[a1] + "00000" + immToBin(a2);
    }
    else if (instr == "bgt" || instr == "blt" || instr == "bge" || instr == "ble") {
        bin = opcodeMap[instr] + registerMap[a1] + registerMap[a2] + immToBin("2");
    }
    else if (instr == "ld") {
        bin = opcodeMap[instr] + registerMap["$t0"] + registerMap["$t1"] + immToBin("4");
    }
    else if (instr == "rand") {
        bin = opcodeMap[instr] + "0000000000000000100100000111111";
    }
    else {
        return "";
    }
    return bin;
}

int main() {
    ifstream infile("program1.asm");
    ofstream outfile("program1.bin");
    string line;
    while (getline(infile, line)) {
        if (line.empty() || line.back() == ':' || line[0] == '#') continue;
        string bin = assembleLine(line);
        if (!bin.empty() && bin.length() == 32) outfile << bin << endl;
    }
    return 0;
}
