package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.main.ClassesProcessor.ClassNode;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger.Severity;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SwitchStatement;

import java.util.*;

public class SwitchHelper {
    public static void simplify(SwitchStatement switchStatement) {
        SwitchExprent switchExprent = (SwitchExprent)switchStatement.getHeadexprent();
        Exprent value = switchExprent.getValue();
        if (isEnumArray(value)) {
            List caseValues = switchStatement.getCaseValues();
            Map mapping = new HashMap(caseValues.size());
            ArrayExprent array = (ArrayExprent)value;
            FieldExprent arrayField = (FieldExprent)array.getArray();
            ClassNode classNode = (ClassNode)DecompilerContext.getClassProcessor().getMapRootClasses().get(arrayField.getClassname());
            if (classNode != null) {
                MethodWrapper wrapper = classNode.getWrapper().getMethodWrapper("<clinit>", "()V");
                if (wrapper != null && wrapper.root != null) {
                    wrapper.getOrBuildGraph().iterateExprents((exprentx) -> {
                        if (exprentx instanceof AssignmentExprent) {
                            AssignmentExprent assignment = (AssignmentExprent)exprentx;
                            Exprent left = assignment.getLeft();
                            if (left.type == 1 && ((ArrayExprent)left).getArray().equals(arrayField)) {
                                mapping.put(assignment.getRight(), ((InvocationExprent)((ArrayExprent)left).getIndex()).getInstance());
                            }
                        }

                        return 0;
                    });
                }
            }

            List realCaseValues = new ArrayList(caseValues.size());
            Iterator var9 = caseValues.iterator();

            while(var9.hasNext()) {
                List caseValue = (List)var9.next();
                List values = new ArrayList(caseValue.size());
                realCaseValues.add(values);
                Iterator var12 = caseValue.iterator();

                while(var12.hasNext()) {
                    Exprent exprent = (Exprent)var12.next();
                    if (exprent == null) {
                        values.add((Object)null);
                    } else {
                        Exprent realConst = (Exprent)mapping.get(exprent);
                        if (realConst == null) {
                            DecompilerContext.getLogger().writeMessage("Unable to simplify switch on enum: " + exprent + " not found, available: " + mapping, Severity.ERROR);
                            return;
                        }

                        values.add(realConst.copy());
                    }
                }
            }

            caseValues.clear();
            caseValues.addAll(realCaseValues);
            switchExprent.replaceExprent(value, ((InvocationExprent)array.getIndex()).getInstance().copy());
        }

    }

    private static boolean isEnumArray(Exprent exprent) {
        if (!(exprent instanceof ArrayExprent)) {
            return false;
        } else {
            Exprent field = ((ArrayExprent)exprent).getArray();
            Exprent index = ((ArrayExprent)exprent).getIndex();
            return field instanceof FieldExprent && (((FieldExprent)field).getName().startsWith("$SwitchMap") || index instanceof InvocationExprent && ((InvocationExprent)index).getName().equals("ordinal"));
        }
    }
}
