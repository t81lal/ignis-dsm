package com.bilbtopia.forager.dsm;

public abstract class BasePrinter<Self extends BasePrinter<Self>> {
	private static final String WORD_SEP = " ";
	private static final String INDENT_STR = "   ";

	private final String wordSep, indentStr;

	private boolean built;
	private StringBuilder builder;
	private String builtString;

	private boolean requireWordSep;
	private int indentLevel;
	private int pendingNewLines;
	private boolean isIndentPrimed;

	public BasePrinter() {
		this(WORD_SEP, INDENT_STR);
	}

	public BasePrinter(String wordSep, String indentStr) {
		this.wordSep = wordSep;
		this.indentStr = indentStr;
		this.built = false;
		this.requireWordSep = false;
		this.pendingNewLines = 0;
		this.isIndentPrimed = true;
		this.indentLevel = 0;
		this.builder = new StringBuilder();
	}

	@Override
	public String toString() {
		if (!built) {
			builtString = builder.toString();
			built = true;
		}
		return builtString;
	}

	private void __emit(String str) {
		if (built) {
			built = false;
			builtString = null;
		}
		builder.append(str);
	}

	private void emitWithIndent(String str) {
		while (pendingNewLines > 0) {
			__emit(System.lineSeparator());
			pendingNewLines--;
		}
		if (isIndentPrimed) {
			// Finally do the indent
			for (int i = 0; i < indentLevel; i++) {
				__emit(indentStr);
			}
			isIndentPrimed = false;
		}
		__emit(str);
	}

	private Self _newline() {
		requireWordSep = false;
		isIndentPrimed = true;
		return getSelf();
	}

	public Self newLine() {
		// This doesn't actually emit a newline or indent, it only primes the __emit method to do it
		// when it finally receives an input
		pendingNewLines = 1;
		return _newline();
	}

	public Self forceNewLine() {
		pendingNewLines++;
		return _newline();
	}

	public Self emit(String str, boolean printSep) {
		if (requireWordSep && printSep) {
			emitWithIndent(wordSep);
		}
		emitWithIndent(str);
		requireWordSep = true;
		return getSelf();
	}

	public Self emit(String str) {
		return emit(str, true);
	}

	private Self changeIndentLevel(int delta) {
		indentLevel += delta;
		return getSelf();
	}

	@SuppressWarnings("unchecked")
	protected Self getSelf() {
		return (Self) this;
	}

	public Self incIndent() {
		return changeIndentLevel(1);
	}

	public Self decIndent() {
		return changeIndentLevel(-1);
	}
}
