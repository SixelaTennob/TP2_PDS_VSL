package TP2;

import java.util.List;
import java.util.ArrayList;

// This file contains a simple LLVM IR representation
// and methods to generate its string representation

public class Llvm {
    static public class IR {
        List<Instruction> header; // IR instructions to be placed before the code (global definitions)
        List<Instruction> code;   // main code

        public IR(List<Instruction> header, List<Instruction> code) {
            this.header = header;
            this.code = code;
        }

        // append an other IR
        public IR append(IR other) {
            header.addAll(other.header);
            code.addAll(other.code);
            return this;
        }

        // append a code instruction
        public IR appendCode(Instruction inst) {
            code.add(inst);
            return this;
        }

        // append a code header
        public IR appendHeader(Instruction inst) {
            header.add(inst);
            return this;
        }

        // Final string generation
        public String toString() {
            // This header describe to LLVM the target
            // and declare the external function printf
            StringBuilder r = new StringBuilder("; Target\n" +
                    "target triple = \"x86_64-unknown-linux-gnu\"\n" +
                    "; External declaration of the printf function\n" +
                    "declare i32 @printf(i8* noalias nocapture, ...)\n" +
                    "\n; Actual code begins\n\n");

            for(Instruction inst: header)
                r.append(inst);

            r.append("\n\n");

            // We create the function main
            // TODO : remove this when you extend the language
            r.append("define i32 @main() {\n");


            for(Instruction inst: code)
                r.append(inst);

            // TODO : remove this when you extend the language
            r.append("}\n");

            return r.toString();
        }
    }

    // Returns a new empty list of instruction, handy
    static public List<Instruction> empty() {
        return new ArrayList<Instruction>();
    }


    // LLVM Types
    static public abstract class Type {
        public abstract String toString();
    }

    static public class Int extends Type {
        public String toString() {
            return "i32";
        }
    }

    // TODO : other types


    // LLVM IR Instructions
    static public abstract class Instruction {
        public abstract String toString();
    }

    static public class accolGauche extends Instruction {
        public String toString() { return "{"; }
    }
    static public class accolDroite extends Instruction {
        public String toString() { return "}"; }
    }

    static public class ConditionTrue extends Instruction{
        String tmp;
        String tmp2;
        Type tp;

        public ConditionTrue(String tmp, Type tp, String tmp2){
            this.tmp = tmp;
            this.tmp2 = tmp2;
            this.tp = tp;
        }

        public String toString() {
            return tmp + " = icmp eq " + tp + " " + tmp2 + ", 0\n";
        }
    }

    static public class ConditionFalse extends Instruction{
        String tmp;
        String tmp2;
        Type tp;

        public ConditionFalse(String tmp, Type tp, String tmp2){
            this.tmp = tmp;
            this.tmp2 = tmp2;
            this.tp = tp;
        }

        public String toString() {
            return tmp + " = icmp ne " + tp + " " + tmp2 + ", 0\n";
        }
    }

    static public class Label extends Instruction {
        String label;

        public Label(String label){
            this.label=label;
        }


        public String toString() {
            return label + ":\n";
        }
    }

    static public class BrFin extends Instruction{
        String br;

        public BrFin(String br){
            this.br = br;
        }

        public String toString() {
            return "br label %" + br + "\n\n";
        }
    }

    static public class IfThen extends Instruction{
        String variable;
        String label;
        String label2;

        public IfThen(String variable, String label, String label2){
            this.variable = variable;
            this.label=label;
            this.label2=label2;
        }

        public String toString() {
            return "br i1 " + variable + ", label %" + label + ", label %" + label2 + "\n\n";
        }

    }

    static public class Variable extends Instruction{
        String variable;
        Type type;

        public Variable(String variable, Type type){
            this.variable = variable;
            this.type = type;
        }

        public String toString() {
            return "%" + variable + " = alloca " + type + "\n";
        }
    }

    static public class VarExp extends Instruction{
        String tmp;
        String variable;
        Type type;

        public VarExp(String tmp, String variable, Type type){
            this.tmp = tmp;
            this.variable = variable;
            this.type = type;
        }

        public String toString() {
            return tmp + " = load " + type + ", " + type + "* %" + variable + "\n";
        }
    }

    static public class Aff extends Instruction {
        Type type;
        String left;
        String right;

        public Aff(Type type, String left, String right) {
            this.type = type;
            this.left = left;
            this.right = right;
        }

        public String toString() {
            return  "store " + type + " " + right + ", " + type + "* %" + left +  "\n";
        }
    }

    static public class Add extends Instruction {
        Type type;
        String left;
        String right;
        String lvalue;

        public Add(Type type, String left, String right, String lvalue) {
            this.type = type;
            this.left = left;
            this.right = right;
            this.lvalue = lvalue;
        }

        public String toString() {
            return lvalue + " = add " + type + " " + left + ", " + right +  "\n";
        }
    }

    static public class Sub extends Instruction {
        Type type;
        String left;
        String right;
        String lvalue;

        public Sub(Type type, String left, String right, String lvalue) {
            this.type = type;
            this.left = left;
            this.right = right;
            this.lvalue = lvalue;
        }

        public String toString() {
            return lvalue + " = sub " + type + " " + left + ", " + right +  "\n";
        }
    }

    static public class Times extends Instruction {
        Type type;
        String left;
        String right;
        String lvalue;

        public Times(Type type, String left, String right, String lvalue) {
            this.type = type;
            this.left = left;
            this.right = right;
            this.lvalue = lvalue;
        }

        public String toString() {
            return lvalue + " = mul " + type + " " + left + ", " + right +  "\n";
        }
    }

    static public class Div extends Instruction {
        Type type;
        String left;
        String right;
        String lvalue;

        public Div(Type type, String left, String right, String lvalue) {
            this.type = type;
            this.left = left;
            this.right = right;
            this.lvalue = lvalue;
        }

        public String toString() {
            return lvalue + " = sdiv " + type + " " + left + ", " + right +  "\n";
        }
    }

    static public class Return extends Instruction {
        Type type;
        String value;

        public Return(Type type, String value) {
            this.type = type;
            this.value = value;
        }

        public String toString() {
            return "ret " + type + " " + value + "\n";
        }
    }

    // TODO : other instructions
}
