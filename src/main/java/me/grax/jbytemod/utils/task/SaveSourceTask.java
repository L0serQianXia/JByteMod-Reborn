package me.grax.jbytemod.utils.task;

import me.grax.jbytemod.JByteMod;
import me.grax.jbytemod.JarArchive;
import me.grax.jbytemod.decompiler.*;
import me.grax.jbytemod.ui.DecompilerPanel;
import me.grax.jbytemod.ui.DecompilerTab;
import me.grax.jbytemod.ui.PageEndPanel;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SaveSourceTask extends SwingWorker<Void, Integer> {

    private File output;
    private PageEndPanel jpb;
    private JarArchive file;
    private String decompiler;

    public SaveSourceTask(JByteMod jbm, File output, JarArchive file, Object choice) {
        this.output = output;
        this.file = file;
        this.jpb = jbm.getPP();
        this.decompiler = ((Decompilers) choice).getName();
    }

    @Override
    protected Void doInBackground() throws Exception {
        synchronized (this.file) {
            try {
                Decompiler decompiler = null;
                switch (this.decompiler){
                    case "Fernflower":
                        decompiler = new FernflowerDecompiler(JByteMod.instance, new DecompilerPanel());
                        break;
                    case "CFR":
                        decompiler = new CFRDecompiler(JByteMod.instance, new DecompilerPanel());
                        break;
                    case "Procyon":
                        decompiler = new ProcyonDecompiler(JByteMod.instance, new DecompilerPanel());
                        break;
                    case "Krakatau":
                        decompiler = new KrakatauDecompiler(JByteMod.instance, new DecompilerPanel());
                        break;
                    case "Koffee":
                        decompiler = new KoffeeDecompiler(JByteMod.instance, new DecompilerPanel());
                        break;
                    default:
                        JByteMod.LOGGER.err("WTF?! No decompiler called " + this.decompiler);
                        break;
                }

                Map<String, ClassNode> classes = this.file.getClasses();
                Map<String, byte[]> outputBytes = this.file.getOutput();
                int flags = JByteMod.ops.get("compute_maxs").getBoolean() ? 1 : 0;
                JByteMod.LOGGER.log("Writing..");
                if (this.file.isSingleEntry()) {
                    ClassNode node = classes.values().iterator().next();
                    ClassWriter writer = new ClassWriter(flags);
                    node.accept(writer);
                    decompiler.setNode(node, null);
                    String result = decompiler.decompile(writer.toByteArray(), null);
                    publish(50);
                    JByteMod.LOGGER.log("Saving..");
                    Files.write(this.output.toPath(), result.getBytes(StandardCharsets.UTF_8));
                    publish(100);
                    JByteMod.LOGGER.log("Saving successful!");
                    return null;
                }

                publish(0);
                double size = classes.keySet().size();
                double i = 0;
                for (String s : classes.keySet()) {
                    try{
                        ClassNode node = classes.get(s);
                        ClassWriter writer = new ClassWriter(flags);
                        node.accept(writer);
                        decompiler.setNode(node, null);
                        String result = decompiler.decompile(writer.toByteArray(), null);
                        outputBytes.put(s + ".java", result.getBytes(StandardCharsets.UTF_8));
                        publish((int) ((i++ / size) * 50d));
                    }catch(StringIndexOutOfBoundsException exception) {
                        JByteMod.LOGGER.println("Failed to save " + classes.get(s).name);
                    }
                }
                publish(50);
                JByteMod.LOGGER.log("Saving..");
                this.saveAsJarNew(outputBytes, output.getAbsolutePath());
                JByteMod.LOGGER.log("Saving successful!");
            } catch (Exception e) {
                e.printStackTrace();
                JByteMod.LOGGER.log("Saving failed!");
            }
            publish(100);
            return null;
        }
    }

    public void saveAsJarNew(Map<String, byte[]> outBytes, String fileName) {
        try {
            ZipOutputStream out = new ZipOutputStream(new java.io.FileOutputStream(fileName));
            out.setEncoding("UTF-8");
            for (String entry : outBytes.keySet()) {
                out.putNextEntry(new ZipEntry(entry));
                if (!entry.endsWith("/") || !entry.endsWith("\\"))
                    out.write(outBytes.get(entry));
                out.closeEntry();
            }
            if (out != null) {
                out.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void process(List<Integer> chunks) {
        int i = chunks.get(chunks.size() - 1);
        jpb.setValue(i);
        super.process(chunks);
    }

}