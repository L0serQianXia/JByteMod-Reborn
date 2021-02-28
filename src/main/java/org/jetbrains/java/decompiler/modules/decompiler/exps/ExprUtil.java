package org.jetbrains.java.decompiler.modules.decompiler.exps;

import org.jetbrains.java.decompiler.main.ClassesProcessor.ClassNode;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExprUtil {
    public static List getSyntheticParametersMask(String className, String descriptor, int parameters) {
        ClassNode node = (ClassNode)DecompilerContext.getClassProcessor().getMapRootClasses().get(className);
        return node != null ? getSyntheticParametersMask(node, descriptor, parameters) : null;
    }

    public static List getSyntheticParametersMask(ClassNode node, String descriptor, int parameters) {
        List mask = null;
        ClassWrapper wrapper = node.getWrapper();
        if (wrapper != null) {
            MethodWrapper methodWrapper = wrapper.getMethodWrapper("<init>", descriptor);
            if (methodWrapper == null) {
                if (DecompilerContext.getOption("iib")) {
                    return null;
                }

                throw new RuntimeException("Constructor " + node.classStruct.qualifiedName + "." + "<init>" + descriptor + " not found");
            }

            mask = methodWrapper.synthParameters;
        } else if (parameters > 0 && node.type == 1 && (node.access & 8) == 0) {
            mask = new ArrayList(Collections.nCopies(parameters, (Object)null));
            ((List)mask).set(0, new VarVersionPair(-1, 0));
        }

        return (List)mask;
    }
}
