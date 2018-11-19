package TP2;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ASD {
    static public class Program {
        Bloc e; // What a program contains. TODO : change when you extend the language

        public Program(Bloc e) {
            this.e = e;
        }

        // Pretty-printer
        public String pp() {
            return e.pp();
        }

        // IR generation
        public Llvm.IR toIR() throws TypeException {
            // TODO : change when you extend the language
            return e.toIR().ir;
        }
    }

    // All toIR methods returns the IR, plus extra information (synthesized attributes)
    // They can take extra arguments (inherited attributes)

    static public abstract class Instruction {
        public abstract String pp();
        public abstract RetInstruction toIR() throws TypeException;

        // Object returned by toIR on expressions, with IR + synthesized attributes
        static public class RetInstruction {
            // The LLVM IR:
            public Llvm.IR ir;
            // And additional stuff:
            public Type type; // The type of the expression
            public String result; // The name containing the expression's result
            // (either an identifier, or an immediate value)

            public RetInstruction(Llvm.IR ir, Type type, String result) {
                this.ir = ir;
                this.type = type;
                this.result = result;
            }
        }
    }

    static public abstract class Bloc {
        public abstract String pp();

        public abstract RetBloc toIR() throws TypeException;


        // Object returned by toIR on expressions, with IR + synthesized attributes
        static public class RetBloc {
            // The LLVM IR:
            public Llvm.IR ir;

            public RetBloc(Llvm.IR ir) {
                this.ir = ir;
            }
        }
    }


    static public class BlocExt extends Bloc {
        List<Instruction> listInstruction;

        public BlocExt(List<Instruction> listInstruction) {

            this.listInstruction = listInstruction;
        }
        // Pretty-printer
        public String pp() {
            String s="{";
            for(Instruction i :listInstruction){
                s +=i.pp();
            }
            return s + "}";
        }
        public RetBloc toIR() throws TypeException {
            Llvm.IR blocIR = new Llvm.IR(Llvm.empty(),Llvm.empty());
            for(Instruction i :listInstruction){
                blocIR.append(i.toIR().ir);
            }
            return new RetBloc(blocIR);
        }
    }

    static public class VariableExt extends Instruction {
        List<String> listVariable;

        public VariableExt (List<String> listVariable){
            this.listVariable = listVariable;
        }

        //Pretty Printer
        public String pp() {
            String decl="INT";
            int i =1;
            for(String e :listVariable){
                if(i!=listVariable.size()){
                    decl = e +",";
                }else decl = e;
            }
            return decl;
        }

        public RetInstruction toIR() throws TypeException {
            Type tp = new Int();
            Llvm.IR VariableIR = new Llvm.IR(Llvm.empty(),Llvm.empty());
            for(String v :listVariable){
                Llvm.Instruction var = new Llvm.Variable(v,tp.toLlvmType());
                VariableIR.appendCode(var);
            }
            return new RetInstruction(VariableIR,tp,null);
        }
    }

    static public class Affectation extends Instruction {
        String left;
        Instruction right;

        public Affectation(String left, Instruction right) {
            this.left = left;
            this.right = right;
        }

        // Pretty-printer
        public String pp() {
            return left + " := " + right.pp();
        }
        // IR generation
        public RetInstruction toIR() throws TypeException {
            Instruction.RetInstruction rightRet = right.toIR();
            Llvm.Instruction aff = new Llvm.Aff(rightRet.type.toLlvmType(),left,rightRet.result);
            rightRet.ir.appendCode(aff);
            return new RetInstruction(rightRet.ir,rightRet.type,left);
        }

    }

    // Concrete class for Expression: add case
    static public class AddExpression extends Instruction {
        Instruction left;
        Instruction right;

        public AddExpression(Instruction left, Instruction right) {
            this.left = left;
            this.right = right;
        }

        // Pretty-printer
        public String pp() {
            return "(" + left.pp() + " + " + right.pp() + ")";
        }

        // IR generation
        public RetInstruction toIR() throws TypeException {
            RetInstruction leftRet = left.toIR();
            RetInstruction rightRet = right.toIR();

            // We check if the types mismatches
            if (!leftRet.type.equals(rightRet.type)) {
                throw new TypeException("type mismatch: have " + leftRet.type + " and " + rightRet.type);
            }

            // We base our build on the left generated IR:
            // append right code
            leftRet.ir.append(rightRet.ir);

            // allocate a new identifier for the result
            String result = Utils.newtmp();

            // new add instruction result = left + right
            Llvm.Instruction add = new Llvm.Add(leftRet.type.toLlvmType(), leftRet.result, rightRet.result, result);

            // append this instruction
            leftRet.ir.appendCode(add);

            // return the generated IR, plus the type of this expression
            // and where to find its result
            return new RetInstruction(leftRet.ir, leftRet.type, result);
        }
    }

    //Concrete class for Instruction: sub case
    static public class SubExpression extends Instruction {
        Instruction left;
        Instruction right;

        public SubExpression(Instruction left, Instruction right) {
            this.left = left;
            this.right = right;
        }

        // Pretty-printer
        public String pp() {
            return "(" + left.pp() + " - " + right.pp() + ")";
        }

        // IR generation
        public RetInstruction toIR() throws TypeException {
            RetInstruction leftRet = left.toIR();
            RetInstruction rightRet = right.toIR();

            // We check if the types mismatches
            if (!leftRet.type.equals(rightRet.type)) {
                throw new TypeException("type mismatch: have " + leftRet.type + " and " + rightRet.type);
            }

            // We base our build on the left generated IR:
            // append right code
            leftRet.ir.append(rightRet.ir);

            // allocate a new identifier for the result
            String result = Utils.newtmp();

            // new add instruction result = left - right
            Llvm.Instruction sub = new Llvm.Sub(leftRet.type.toLlvmType(), leftRet.result, rightRet.result, result);

            // append this instruction
            leftRet.ir.appendCode(sub);

            // return the generated IR, plus the type of this Instruction
            // and where to find its result
            return new RetInstruction(leftRet.ir, leftRet.type, result);
        }
    }

    //Concrete class for Instruction: Times case
    static public class TimesExpression extends Instruction {
        Instruction left;
        Instruction right;

        public TimesExpression(Instruction left, Instruction right) {
            this.left = left;
            this.right = right;
        }

        // Pretty-printer
        public String pp() {
            return "(" + left.pp() + " * " + right.pp() + ")";
        }

        // IR generation
        public RetInstruction toIR() throws TypeException {
            RetInstruction leftRet = left.toIR();
            RetInstruction rightRet = right.toIR();

            // We check if the types mismatches
            if (!leftRet.type.equals(rightRet.type)) {
                throw new TypeException("type mismatch: have " + leftRet.type + " and " + rightRet.type);
            }

            // We base our build on the left generated IR:
            // append right code
            leftRet.ir.append(rightRet.ir);

            // allocate a new identifier for the result
            String result = Utils.newtmp();

            // new add instruction result = left + right
            Llvm.Instruction times = new Llvm.Times(leftRet.type.toLlvmType(), leftRet.result, rightRet.result, result);

            // append this instruction
            leftRet.ir.appendCode(times);

            // return the generated IR, plus the type of this Expression
            // and where to find its result
            return new RetInstruction(leftRet.ir, leftRet.type, result);
        }
    }


    //Concrete class for Instruction: Div case
    static public class DivExpression extends Instruction {
        Instruction left;
        Instruction right;

        public DivExpression(Instruction left, Instruction right) {
            this.left = left;
            this.right = right;
        }

        // Pretty-printer
        public String pp() {
            return "(" + left.pp() + " / " + right.pp() + ")";
        }

        // IR generation
        public RetInstruction toIR() throws TypeException {
            RetInstruction leftRet = left.toIR();
            RetInstruction rightRet = right.toIR();

            // We check if the types mismatches
            if (!leftRet.type.equals(rightRet.type)) {
                throw new TypeException("type mismatch: have " + leftRet.type + " and " + rightRet.type);
            }

            // We base our build on the left generated IR:
            // append right code
            leftRet.ir.append(rightRet.ir);

            // allocate a new identifier for the result
            String result = Utils.newtmp();

            // new add instruction result = left + right
            Llvm.Instruction div = new Llvm.Div(leftRet.type.toLlvmType(), leftRet.result, rightRet.result, result);

            // append this instruction
            leftRet.ir.appendCode(div);

            // return the generated IR, plus the type of this expression
            // and where to find its result
            return new RetInstruction(leftRet.ir, leftRet.type, result);
        }
    }

    // Concrete class for Expression: constant (integer) case
    static public class IntegerExpression extends Instruction {
        int value;
        public IntegerExpression(int value) {
            this.value = value;
        }

        public String pp() {
            return "" + value;
        }

        public RetInstruction toIR() {
            // Here we simply return an empty IR
            // the `result' of this expression is the integer itself (as string)
            return new RetInstruction(new Llvm.IR(Llvm.empty(), Llvm.empty()), new Int(), "" + value);
        }
    }

    // Warning: this is the type from VSL+, not the LLVM types!
    static public abstract class Type {
        public abstract String pp();
        public abstract Llvm.Type toLlvmType();
    }

    static class Int extends Type {
        public String pp() {
            return "INT";
        }

        @Override public boolean equals(Object obj) {
            return obj instanceof Int;
        }

        public Llvm.Type toLlvmType() {
            return new Llvm.Int();
        }
    }
}
