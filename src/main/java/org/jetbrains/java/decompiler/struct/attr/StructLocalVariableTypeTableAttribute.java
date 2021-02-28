package org.jetbrains.java.decompiler.struct.attr;

import org.jetbrains.java.decompiler.struct.consts.ConstantPool;
import org.jetbrains.java.decompiler.util.DataInputFullStream;

import java.io.IOException;

public class StructLocalVariableTypeTableAttribute extends StructGeneralAttribute {
    private final StructLocalVariableTableAttribute backingAttribute = new StructLocalVariableTableAttribute();

    public void initContent(DataInputFullStream data, ConstantPool pool) throws IOException {
        this.backingAttribute.initContent(data, pool);
    }

    public void add(StructLocalVariableTypeTableAttribute attr) {
        this.backingAttribute.add(attr.backingAttribute);
    }

    public String getSignature(int index, int visibleOffset) {
        return this.backingAttribute.getDescriptor(index, visibleOffset);
    }
}
