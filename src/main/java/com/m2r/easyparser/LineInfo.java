package com.m2r.easyparser;

public class LineInfo {

    private int line;
    private int pos;

    private LineInfo(int line, int pos) {
        this.line = line;
        this.pos = pos;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public static boolean isBreakLine(String text) {
        return text.equals("\n");
    }

    public void incLine() {
        this.line++;
        this.pos = 0;
    }

    public void incPos(int pos) {
        this.pos += pos;
    }

    public LineInfo clone() {
        return LineInfo.build(this.line, this.pos);
    }

    @Override
    public String toString() {
        return String.format("[line: %d, pos: %d]", line, pos);
    }

    public static LineInfo build(int line, int pos) {
        return new LineInfo(line, pos);
    }

}
