package TP2;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ASD {
	static public class Program {
		//Expression e; // What a program contains. TODO : change when you extend the language
        Bloc e;
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

			// computes the IR of the expression
			// add a return instruction
			//Llvm.Instruction ret = new Llvm.Return(retExpr.type.toLlvmType(), retExpr.result);
			//retExpr.ir.appendCode(ret);

			return e.toIR().ir;
		}
	}

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
        List<Expression> listExpression;
        List<Affectation> listAffectation;

        public BlocExt(List<Expression> listExpression,List<Affectation> listAffectation) {
            this.listAffectation = listAffectation;
            this.listExpression = listExpression;
        }
        // Pretty-printer
        public String pp() {
            String s="{";
            for(Expression e :listExpression){
                s +=e.pp();
            }
            for(Affectation a :listAffectation){
                s+=a.pp();
            }
            return s + "}";
        }

        public RetBloc toIR() throws TypeException {
            Llvm.IR blocIR = new Llvm.IR(Llvm.empty(),Llvm.empty());
            for(Expression e :listExpression){
                 blocIR.append(e.toIR().ir);
            }
            for(Affectation a :listAffectation){
                blocIR.append(a.toIR().ir);
            }
            return new RetBloc(blocIR);
        }
    }


    static public abstract class Variable {
        public abstract String pp();

        public abstract RetVariable toIR() throws TypeException;


        // Object returned by toIR on expressions, with IR + synthesized attributes
        static public class RetVariable {

            public String variable;

            public RetVariable(String variable) {
                this.variable = variable;
            }
        }
    }

    static public class VariableExt extends Variable {
        List<String> listVariable;

        public VariableExt (List<String> listVariable){
            this.listVariable = listVariable;
        }

        //Pretty Printer
        public String pp() {
            String decl="INT";
            for(String e :listVariable){
                decl +=e +",";
            }
            return decl;
        }

        public RetVariable toIR() throws TypeException {
            String ret="";
            for(String e :listVariable){
                Llvm.Instruction var = new Llvm.Variable(e);
                ret +=e;
            }
            return new RetVariable(ret);
        }
    }

	static public class Affectation extends Instruction {
	    String left;
	    Expression right;

        public Affectation(String left, Expression right) {
            this.left = left;
            this.right = right;
        }

        // Pretty-printer
        public String pp() {
            return left + " := " + right.pp();
        }
        // IR generation
        public RetInstruction toIR() throws TypeException {
            Expression.RetExpression rightRet = right.toIR();
            Llvm.Instruction aff = new Llvm.Aff(rightRet.type.toLlvmType(),left,rightRet.result);
            rightRet.ir.appendCode(aff);
            return new RetInstruction(rightRet.ir,rightRet.type,left);
        }

    }

	static public abstract class Expression {
		public abstract String pp();

		public abstract RetExpression toIR() throws TypeException;

		// Object returned by toIR on expressions, with IR + synthesized attributes
		static public class RetExpression {
			// The LLVM IR:
			public Llvm.IR ir;
			// And additional stuff:
			public Type type; // The type of the expression
			public String result; // The name containing the expression's result
			// (either an identifier, or an immediate value)

			public RetExpression(Llvm.IR ir, Type type, String result) {
				this.ir = ir;
				this.type = type;
				this.result = result;
			}
		}
	}


	// Concrete class for Expression: add case
	static public class AddExpression extends Expression {
		Expression left;
		Expression right;

		public AddExpression(Expression left, Expression right) {
			this.left = left;
			this.right = right;
		}

		// Pretty-printer
		public String pp() {
			return "(" + left.pp() + " + " + right.pp() + ")";
		}

		// IR generation
		public RetExpression toIR() throws TypeException {
			RetExpression leftRet = left.toIR();
			RetExpression rightRet = right.toIR();

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
			return new RetExpression(leftRet.ir, leftRet.type, result);
		}
	}

//Concrete class for Expression: sub case
	static public class SubExpression extends Expression {
		Expression left;
		Expression right;

		public SubExpression(Expression left, Expression right) {
			this.left = left;
			this.right = right;
		}

		// Pretty-printer
		public String pp() {
			return "(" + left.pp() + " - " + right.pp() + ")";
		}

		// IR generation
		public RetExpression toIR() throws TypeException {
			RetExpression leftRet = left.toIR();
			RetExpression rightRet = right.toIR();

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

			// return the generated IR, plus the type of this expression
			// and where to find its result
			return new RetExpression(leftRet.ir, leftRet.type, result);
		}
	}

	//Concrete class for Expression: Times case
	static public class TimesExpression extends Expression {
		Expression left;
		Expression right;

		public TimesExpression(Expression left, Expression right) {
			this.left = left;
			this.right = right;
		}

		// Pretty-printer
		public String pp() {
			return "(" + left.pp() + " * " + right.pp() + ")";
		}

		// IR generation
		public RetExpression toIR() throws TypeException {
			RetExpression leftRet = left.toIR();
			RetExpression rightRet = right.toIR();

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

			// return the generated IR, plus the type of this expression
			// and where to find its result
			return new RetExpression(leftRet.ir, leftRet.type, result);
		}
	}
	
	
	//Concrete class for Expression: Div case
		static public class DivExpression extends Expression {
			Expression left;
			Expression right;

			public DivExpression(Expression left, Expression right) {
				this.left = left;
				this.right = right;
			}

			// Pretty-printer
			public String pp() {
				return "(" + left.pp() + " / " + right.pp() + ")";
			}

			// IR generation
			public RetExpression toIR() throws TypeException {
				RetExpression leftRet = left.toIR();
				RetExpression rightRet = right.toIR();

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
				return new RetExpression(leftRet.ir, leftRet.type, result);
			}
		}

	// Concrete class for Expression: constant (integer) case
	static public class IntegerExpression extends Expression {
		int value;

		public IntegerExpression(int value) {
			this.value = value;
		}

		public String pp() {
			return "" + value;
		}

		public RetExpression toIR() {
			// Here we simply return an empty IR
			// the `result' of this expression is the integer itself (as string)
			return new RetExpression(new Llvm.IR(Llvm.empty(), Llvm.empty()), new Int(), "" + value);
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
