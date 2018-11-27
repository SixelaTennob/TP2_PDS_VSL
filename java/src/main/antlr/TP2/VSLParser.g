parser grammar VSLParser;

options {
  language = Java;
  tokenVocab = VSLLexer;
}

@header {
  package TP2;

  import java.util.stream.Collectors;
  import java.util.Arrays;
}


// TODO : other rules

program returns [ASD.Program out]
    : a=bloc EOF {$out = new ASD.Program($a.out); }
    ;

bloc returns [ASD.Bloc out]
    //déclaration d'une ArrayList de type expression_basse
    :{ List<ASD.Instruction> ListInstruction = new ArrayList<>(); }//{} spécifie au parser que c'est du code java pas une recherche de tokens
     (i=instruction {ListInstruction.add($i.out);})*  { $out = new ASD.BlocExt(ListInstruction); }
     |LA (i=instruction {ListInstruction.add($i.out);})*  RA { $out = new ASD.BlocExt(ListInstruction); }
     //Pas besoin de $ car c'est une variable synthétisée (qui existe que dans le code java) contrairement à e qui est une variable héritée
    ;
instruction returns [ASD.Instruction out]
     :a=affectation {$out = $a.out;}
     |e=expression_basse {$out = $e.out;}
     |INT v=variable {$out = $v.out;}
     |IF c=condition THEN b=bloc FI {$out.add(new ASD.IfThen($c.out,$b.out));}
     |IF c=condition THEN b=bloc ELSE b2=bloc FI {$out.add(new ASD.IfThenElse($c.out,$b.out,$b2.out));}
     |WHILE c=condition DO b=bloc DONE {$out.add(new ASD.While($c.out,$b.bloc));}
     |RET e=expression_basse {$out = new ASD.Retourne ($e.out);}
     ;

condition returns [ASD.Condition out]
    : NOT e=expression_basse {$out = new ASD.ConditionNot($e.out);}
    | e=xpression_basse {$out = new ASD.Condition($e.out);}
    ;
variable returns [ASD.Instruction out]
    : {List<String> listVariable = new ArrayList<>();}
     (IDENT {listVariable.add($IDENT.text);} VIR)* IDENT {listVariable.add($IDENT.text);} { $out = new ASD.VariableExt(listVariable); }
    ;

affectation returns [ASD.Affectation out]
    :IDENT AFF r=expression_basse { $out = new ASD.Affectation($IDENT.text,$r.out); }
    ;

expression_basse returns [ASD.Instruction out]
	:l=expression_basse MINUS r=expression_haute  { $out = new ASD.SubExpression($l.out, $r.out); }
    |l=expression_basse PLUS r=expression_haute  { $out = new ASD.AddExpression($l.out, $r.out); }
    |e=expression_haute {  $out = $e.out; }
	;

expression_haute returns [ASD.Instruction out]
    :l=expression_haute TIMES r=primary  { $out = new ASD.TimesExpression($l.out, $r.out); }
    |l=expression_haute DIV r=primary  { $out = new ASD.DivExpression($l.out, $r.out); }
    |e=primary {  $out = $e.out; }
    ;


primary returns [ASD.Instruction out]
    : INTEGER { $out = new ASD.IntegerExpression($INTEGER.int); }
    | IDENT {$out = new ASD.VarExpression($IDENT.text);}
    | LP e=expression_basse RP { $out=$e.out; }
    ;