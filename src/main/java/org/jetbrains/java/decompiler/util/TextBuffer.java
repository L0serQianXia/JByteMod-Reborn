package org.jetbrains.java.decompiler.util;

import org.jetbrains.java.decompiler.main.DecompilerContext;

import java.util.*;
import java.util.Map.Entry;

public class TextBuffer {
    private final String myLineSeparator = DecompilerContext.getNewLineSeparator();
    private final String myIndent = (String)DecompilerContext.getProperty("ind");
    private final StringBuilder myStringBuilder;
    private Map myLineToOffsetMapping = null;
    private Map myLineMapping = null;

    public TextBuffer() {
        this.myStringBuilder = new StringBuilder();
    }

    public TextBuffer(int size) {
        this.myStringBuilder = new StringBuilder(size);
    }

    public TextBuffer(String text) {
        this.myStringBuilder = new StringBuilder(text);
    }

    public TextBuffer append(String str) {
        this.myStringBuilder.append(str);
        return this;
    }

    public TextBuffer append(char ch) {
        this.myStringBuilder.append(ch);
        return this;
    }

    public TextBuffer append(int i) {
        this.myStringBuilder.append(i);
        return this;
    }

    public TextBuffer appendLineSeparator() {
        this.myStringBuilder.append(this.myLineSeparator);
        return this;
    }

    public TextBuffer appendIndent(int length) {
        while(length-- > 0) {
            this.append(this.myIndent);
        }

        return this;
    }

    public TextBuffer prepend(String s) {
        this.myStringBuilder.insert(0, s);
        this.shiftMapping(s.length());
        return this;
    }

    public TextBuffer enclose(String left, String right) {
        this.prepend(left);
        this.append(right);
        return this;
    }

    public boolean containsOnlyWhitespaces() {
        for(int i = 0; i < this.myStringBuilder.length(); ++i) {
            if (this.myStringBuilder.charAt(i) != ' ') {
                return false;
            }
        }

        return true;
    }

    public String toString() {
        String original = this.myStringBuilder.toString();
        if (this.myLineToOffsetMapping != null && !this.myLineToOffsetMapping.isEmpty()) {
            StringBuilder res = new StringBuilder();
            String[] srcLines = original.split(this.myLineSeparator);
            int currentLineStartOffset = 0;
            int currentLine = 0;
            int previousMarkLine = 0;
            int dumpedLines = 0;
            ArrayList linesWithMarks = new ArrayList(this.myLineToOffsetMapping.keySet());
            Collections.sort(linesWithMarks);
            Iterator var9 = linesWithMarks.iterator();

            while(true) {
                while(var9.hasNext()) {
                    Integer markLine = (Integer)var9.next();

                    for(Integer markOffset = (Integer)this.myLineToOffsetMapping.get(markLine); currentLine < srcLines.length; ++currentLine) {
                        String line = srcLines[currentLine];
                        int lineEnd = currentLineStartOffset + line.length() + this.myLineSeparator.length();
                        if (markOffset <= lineEnd) {
                            int requiredLine = markLine - 1;
                            int linesToAdd = requiredLine - dumpedLines;
                            dumpedLines = requiredLine;
                            this.appendLines(res, srcLines, previousMarkLine, currentLine, linesToAdd);
                            previousMarkLine = currentLine;
                            break;
                        }

                        currentLineStartOffset = lineEnd;
                    }
                }

                if (previousMarkLine < srcLines.length) {
                    this.appendLines(res, srcLines, previousMarkLine, srcLines.length, srcLines.length - previousMarkLine);
                }

                return res.toString();
            }
        } else {
            return this.myLineMapping != null ? this.addOriginalLineNumbers() : original;
        }
    }

    private String addOriginalLineNumbers() {
        StringBuilder sb = new StringBuilder();
        int lineStart = 0;
        int count = 0;

        int lineEnd;
        for(int length = this.myLineSeparator.length(); (lineEnd = this.myStringBuilder.indexOf(this.myLineSeparator, lineStart)) > 0; lineStart = lineEnd + length) {
            ++count;
            sb.append(this.myStringBuilder.substring(lineStart, lineEnd));
            Set integers = (Set)this.myLineMapping.get(count);
            if (integers != null) {
                sb.append("//");
                Iterator var7 = integers.iterator();

                while(var7.hasNext()) {
                    Integer integer = (Integer)var7.next();
                    sb.append(' ').append(integer);
                }
            }

            sb.append(this.myLineSeparator);
        }

        if (lineStart < this.myStringBuilder.length()) {
            sb.append(this.myStringBuilder.substring(lineStart));
        }

        return sb.toString();
    }

    private void appendLines(StringBuilder res, String[] srcLines, int from, int to, int requiredLineNumber) {
        if (to - from > requiredLineNumber) {
            List strings = compactLines(Arrays.asList(srcLines).subList(from, to), requiredLineNumber);
            int separatorsRequired = requiredLineNumber - 1;
            Iterator var8 = strings.iterator();

            while(var8.hasNext()) {
                String s = (String)var8.next();
                res.append(s);
                if (separatorsRequired-- > 0) {
                    res.append(this.myLineSeparator);
                }
            }

            res.append(this.myLineSeparator);
        } else if (to - from <= requiredLineNumber) {
            int i;
            for(i = from; i < to; ++i) {
                res.append(srcLines[i]).append(this.myLineSeparator);
            }

            for(i = 0; i < requiredLineNumber - to + from; ++i) {
                res.append(this.myLineSeparator);
            }
        }

    }

    public int length() {
        return this.myStringBuilder.length();
    }

    public void setStart(int position) {
        this.myStringBuilder.delete(0, position);
        this.shiftMapping(-position);
    }

    public void setLength(int position) {
        this.myStringBuilder.setLength(position);
        if (this.myLineToOffsetMapping != null) {
            Map newMap = new HashMap();
            Iterator var3 = this.myLineToOffsetMapping.entrySet().iterator();

            while(var3.hasNext()) {
                Entry entry = (Entry)var3.next();
                if ((Integer)entry.getValue() <= position) {
                    newMap.put(entry.getKey(), entry.getValue());
                }
            }

            this.myLineToOffsetMapping = newMap;
        }

    }

    public TextBuffer append(TextBuffer buffer) {
        if (buffer.myLineToOffsetMapping != null && !buffer.myLineToOffsetMapping.isEmpty()) {
            this.checkMapCreated();
            Iterator var2 = buffer.myLineToOffsetMapping.entrySet().iterator();

            while(var2.hasNext()) {
                Entry entry = (Entry)var2.next();
                this.myLineToOffsetMapping.put(entry.getKey(), (Integer)entry.getValue() + this.myStringBuilder.length());
            }
        }

        this.myStringBuilder.append(buffer.myStringBuilder);
        return this;
    }

    private void shiftMapping(int shiftOffset) {
        if (this.myLineToOffsetMapping != null) {
            Map newMap = new HashMap();
            Iterator var3 = this.myLineToOffsetMapping.entrySet().iterator();

            while(var3.hasNext()) {
                Entry entry = (Entry)var3.next();
                int newValue = (Integer)entry.getValue();
                if (newValue >= 0) {
                    newValue += shiftOffset;
                }

                if (newValue >= 0) {
                    newMap.put(entry.getKey(), newValue);
                }
            }

            this.myLineToOffsetMapping = newMap;
        }

    }

    private void checkMapCreated() {
        if (this.myLineToOffsetMapping == null) {
            this.myLineToOffsetMapping = new HashMap();
        }

    }

    public int countLines() {
        return this.countLines(0);
    }

    public int countLines(int from) {
        return this.count(this.myLineSeparator, from);
    }

    public int count(String substring, int from) {
        int count = 0;
        int length = substring.length();

        for(int p = from; (p = this.myStringBuilder.indexOf(substring, p)) > 0; p += length) {
            ++count;
        }

        return count;
    }

    private static List compactLines(List srcLines, int requiredLineNumber) {
        if (srcLines.size() >= 2 && srcLines.size() > requiredLineNumber) {
            List res = new LinkedList(srcLines);

            int i;
            String s;
            for(i = res.size() - 1; i > 0; --i) {
                s = (String)res.get(i);
                if (s.trim().equals("{") || s.trim().equals("}")) {
                    res.set(i - 1, ((String)res.get(i - 1)).concat(s));
                    res.remove(i);
                }

                if (res.size() <= requiredLineNumber) {
                    return res;
                }
            }

            for(i = res.size() - 1; i > 0; --i) {
                s = (String)res.get(i);
                if (s.trim().isEmpty()) {
                    res.set(i - 1, ((String)res.get(i - 1)).concat(s));
                    res.remove(i);
                }

                if (res.size() <= requiredLineNumber) {
                    return res;
                }
            }

            return res;
        } else {
            return srcLines;
        }
    }

    public void dumpOriginalLineNumbers(int[] lineMapping) {
        if (lineMapping.length > 0) {
            this.myLineMapping = new HashMap();

            for(int i = 0; i < lineMapping.length; i += 2) {
                int key = lineMapping[i + 1];
                Set existing = (Set)this.myLineMapping.computeIfAbsent(key, (k) -> {
                    return new TreeSet();
                });
                existing.add(lineMapping[i]);
            }
        }

    }
}
