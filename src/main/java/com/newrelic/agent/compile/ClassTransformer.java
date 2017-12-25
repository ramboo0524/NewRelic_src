//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

import com.newrelic.agent.util.Streams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.regex.Pattern;

public final class ClassTransformer {
    private final Log log;
    private final List<File> classes;
    private File inputFile;
    private File outputFile;
    private InvocationDispatcher invocationDispatcher;
    private ClassData classData;
    private boolean noopTransform;
    private ClassTransformer.WriteMode writeMode;

    public ClassTransformer() {
        this.noopTransform = false;
        this.writeMode = ClassTransformer.WriteMode.modified;
        String props = System.getProperty("NewRelic.AgentArgs");
        Map<String, String> agentOptions = RewriterAgent.parseAgentArgs(props);
        this.log = new SystemErrLog(agentOptions);
        this.classes = new ArrayList();
        this.inputFile = new File(".");
        this.outputFile = new File(".");
        this.classData = null;
        this.noopTransform = false;
        this.writeMode = ClassTransformer.WriteMode.modified;

        try {
            this.invocationDispatcher = new InvocationDispatcher(this.log);
        } catch (Exception var4) {
            this.log.error("[ClassTransformer] " + var4);
        }

    }

    public ClassTransformer(File classPath, File outputDir) {
        this();
        this.classes.add(classPath);
        this.inputFile = classPath;
        this.outputFile = outputDir;
        if(classPath.isDirectory()) {
            this.inputFile = classPath;
        }

    }

    public ClassTransformer(JarFile jarFile, File outputJar) {
        this();
        File jar = new File(jarFile.getName());
        this.inputFile = jar.getParentFile();
        this.outputFile = outputJar;
    }

    protected void doTransform() {
        long tStart = System.currentTimeMillis();
        this.log.info("[ClassTransformer] Version: " + RewriterAgent.getVersion());
        Iterator var3 = this.classes.iterator();

        while(var3.hasNext()) {
            File classFile = (File)var3.next();
            this.inputFile = ClassTransformer.FileUtils.isClass(classFile)?classFile.getParentFile():classFile;
            this.log.debug("[ClassTransformer] Transforming classpath[" + classFile.getAbsolutePath() + "]");
            this.log.debug("[ClassTransformer] InputFile[" + this.inputFile.getAbsolutePath() + "]");
            this.log.debug("[ClassTransformer] OutputFile[" + this.outputFile.getAbsolutePath() + "]");
            this.transformClass(classFile);
        }

        this.log.info(MessageFormat.format("[ClassTransformer] doTransform finished in {0} sec.", new Object[]{Float.valueOf((float)(System.currentTimeMillis() - tStart) / 1000.0F)}));
    }

    public byte[] transformClassBytes(String destClassPath, byte[] bytes) {
        if(this.noopTransform) {
            return bytes;
        } else {
            if(ClassTransformer.FileUtils.isClass(destClassPath)) {
                try {
                    if(bytes != null) {
                        this.log.debug("[ClassTransformer] transformClassBytes: [" + destClassPath + "]");
                        this.classData = this.invocationDispatcher.visitClassBytes(bytes);
                        if(this.classData != null && this.classData.getMainClassBytes() != null && this.classData.isModified()) {
                            return this.classData.getMainClassBytes();
                        }
                    }
                } catch (Exception var4) {
                    this.log.error("[ClassTransformer] " + var4);
                }
            }

            return bytes;
        }
    }

    private ByteArrayInputStream processClassBytes(File file, InputStream inStrm) throws IOException {
        byte[] classBytes = Streams.slurpBytes(inStrm);
        byte[] rewrittenClassBytes = this.transformClassBytes(file.getPath(), classBytes);
        ByteArrayInputStream byteStream = new ByteArrayInputStream(classBytes);
        if(rewrittenClassBytes != null) {
            if(classBytes.length != rewrittenClassBytes.length && this.classData != null && this.classData.isModified()) {
                this.log.info("[ClassTransformer] Rewrote class[" + file.getPath() + "] bytes[" + classBytes.length + "] rewritten[" + rewrittenClassBytes.length + "]");
            }

            byteStream = new ByteArrayInputStream(rewrittenClassBytes);
        }

        return byteStream;
    }

    public boolean transformClass(File file) {
        boolean didProcessClass = false;

        try {
            if(ClassTransformer.FileUtils.isArchive(file)) {
                didProcessClass = this.transformArchive(file, true);
            } else if(file.isDirectory()) {
                didProcessClass = this.transformDirectory(file);
            } else if(ClassTransformer.FileUtils.isClass(file)) {
                String classpath = file.getAbsolutePath();
                if(classpath.startsWith(this.inputFile.getAbsolutePath())) {
                    classpath = classpath.substring(this.inputFile.getAbsolutePath().length() + 1);
                }

                ByteArrayInputStream byteStream = this.processClassBytes(new File(classpath), new FileInputStream(file));

                try {
                    File transformedClass = new File(this.outputFile, classpath);
                    didProcessClass = this.writeModifiedClassFile(byteStream, (File)transformedClass);
                } catch (Exception var10) {
                    this.log.error("[ClassTransformer] transformClass: " + var10);
                    didProcessClass = false;
                } finally {
                    byteStream.close();
                }
            } else {
                this.log.debug("[ClassTransformer] Class ignored: " + file.getName());
            }
        } catch (Exception var12) {
            this.log.error("[ClassTransformer] " + var12);
        }

        return didProcessClass;
    }

    public boolean transformDirectory(File directory) {
        boolean didProcessDirectory = false;
        if(directory.isDirectory()) {
            File[] var3 = directory.listFiles();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                File f = var3[var5];
                didProcessDirectory |= this.transformClass(f);
            }
        }

        return didProcessDirectory;
    }

    public boolean transformArchive(File archiveFile, boolean explodeJar) throws IOException {
        boolean didProcessArchive = false;
        if(this.isSupportJar(archiveFile)) {
            this.log.debug("[ClassTransformer] Skipping support jar [" + archiveFile.getPath() + "]");
            return false;
        } else {
            this.log.debug("[ClassTransformer] Transforming archive[" + archiveFile.getCanonicalPath() + "]");
            ByteArrayOutputStream byteStrm = new ByteArrayOutputStream();
            JarInputStream jarInStrm = new JarInputStream(new FileInputStream(archiveFile));
            JarOutputStream jarOutStream = new JarOutputStream(byteStrm);
            JarFile jarFile = new JarFile(archiveFile);

            try {
                JarEntry manifest = new JarEntry("META-INF/MANIFEST.MF");
                jarOutStream.putNextEntry(manifest);
                Manifest realManifest = jarFile.getManifest();
                if(realManifest != null) {
                    realManifest.getMainAttributes().put(new Name("Transformed-By"), "New Relic Android Agent");
                    realManifest.write(jarOutStream);
                }

                jarOutStream.flush();
                jarOutStream.closeEntry();

                for(JarEntry entry = jarInStrm.getNextJarEntry(); entry != null; entry = jarInStrm.getNextJarEntry()) {
                    String jarEntryPath = entry.getName();
                    if(!entry.isDirectory() && ClassTransformer.FileUtils.isClass(jarEntryPath)) {
                        JarEntry jarEntry = new JarEntry(jarEntryPath);
                        InputStream inputStrm = jarFile.getInputStream(entry);
                        File archiveClass = new File(this.outputFile, jarEntryPath);
                        jarEntry.setTime(entry.getTime());
                        jarOutStream.putNextEntry(jarEntry);
                        ByteArrayInputStream byteStream = this.processClassBytes(archiveClass, inputStrm);

                        try {
                            if(explodeJar) {
                                this.writeModifiedClassFile(byteStream, (File)archiveClass);
                            } else {
                                this.writeModifiedClassFile(byteStream, (OutputStream)jarOutStream);
                                didProcessArchive = true;
                            }
                        } catch (Exception var27) {
                            this.log.error("[ClassTransformer] transformArchive: " + var27);
                            didProcessArchive = false;
                        } finally {
                            byteStream.close();
                        }

                        jarOutStream.flush();
                        jarOutStream.closeEntry();
                    }
                }

                if(didProcessArchive) {
                    File rewrittenJar = new File(this.outputFile.getAbsolutePath());
                    if(archiveFile.getAbsolutePath() != rewrittenJar.getAbsolutePath()) {
                        this.log.debug("[ClassTransformer] Rewriting archive to [" + rewrittenJar.getAbsolutePath() + "]");
                        jarOutStream.close();
                        this.writeModifiedClassFile(new ByteArrayInputStream(byteStrm.toByteArray()), (File)rewrittenJar);
                    } else {
                        this.log.error("[ClassTransformer] Refusing to overwrite archive [" + rewrittenJar.getAbsolutePath() + "]");
                    }
                }
            } catch (Exception var29) {
                this.log.error("[ClassTransformer] transformArchive: " + var29);
            } finally {
                jarFile.close();
                jarInStrm.close();
                jarOutStream.close();
            }

            return didProcessArchive;
        }
    }

    private boolean isSupportJar(File archiveFile) {
        boolean matches = false;

        try {
            String canonicalPath = archiveFile.getCanonicalPath().toLowerCase();
            matches |= Pattern.matches("^.*\\/jre\\/lib\\/rt\\.jar$", canonicalPath);
        } catch (Exception var4) {
            ;
        }

        return matches;
    }

    public ClassTransformer asNoopTransform(boolean noopTransform) {
        this.noopTransform = noopTransform;
        return this;
    }

    public ClassTransformer addClasspath(File classpath) {
        this.classes.add(classpath);
        return this;
    }

    public ClassTransformer withWriteMode(ClassTransformer.WriteMode writeMode) {
        this.writeMode = writeMode;
        return this;
    }

    protected boolean writeModifiedClassFile(InputStream inStream, OutputStream outStrm) throws IOException {
        return (this.writeMode == ClassTransformer.WriteMode.always || this.writeMode == ClassTransformer.WriteMode.modified && this.classData != null && this.classData.isModified()) && inStream != null?0 < Streams.copy(inStream, outStrm):false;
    }

    protected boolean writeModifiedClassFile(InputStream inStream, File className) throws IOException {
        if((this.writeMode == ClassTransformer.WriteMode.always || this.writeMode == ClassTransformer.WriteMode.modified && this.classData != null && this.classData.isModified()) && inStream != null) {
            className.getParentFile().mkdirs();
            return this.writeModifiedClassFile(inStream, (OutputStream)(new FileOutputStream(className)));
        } else {
            return false;
        }
    }

    private static final class FileUtils {
        private FileUtils() {
        }

        public static boolean isArchive(String fileName) {
            String lowerPath = fileName.toLowerCase();
            return lowerPath.endsWith(".zip") || lowerPath.endsWith(".jar") || lowerPath.endsWith(".aar");
        }

        public static boolean isArchive(File f) {
            return isArchive(f.getAbsolutePath());
        }

        public static boolean isClass(String fileName) {
            String lowerPath = fileName.toLowerCase();
            return lowerPath.endsWith(".class");
        }

        public static boolean isClass(File f) {
            return isClass(f.getAbsolutePath());
        }
    }

    public static enum WriteMode {
        modified,
        always,
        never;

        private WriteMode() {
        }
    }
}
