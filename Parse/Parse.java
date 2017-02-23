package Parse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import Absyn.Exp;
import ErrorMsg.ErrorMsg;
import Parse.Grm;
import Parse.Yylex;


public class Parse {
	public ErrorMsg errorMsg;
	
	static String symnames[] = new String[100];
	static {

		symnames[sym.FUNCTION] = "FUNCTION";
		symnames[sym.EOF] = "EOF";
		symnames[sym.INT] = "INT";
		symnames[sym.GT] = "GT";
		symnames[sym.DIVIDE] = "DIVIDE";
		symnames[sym.COLON] = "COLON";
		symnames[sym.ELSE] = "ELSE";
		symnames[sym.OR] = "OR";
		symnames[sym.NIL] = "NIL";
		symnames[sym.DO] = "DO";
		symnames[sym.GE] = "GE";
		symnames[sym.error] = "error";
		symnames[sym.LT] = "LT";
		symnames[sym.OF] = "OF";
		symnames[sym.MINUS] = "MINUS";
		symnames[sym.ARRAY] = "ARRAY";
		symnames[sym.TYPE] = "TYPE";
		symnames[sym.FOR] = "FOR";
		symnames[sym.TO] = "TO";
		symnames[sym.TIMES] = "TIMES";
		symnames[sym.COMMA] = "COMMA";
		symnames[sym.LE] = "LE";
		symnames[sym.IN] = "IN";
		symnames[sym.END] = "END";
		symnames[sym.ASSIGN] = "ASSIGN";
		symnames[sym.STRING] = "STRING";
		symnames[sym.DOT] = "DOT";
		symnames[sym.LPAREN] = "LPAREN";
		symnames[sym.RPAREN] = "RPAREN";
		symnames[sym.IF] = "IF";
		symnames[sym.SEMICOLON] = "SEMICOLON";
		symnames[sym.ID] = "ID";
		symnames[sym.WHILE] = "WHILE";
		symnames[sym.LBRACK] = "LBRACK";
		symnames[sym.RBRACK] = "RBRACK";
		symnames[sym.NEQ] = "NEQ";
		symnames[sym.VAR] = "VAR";
		symnames[sym.BREAK] = "BREAK";
		symnames[sym.AND] = "AND";
		symnames[sym.PLUS] = "PLUS";
		symnames[sym.LBRACE] = "LBRACE";
		symnames[sym.RBRACE] = "RBRACE";
		symnames[sym.LET] = "LET";
		symnames[sym.THEN] = "THEN";
		symnames[sym.EQ] = "EQ";
	}
	
	public Parse(ErrorMsg err) {
		errorMsg = err;
	}
	
	public  void printToken(String filename) throws IOException {
		InputStream inp = new java.io.FileInputStream(filename);
		Yylex lexer = new Yylex(inp, errorMsg);
		java_cup.runtime.Symbol tok;
		System.out.println("======================================");
		System.out.println("Lexing...");
		do {
			tok = lexer.nextToken();
			System.out.println(symnames[tok.sym] + " " + tok.left);
		} while (tok.sym != sym.EOF);
		System.out.println("Complete.");
		System.out.println("======================================");
		inp.close();
	}

	public Exp parse(String filename) throws IOException {
		InputStream inp;
		try {
			inp = new java.io.FileInputStream(filename);
		}
		catch (FileNotFoundException e) {
			System.out.println("File not found: " + filename);
			return null;
		}
		try {
			printToken(filename);
			Grm parser = new Grm(new Yylex(inp, errorMsg), errorMsg);
			parser.parse();
			Exp absyn = parser.parseResult;
			if (errorMsg.anyErrors) {
				System.out.println("Error occured, stop compiling");
				return null;
			}
			else return absyn;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		finally {
			try {
				inp.close();
			}
			catch (java.io.IOException e) {
			}
		}
	}
}
