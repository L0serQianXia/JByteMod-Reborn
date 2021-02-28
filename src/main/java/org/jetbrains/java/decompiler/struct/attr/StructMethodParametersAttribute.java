package org.jetbrains.java.decompiler.struct.attr;

import org.jetbrains.java.decompiler.struct.consts.ConstantPool;
import org.jetbrains.java.decompiler.util.DataInputFullStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StructMethodParametersAttribute extends StructGeneralAttribute {
    private List myEntries;

    public void initContent(DataInputFullStream data, ConstantPool pool) throws IOException {
        int len = data.readUnsignedByte();
        Object entries;
        if (len > 0) {
            entries = new ArrayList(len);

            for(int i = 0; i < len; ++i) {
                int nameIndex = data.readUnsignedShort();
                String name = nameIndex != 0 ? pool.getPrimitiveConstant(nameIndex).getString() : null;
                int access_flags = data.readUnsignedShort();
                ((List)entries).add(new Entry(name, access_flags));
            }
        } else {
            entries = Collections.emptyList();
        }

        this.myEntries = Collections.unmodifiableList((List)entries);
    }

    public List getEntries() {
        return this.myEntries;
    }

    public class Entry {
        public final String myName;
        public final int myAccessFlags;

        public Entry(String name, int accessFlags) {
            this.myName = name;
            this.myAccessFlags = accessFlags;
        }
    }

}
