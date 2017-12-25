//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.util;

import com.newrelic.agent.android.Agent;
import com.newrelic.agent.compile.visitor.ClassAnnotationVisitor;
import com.newrelic.agent.compile.visitor.MethodAnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class Annotations {
    private Annotations() {
    }

    public static Collection<ClassAnnotation> getClassAnnotations(Class annotationClass, String packageSearchPath, Set<URL> classpathURLs) {
        String annotationDescription = 'L' + annotationClass.getName().replace('.', '/') + ';';
        System.out.println("getClassAnnotations: annotationClass[" + annotationClass.getSimpleName() + "] packageSearchPath[" + packageSearchPath + "]  classpathURLs[" + classpathURLs + "])");
        Map<String, URL> fileNames = getMatchingFiles(packageSearchPath, classpathURLs);
        Collection<ClassAnnotation> list = new ArrayList();
        Iterator var6 = fileNames.entrySet().iterator();

        while(var6.hasNext()) {
            Entry<String, URL> entry = (Entry)var6.next();
            String fileName = (String)entry.getKey();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try {
                Streams.copy(Annotations.class.getResourceAsStream('/' + fileName), out, true);
                ClassReader cr = new ClassReader(out.toByteArray());
                Collection<ClassAnnotation> annotations = ClassAnnotationVisitor.getAnnotations(cr, annotationDescription);
                list.addAll(annotations);
            } catch (IOException var12) {
                var12.printStackTrace();
            }
        }

        return list;
    }

    public static Collection<MethodAnnotation> getMethodAnnotations(Class annotationClass, String packageSearchPath, Set<URL> classpathURLs) {
        String annotationDescription = Type.getType(annotationClass).getDescriptor();
        System.out.println("getClassAnnotations: annotationClass[" + annotationClass.getSimpleName() + "] packageSearchPath[" + packageSearchPath + "]  classpathURLs[" + classpathURLs + "])");
        Map<String, URL> fileNames = getMatchingFiles(packageSearchPath, classpathURLs);
        Collection<MethodAnnotation> list = new ArrayList();
        Iterator var6 = fileNames.entrySet().iterator();

        while(var6.hasNext()) {
            Entry<String, URL> entry = (Entry)var6.next();
            String fileName = (String)entry.getKey();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try {
                Streams.copy(Annotations.class.getResourceAsStream('/' + fileName), out, true);
                ClassReader cr = new ClassReader(out.toByteArray());
                Collection<MethodAnnotation> annotations = MethodAnnotationVisitor.getAnnotations(cr, annotationDescription);
                list.addAll(annotations);
            } catch (IOException var12) {
                var12.printStackTrace();
            }
        }

        return list;
    }

    private static Map<String, URL> getMatchingFiles(String packageSearchPath, Set<URL> classpathURLs) {
        if(!packageSearchPath.endsWith("/")) {
            packageSearchPath = packageSearchPath + "/";
        }

        Pattern pattern = Pattern.compile("(.*).class");
        Map<String, URL> fileNames = getMatchingFileNames(pattern, classpathURLs);
        String[] var4 = (String[])fileNames.keySet().toArray(new String[0]);
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String file = var4[var6];
            if(!file.startsWith(packageSearchPath)) {
                fileNames.remove(file);
            }
        }

        return fileNames;
    }

    static Map<String, URL> getMatchingFileNames(Pattern pattern, Collection<URL> urls) {
        Map<String, URL> names = new HashMap();
        Iterator var3 = urls.iterator();

        while(var3.hasNext()) {
            URL url = (URL)var3.next();
            url = fixUrl(url);

            File file;
            try {
                file = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
            } catch (UnsupportedEncodingException var19) {
                var19.printStackTrace();
                System.exit(1);
                return names;
            }

            if(file.isDirectory()) {
                List<File> files = Annotations.PatternFileMatcher.getMatchingFiles(file, pattern);
                Iterator var23 = files.iterator();

                while(var23.hasNext()) {
                    File f = (File)var23.next();
                    String path = f.getAbsolutePath();
                    path = path.substring(file.getAbsolutePath().length() + 1);
                    names.put(path, url);
                }
            } else if(file.isFile()) {
                JarFile jarFile = null;

                try {
                    jarFile = new JarFile(file);
                    Enumeration entries = jarFile.entries();

                    while(entries.hasMoreElements()) {
                        JarEntry jarEntry = (JarEntry)entries.nextElement();
                        if(pattern.matcher(jarEntry.getName()).matches()) {
                            names.put(jarEntry.getName(), url);
                        }
                    }
                } catch (IOException var20) {
                    var20.printStackTrace();
                    System.exit(1);
                } finally {
                    if(jarFile != null) {
                        try {
                            jarFile.close();
                        } catch (IOException var18) {
                            ;
                        }
                    }

                }
            }
        }

        return names;
    }

    private static URL fixUrl(URL url) {
        String protocol = url.getProtocol();
        if("jar".equals(protocol)) {
            try {
                String urlString = url.toString().substring(4);
                int index = urlString.indexOf("!/");
                if(index > 0) {
                    urlString = urlString.substring(0, index);
                }

                url = new URL(urlString);
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        }

        return url;
    }

    static URL[] getClasspathURLs() {
        ClassLoader classLoader = Agent.class.getClassLoader();
        return classLoader instanceof URLClassLoader?((URLClassLoader)classLoader).getURLs():new URL[0];
    }

    static class PatternFileMatcher {
        private final FileFilter filter;
        private final List<File> files = new ArrayList();

        public static List<File> getMatchingFiles(File directory, Pattern pattern) {
            Annotations.PatternFileMatcher matcher = new Annotations.PatternFileMatcher(pattern);
            directory.listFiles(matcher.filter);
            return matcher.files;
        }

        private PatternFileMatcher(final Pattern pattern) {
            this.filter = new FileFilter() {
                public boolean accept(File f) {
                    if(f.isDirectory()) {
                        f.listFiles(this);
                    }

                    boolean match = pattern.matcher(f.getAbsolutePath()).matches();
                    if(match) {
                        PatternFileMatcher.this.files.add(f);
                    }

                    return match;
                }
            };
        }
    }
}
