//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Streams {
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private Streams() {
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, DEFAULT_BUFFER_SIZE, false);
    }

    public static int copy(InputStream input, OutputStream output, boolean closeStreams) throws IOException {
        return copy(input, output, DEFAULT_BUFFER_SIZE, closeStreams);
    }

    public static int copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        return copy(input, output, bufferSize, false);
    }

    public static int copy(InputStream input, OutputStream output, int bufferSize, boolean closeStreams) throws IOException {
        try {
            byte[] buffer = new byte[bufferSize];
            int count = 0;

            int n;
            for(boolean var6 = false; -1 != (n = input.read(buffer)); count += n) {
                output.write(buffer, 0, n);
            }

            int var7 = count;
            return var7;
        } finally {
            if(closeStreams) {
                input.close();
                output.close();
            }

        }
    }

    public static byte[] slurpBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] var2;
        try {
            copy(in, out);
            out.flush();
            var2 = out.toByteArray();
        } finally {
            out.close();
        }

        return var2;
    }

    public static String slurp(InputStream in, String encoding) throws IOException {
        byte[] bytes = slurpBytes(in);
        return new String(bytes, encoding);
    }

    public static void copyBytesToFile(File file, byte[] newBytes) throws IOException {
        FileOutputStream oStream = new FileOutputStream(file);

        try {
            copy(new ByteArrayInputStream(newBytes), oStream, true);
        } finally {
            oStream.close();
        }

    }
}
