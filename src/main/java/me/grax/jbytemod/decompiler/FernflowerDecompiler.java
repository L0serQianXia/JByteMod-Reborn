package me.grax.jbytemod.decompiler;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.ui.DecompilerPanel;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.struct.*;
import org.jetbrains.java.decompiler.struct.consts.ConstantPool;
import org.jetbrains.java.decompiler.struct.consts.PrimitiveConstant;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;
import org.jetbrains.java.decompiler.util.DataInputFullStream;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.VBStyleCollection;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.jar.Manifest;

public class FernflowerDecompiler extends Decompiler implements IBytecodeProvider, IResultSaver {

    public static final HashMap<String, Boolean> options = new HashMap<>();

    static {
        options.put("rbr", true);
        options.put("rsy", false);
        options.put("din", true);
        options.put("dc4", true);
        options.put("das", true);
        options.put("hes", true);
        options.put("hdc", true);
        options.put("dgs", false);
        options.put("ner", true);
        options.put("den", true);
        options.put("rgn", true);
        options.put("lit", false);
        options.put("asc", true);
        options.put("bto", true);
        options.put("nns", false);
        options.put("uto", true);
        options.put("udv", true);
        options.put("rer", true);
        options.put("fdi", true);
        options.put("ren", false);
        options.put("inn", true);
        options.put("lac", false);
        options.put("nls", false);
    }

    private byte[] bytes;
    private String returned;

    public FernflowerDecompiler(JByteMod jbm, DecompilerPanel dp) {
        super(jbm, dp);
    }

    public String decompile(byte[] b, MethodNode mn) {
        try {
            this.bytes = b;
            HashMap<String, Object> map = new HashMap<>();
            for (String key : options.keySet()) {
                map.put(key, JByteMod.ops.get("ff_" + key).getBoolean() ? "1" : "0");
            }
            Fernflower f = new Fernflower(this, this, map, new PrintStreamLogger(JByteMod.LOGGER));
            StructContext sc = f.structContext;
            DataInputFullStream in = new DataInputFullStream(b);
            StructClass cl = StructClass.create(in, true, sc.loader);

            if(mn != null){
                Field methodField = cl.getClass().getDeclaredField("methods");
                methodField.setAccessible(true);

                VBStyleCollection<StructMethod, String> methods = (VBStyleCollection<StructMethod, String>) methodField.get(cl);
                VBStyleCollection<StructMethod, String> result = new VBStyleCollection<>(1);

                for (StructMethod method : methods) {
                    if(method.getName().equals(mn.name) && method.getDescriptor().equals(mn.desc)){
                        result.add(method);
                    }
                }
                methodField.set(cl, result);
            }

            sc.getClasses().put(cn.name, cl);
            //instead of loading a file use custom bridge, created a few getters
            String fakePath = new File("none.class").getAbsolutePath();
            ContextUnit unit = new ContextUnit(ContextUnit.TYPE_FOLDER, null, fakePath, true, sc.saver, sc.decompiledData);
            sc.units.put(fakePath, unit);
            unit.addClass(cl, "none.class");
            sc.loader.addClassLink(cn.name, new LazyLoader.Link(fakePath, null));

            f.decompileContext();

            if(mn != null){
                returned = "// Decompiled Method:" + mn.name + "\r\n" + returned;
            }
            return returned;
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            return sw.toString();
        }
    }

    @Override
    public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
        return bytes;
    }

    //we can ignore most of those methods because we do not want to save the output as a file
    @Override
    public void saveFolder(String path) {
    }

    @Override
    public void copyFile(String source, String path, String entryName) {
    }

    @Override
    public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
        this.returned = content;
    }

    @Override
    public void createArchive(String path, String archiveName, Manifest manifest) {
    }

    @Override
    public void saveDirEntry(String path, String archiveName, String entryName) {
    }

    @Override
    public void copyEntry(String source, String path, String archiveName, String entry) {
    }

    @Override
    public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
    }

    @Override
    public void closeArchive(String path, String archiveName) {
    }
}
