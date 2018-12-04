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
            String s = "{";
            for (Instruction i : listInstruction) {
                s += i.pp();
            }
            return s + "}";
        }

        public RetBloc toIR() throws TypeException {
            Llvm.IR blocIR = new Llvm.IR(Llvm.empty(), Llvm.empty());
            for (Instruction i : listInstruction) {
                blocIR.append(i.toIR().ir);
            }
            return new RetBloc(blocIR);
        }
    }

    static public class ConditionTrue extends Instruction{
        Instruction e;

        public ConditionTrue(Instruction e){
            this.e = e;
        }

        //Pretty Printer
        public String pp() {
            return "IF" + e.pp() + "\n";
        }


        public RetInstruction toIR() throws TypeException {
            Instruction.RetInstruction retE = this.e.toIR();
            String tmp = Utils.newtmp();
            Type tp = new Int();
            Llvm.IR cond = new Llvm.IR(Llvm.empty(),Llvm.empty());
            Llvm.Instruction testCond = new Llvm.ConditionTrue(tmp, tp.toLlvmType(),retE.result);
            cond.append(retE.ir);
            cond.appendCode(testCond);
            return new RetInstruction(cond,tp,tmp);
        }
    }

    static public class ConditionFalse extends Instruction{
        Instruction e;

        public ConditionFalse(Instruction e){this.e = e;}

        //Pretty Printer
        public String pp() {
            return "IF" + e.pp() + "\n";
        }


        public RetInstruction toIR() throws TypeException {
            Instruction.RetInstruction retE = this.e.toIR();
            String tmp = Utils.newtmp();
            Type tp = new Int();
            Llvm.IR cond = new Llvm.IR(Llvm.empty(),Llvm.empty());
            Llvm.Instruction testCond = new Llvm.ConditionFalse(tmp, tp.toLlvmType(),retE.result);
            cond.append(retE.ir);
            cond.appendCode(testCond);
            return new RetInstruction(cond,tp,tmp);
        }
    }

    static public class While extends Instruction {
        Instruction e;
        Bloc bDo;

        public While (Instruction e, Bloc bDo) {
            this.e=e;
            this.bDo=bDo;
        }

        //Pretty Printer
        public String pp() {
            return "WHILE " + e + "\nDO\n{\n\t" + bDo + "DONE";
        }

        public RetInstruction toIR() throws TypeException {
            String labelWhile = Utils.newlab("while");
            String labelDo = Utils.newlab("do");
            String labelDone = Utils.newlab("done");

            Llvm.IR whileDo = new Llvm.IR(Llvm.empty(), Llvm.empty());
            Instruction.RetInstruction retE = this.e.toIR();
            Llvm.Instruction BrWhile = new Llvm.BrFin(labelWhile);
            Llvm.Instruction While = new Llvm.Label(labelWhile);
            Llvm.Instruction LDo = new Llvm.Label(labelDo);
            Llvm.Instruction BrDoOrDone = new Llvm.IfThen(retE.result,labelDo,labelDone);
            Llvm.Instruction Fin = new Llvm.Label(labelDone);

            whileDo.appendCode(BrWhile);
            whileDo.appendCode(While);
            whileDo.append(retE.ir);
            whileDo.appendCode(BrDoOrDone);
            whileDo.appendCode(LDo);
            whileDo.append(bDo.toIR().ir);
            whileDo.appendCode(BrWhile);
            whileDo.appendCode(Fin);
            return new RetInstruction(whileDo, null, null);
        }
    }

    static public class IfThen extends Instruction {
        Instruction e;
        Bloc bIf;

        public IfThen(Instruction e, Bloc bIf) {
            this.e=e;
            this.bIf=bIf;
        }

        //Pretty Printer
        public String pp() {
            return "THEN\n\t"+ bIf;
        }

        public RetInstruction toIR() throws TypeException {
            String labelIf = Utils.newlab("then");
            String labelFin = Utils.newlab("fi");

            Llvm.IR IfThenIR = new Llvm.IR(Llvm.empty(), Llvm.empty());
            Instruction.RetInstruction retE = this.e.toIR();
            Llvm.Instruction Br = new Llvm.IfThen(retE.result,labelIf,labelFin);
            Llvm.Instruction IfIR = new Llvm.Label(labelIf);
            Llvm.Instruction IfFin = new Llvm.BrFin(labelFin);
            Llvm.Instruction Fin = new Llvm.Label(labelFin);

            IfThenIR.append(retE.ir);
            IfThenIR.appendCode(Br);
            IfThenIR.appendCode(IfIR);
            IfThenIR.append(bIf.toIR().ir);
            IfThenIR.appendCode(IfFin);
            IfThenIR.appendCode(Fin);
            return new RetInstruction(IfThenIR, null, null);
        }
    }

    static public class IfThenElse extends Instruction {
        Instruction e;
        Bloc bIf;
        Bloc bElse;

        public IfThenElse(Instruction e, Bloc bIf, Bloc bElse) {
            this.e=e;
            this.bIf=bIf;
            this.bElse=bElse;
        }

        //Pretty Printer
        public String pp() {
            return "THEN\n\t" + bIf + "ELSE\n\t" + bElse;
        }

        public RetInstruction toIR() throws TypeException {
            String labelIf = Utils.newlab("if");
            String labelElse = Utils.newlab("else");
            String labelFin = Utils.newlab("fi");

            Llvm.IR IfThenElseIR = new Llvm.IR(Llvm.empty(), Llvm.empty());
            Instruction.RetInstruction retE = this.e.toIR();
            Llvm.Instruction Br = new Llvm.IfThen(retE.result,labelIf,labelElse);
            Llvm.Instruction IfIR = new Llvm.Label(labelIf);
            Llvm.Instruction IfFin = new Llvm.BrFin(labelFin);
            Llvm.Instruction ElseIR = new Llvm.Label(labelElse);
            Llvm.Instruction Fin = new Llvm.Label(labelFin);

            IfThenElseIR.append(retE.ir);
            IfThenElseIR.appendCode(Br);
            IfThenElseIR.appendCode(IfIR);
            IfThenElseIR.append(bIf.toIR().ir);
            IfThenElseIR.appendCode(IfFin);
            IfThenElseIR.appendCode(ElseIR);
            IfThenElseIR.append(bElse.toIR().ir);
            IfThenElseIR.appendCode(IfFin);
            IfThenElseIR.appendCode(Fin);
            return new RetInstruction(IfThenElseIR, null, null);
        }
    }

    static public class VariableExt extends Instruction {
        List<String> listVariable;

        public VariableExt(List<String> listVariable) {
            this.listVariable = listVariable;
        }

        //Pretty Printer
        public String pp() {
            String decl = "INT";
            int i = 1;
            for (String e : listVariable) {
                if (i != listVariable.size()) {
                    decl = e + ",";
                } else decl = e;
            }
            return decl;
        }

        public RetInstruction toIR() throws TypeException {
            Type tp = new Int();
            Llvm.IR VariableIR = new Llvm.IR(Llvm.empty(), Llvm.empty());
            for (String v : listVariable) {
                Llvm.Instruction var = new Llvm.Variable(v, tp.toLlvmType());
                VariableIR.appendCode(var);
            }
            return new RetInstruction(VariableIR, tp, null);
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
            Llvm.Instruction aff = new Llvm.Aff(rightRet.type.toLlvmType(), left, rightRet.result);
            rightRet.ir.appendCode(aff);
            return new RetInstruction(rightRet.ir, rightRet.type, left);
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

    static public class Retourne extends Instruction {
        Instruction e;

        public Retourne(Instruction e) {

            this.e = e;
        }

        @java.lang.Override
        public String pp() {
            return "RETURN " + e.pp();
        }

        @java.lang.Override
        public RetInstruction toIR() throws TypeException {
            Instruction.RetInstruction expr = e.toIR();
            Llvm.Instruction ret = new Llvm.Return(expr.type.toLlvmType(), expr.result);
            expr.ir.appendCode(ret);
            return new RetInstruction(expr.ir, expr.type, expr.result);
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

    static public class VarExpression extends Instruction {

        String value;
        Type type;

        public VarExpression(String value) {
            this.value = value;
            this.type = new Int();
        }

        public String pp() {
            return "" + value;
        }

        public RetInstruction toIR() {
            // Here we simply return an empty IR
            // the `result' of this expression is the integer itself (as string)
            String tmp = Utils.newtmp();
            Llvm.Instruction varExp = new Llvm.VarExp(tmp, value, type.toLlvmType());
            Llvm.IR res = new Llvm.IR(Llvm.empty(), Llvm.empty());
            res.appendCode(varExp);
            return new RetInstruction(res, type, "" + tmp);
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

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Int;
        }

        public Llvm.Type toLlvmType() {
            return new Llvm.Int();
        }
    }
}
