package com.m2r.easyparser;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Parser<R> {

	protected boolean useTrim;
	protected List<Token> tokens;
	protected LineInfo lineInfo;
	protected int pos;
	protected int last;
	protected R result;

	protected Parser(boolean useTrim) {
		this.useTrim = useTrim;
	}

	@SuppressWarnings("rawtypes")
	protected Object newResult(){
		try {
			return ((Class)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected void initResult(R result) {

	}

	protected R execute(Reader reader) throws ParserException {
		String input;
		try {
			input = IOUtils.toString(reader);
			return execute(input);
		}
		catch (IOException e) {
			throw new ParserException("Erro na leitura dos dados", e);
		}
	}

	@SuppressWarnings("unchecked")
	protected R execute(String input) throws ParserException {
		result = (R) newResult();
		initResult(result);
		lex(input);
		syn();
		sem();
		return result;
	}

	protected abstract boolean ignoreToken(Token token);

	protected void lex(String sequence) throws ParserException {
		tokens = new LinkedList<Token>();
		Token token;
		lineInfo = LineInfo.build(1, 0);
		pos = 0;
		last = 0;
		while ((token = nextToken(sequence)) != null) {
			if (!ignoreToken(token)) {
				tokens.add(token);
			}
		}
	}

	protected void sem() throws ParserException {
	}

	protected List<Token> getTokens() {
		return tokens;
	}

	protected abstract ITokenType[] getTokenTypes();

	protected Token nextToken(String sequence) throws ParserException {
		String subSequence = sequence.substring(pos);
		for (ITokenType type : getTokenTypes()) {
			Matcher m = type.getRegex().matcher(subSequence);
			if (m.find()) {
				String value = m.group();
				if (LineInfo.isBreakLine(value)) {
					lineInfo.incLine();
				}
				if (useTrim) {
					value = value.trim();
				}
				pos += m.group().length();
				LineInfo tokenLineInfo = lineInfo.clone();
				lineInfo.incPos(m.group().length());
				return new Token(type, value, tokenLineInfo, pos);
			}
		}
		if (subSequence.length() > 0) {
			throw new LexicalException("Token não identificado " + lineInfo.toString(), pos);
		}
		return null;
	}

	static public class Token {

		private ITokenType type;
		private String value;
		private LineInfo lineInfo;
		private int pos;

		public Token(ITokenType type, String value, LineInfo lineInfo, int pos) {
			this.type = type;
			this.value = value;
			this.lineInfo = lineInfo;
			this.pos = pos;
		}

		public ITokenType getType() {
			return type;
		}

		public String getValue() {
			return value;
		}

		public LineInfo getLineInfo() {
			return lineInfo;
		}

		public int getPos() {
			return pos;
		}

		@Override
		public String toString() {
			return "<"+type.name()+","+value+">";
		}

	}

	static public interface ITokenType {

		public Pattern getRegex();
		public String name();

	}

	protected void syn() throws ParserException {
		pos = 0;
		if (!(exp() && isEnd())) {
			Token token = tokens.get(pos);
			throw new SyntacticException("Estrutura da expressão incorreta " + token.getLineInfo().toString(), last);
		}
	}

	protected abstract boolean exp();

	protected boolean term(ITokenType type) {
		return term(type, null);
	}

	protected boolean term(ITokenType type, String value) {
		Token token = nextToken();
		return token != null && token.getType().equals(type) && (value == null || value.equals(token.getValue()));
	}

	protected Token nextToken() {
		Token token = tokens.size() > pos ? tokens.get(pos++) : null;
		if (token != null) {
			last = pos;
		}
		return token;
	}

	protected boolean reset(int newPos) {
		pos = newPos;
		return false;
	}

	protected boolean isEnd() {
		return pos == tokens.size();
	}

	@SuppressWarnings("serial")
	public static class LexicalException extends ParserException {
		public LexicalException(String s, int errorOffset) {
			super(s, errorOffset);
		}
	}
	@SuppressWarnings("serial")
	public static class SyntacticException extends ParserException {
		public SyntacticException(String s, int errorOffset) {
			super(s, errorOffset);
		}
	}
	@SuppressWarnings("serial")
	public static class SemanticException extends ParserException {
		public SemanticException(String s, int errorOffset) {
			super(s, errorOffset);
		}
	}

}
