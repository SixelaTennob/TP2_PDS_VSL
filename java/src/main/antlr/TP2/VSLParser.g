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
    //: e=expression_basse EOF { $out = new ASD.Program($e.out); } // TODO : change when you extend the language
    : a=bloc EOF {$out = new ASD.Program($a.out); }
    ;

bloc returns [ASD.Bloc out]
    //déclaration d'une ArrayList de type expression
    :{ List<ASD.Expression> ListExpression = new ArrayList<>(); List<ASD.Affectation> ListAffectation = new ArrayList<>();}//{} spécifie au parser que c'est du code java pas une recherche de tokens

     LA ((a=affectation {ListAffectation.add($a.out);})* | (e=expression_basse {ListExpression.add($e.out);})*) RA { $out = new ASD.BlocExt(ListExpression,ListAffectation); }
     //Pas besoin de $ car c'est une variable synthétisée (qui existe que dans le code java) contrairement à e qui est une variable héritée
    ;

variable returns [ASD.Variable out]
    : {List<String> listVariable = new ArrayList<>();}
    INT (IDENT {listVariable.add($IDENT.text);} VIR)* IDENT {listVariable.add($IDENT.text);} { $out = new ASD.VariableExt(listVariable); }
    ;

affectation returns [ASD.Affectation out]
    :IDENT AFF r=expression_basse { $out = new ASD.Affectation($IDENT.text,$r.out); }
    ;
	
expression_basse returns [ASD.Expression out]
	:l=expression_basse MINUS r=expression_haute  { $out = new ASD.SubExpression($l.out, $r.out); }
    |l=expression_basse PLUS r=expression_haute  { $out = new ASD.AddExpression($l.out, $r.out); }
    |e=expression_haute {  $out = $e.out; }
	;

expression_haute returns [ASD.Expression out]
    :l=expression_haute TIMES r=primary  { $out = new ASD.TimesExpression($l.out, $r.out); }
    |l=expression_haute DIV r=primary  { $out = new ASD.DivExpression($l.out, $r.out); }
    |e=primary {  $out = $e.out; }
    ;


primary returns [ASD.Expression out]
    : INTEGER { $out = new ASD.IntegerExpression($INTEGER.int); }
    | LP e=expression_basse RP { $out=$e.out; }
    ;
