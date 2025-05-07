#include <iostream>
#include <fstream>
#include <unordered_map>
#include <vector>
#include <string>
#include <bitset>

using namespace std;

unordered_map<string, string> op_codes = {
    {"110000", "fr"},
    {"000010", "frj"},
    {"000000", "modulo"},
    {"110001", "prnt"},
    {"110010", "bgt"},
    {"110011", "blt"},
    {"110100", "bge"},
    {"110101", "ble"},
    {"000000", "pow"},
    {"000000", "rand"},
    {"001000", "addn"},
    {"000000", "addr"},
    {"001001", "subn"},
    {"000000", "subr"},
    {"000000", "swap"},
    {"101101", "ld"},
    {"000000", "multi"},
    {"000000", "divi"},
    {"001000", "inc"},
    {"001001", "dec"}
};

unordered_map<string, string> func_codes = {
    {"101101", "modulo"},
    {"101110", "pow"},
    {"100000", "addr"},
    {"100010", "subr"},
    {"011000", "multi"},
    {"011010", "divi"},
    {"111000", "swap"}
};

unordered_map<string, string> registers = {
    {"01000", "$t0"},
    {"01001", "$t1"},
    {"01010", "$t2"},
    {"01011", "$t3"},
    {"01100", "$t4"},
    {"01101", "$t5"},
    {"01111", "$t9"},
    {"11111", "$ra"},
    {"10000", "$s0"},
    {"10001", "$s1"},
    {"10001", "$s2"},
    {"00000", "$zero"},
    {"11000", "$fizz"},
    {"11001", "$buzz"},
    {"11010", "$fizzbuzz"}
};

vector<string> bin_to_custom(const string& line) {
    vector<string> mips;
    for (size_t i = 0; i + 32 <= line.size(); i += 32) {
        string bin = line.substr(i, 32);
        string op_code = bin.substr(0, 6);

        if (op_code == "000000") {
            string rs = bin.substr(6, 5);
            string rt = bin.substr(11, 5);
            string rd = bin.substr(16, 5);
            string func = bin.substr(26, 6);
            string inst = func_codes.count(func) ? func_codes[func] : "unknown";
            if (inst == "rand") {
                mips.push_back("rand");
            } else {
                mips.push_back(inst + " " + registers[rs] + ", " + registers[rt]);
            }
        } else if (op_code == "001000" || op_code == "001001") {
            string rs = bin.substr(6, 5);
            string rt = bin.substr(11, 5); // should be $t1
            string imm = bin.substr(16, 16);
            int val = stoi(imm, nullptr, 2);
            string inst = op_codes[op_code];
            if (inst == "addn" || inst == "subn")
                mips.push_back(inst + " " + registers[rs] + ", " + to_string(val));
            else if (inst == "inc")
                mips.push_back("inc " + registers[rs]);
            else if (inst == "dec")
                mips.push_back("dec " + registers[rs]);
        } else if (op_code == "110000") {
            string rs = bin.substr(6, 5);
            string rt = bin.substr(11, 5);
            string imm = bin.substr(16, 16);
            int val = stoi(imm, nullptr, 2);
            mips.push_back("fr " + registers[rs] + ", " + registers[rt] + ", " + to_string(val));
        } else if (op_code == "000010") {
            mips.push_back("frj $ra");
        } else if (op_code == "110001") {
            string rs = bin.substr(6, 5);
            string type = bin.substr(16, 16);
            int val = stoi(type, nullptr, 2);
            mips.push_back("prnt " + registers[rs] + ", " + to_string(val));
        } else if (op_code == "110010" || op_code == "110011" || op_code == "110100" || op_code == "110101") {
            string rs = bin.substr(6, 5);
            string rt = bin.substr(11, 5);
            string offset = bin.substr(16, 16);
            int val = stoi(offset, nullptr, 2);
            string inst = op_codes[op_code];
            mips.push_back(inst + " " + registers[rs] + ", " + registers[rt] + ", " + to_string(val));
        } else if (op_code == "101101") {
            mips.push_back("ld");
        } else {
            mips.push_back("unknown_instruction");
        }
    }
    return mips;
}

void handle_lines(const string& bin_file) {
    ifstream input_file(bin_file);
    ofstream output_file("BACK_TO_ASM.txt");
    string line;
    while (getline(input_file, line)) {
        vector<string> instructions = bin_to_custom(line);
        for (const string& instr : instructions) {
            output_file << instr << "\n";
        }
    }
    input_file.close();
    output_file.close();
}

int main(int argc, char* argv[]) {
    if (argc < 2) return 1;
    string file = argv[1];
    handle_lines(file);
    return 0;
}

