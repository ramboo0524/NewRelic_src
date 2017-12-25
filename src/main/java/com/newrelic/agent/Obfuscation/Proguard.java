//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.Obfuscation;

import com.newrelic.agent.compile.Log;
import com.newrelic.agent.compile.RewriterAgent;
import com.newrelic.agent.compile.visitor.NewRelicClassVisitor;
import com.google.common.io.BaseEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.input.ReversedLinesFileReader;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class Proguard {
    public static final String NR_PROPERTIES = "newrelic.properties";
    public static final String MAPPING_FILENAME = "mapping.txt";
    private static final String PROP_NR_APP_TOKEN = "com.newrelic.application_token";
    private static final String PROP_UPLOADING_ENABLED = "com.newrelic.enable_proguard_upload";
    private static final String PROP_MAPPING_API_HOST = "com.newrelic.mapping_upload_host";
    private static final String DEFAULT_MAPPING_API_HOST = "mobile-symbol-upload.newrelic.com";
    private static final String MAPPING_API_PATH = "/symbol";
    private static final String LICENSE_KEY_HEADER = "X-APP-LICENSE-KEY";
    private static final String NR_MAP_PREFIX = "# NR_BUILD_ID -> ";
    private final Log log;
    private final String buildId;
    private String projectRoot;
    private String licenseKey;
    private boolean uploadingEnabled;
    private String mappingApiHost;
    private static Map<String, String> agentOptions = Collections.emptyMap();
    private static String newLn = System.getProperty("line.separator");

    public Proguard(Log log) {
        this(log, RewriterAgent.getAgentOptions(), NewRelicClassVisitor.getBuildId());
    }

    public Proguard(Log log, Map<String, String> agentOptions, String buildId) {
        this.licenseKey = null;
        this.uploadingEnabled = true;
        this.mappingApiHost = null;
        this.log = log;
        Proguard.agentOptions = agentOptions;
        this.buildId = buildId;
    }

    public void findAndSendMapFile() {
        if(this.getProjectRoot() != null) {
            if(!this.fetchConfiguration()) {
                return;
            }

            File projectRoot = new File(this.getProjectRoot());
            IOFileFilter fileFilter = FileFilterUtils.nameFileFilter("mapping.txt");
            Collection files = FileUtils.listFiles(projectRoot, fileFilter, TrueFileFilter.INSTANCE);
            if(files.isEmpty()) {
                this.log.error("While evidence of ProGuard/Dexguard was detected, New Relic failed to find your mapping.txt file.");
                this.log.error("To de-obfuscate your builds, you\'ll need to upload your mapping.txt manually.");
                return;
            }

            Iterator var4 = files.iterator();

            while(var4.hasNext()) {
                File file = (File)var4.next();
                String mappingString = "";
                this.log.debug("Found mapping.txt[" + file.getPath() + "]");

                try {
                    ReversedLinesFileReader e = new ReversedLinesFileReader(file);

                    String lastLine;
                    try {
                        lastLine = e.readLine();
                    } finally {
                        e.close();
                    }

                    if(!lastLine.startsWith("# NR_BUILD_ID -> ")) {
                        FileWriter fileWriter = new FileWriter(file, true);
                        fileWriter.write("# NR_BUILD_ID -> " + this.buildId + newLn);
                        fileWriter.close();
                        mappingString = mappingString + FileUtils.readFileToString(file);
                        if(this.uploadingEnabled && mappingString.length() > 0) {
                            this.sendMapping(mappingString);
                        }
                    }
                } catch (FileNotFoundException var14) {
                    this.log.error("Unable to open your mapping.txt file: " + var14.getLocalizedMessage());
                    this.log.error("To de-obfuscate your builds, you\'ll need to upload your mapping.txt manually.");
                } catch (IOException var15) {
                    this.log.error("Unable to open your mapping.txt file: " + var15.getLocalizedMessage());
                    this.log.error("To de-obfuscate your builds, you\'ll need to upload your mapping.txt manually.");
                }
            }
        }

    }

    private String getProjectRoot() {
        if(this.projectRoot == null) {
            String encodedProjectRoot = agentOptions.get("projectRoot");
            if(encodedProjectRoot == null) {
                this.log.info("Unable to determine project root, falling back to CWD.");
                this.projectRoot = System.getProperty("user.dir");
            } else {
                this.projectRoot = new String(BaseEncoding.base64().decode(encodedProjectRoot));
            }

            this.log.debug("Project root[" + this.projectRoot + "]");
        }

        return this.projectRoot;
    }

    private boolean fetchConfiguration() {
        try {
            BufferedReader e = new BufferedReader(new FileReader(this.getProjectRoot() + File.separator + "newrelic.properties"));
            Properties newRelicProps = new Properties();
            newRelicProps.load(e);
            this.licenseKey = newRelicProps.getProperty("com.newrelic.application_token");
            this.uploadingEnabled = newRelicProps.getProperty("com.newrelic.enable_proguard_upload", "true").equals("true");
            this.mappingApiHost = newRelicProps.getProperty("com.newrelic.mapping_upload_host");
            if(this.licenseKey == null) {
                this.log.error("Unable to find a value for com.newrelic.application_token in your newrelic.properties");
                this.log.error("To de-obfuscate your builds, you\'ll need to upload your mapping.txt manually.");
                return false;
            } else {
                e.close();
                return true;
            }
        } catch (FileNotFoundException var3) {
            this.log.error("Unable to find your newrelic.properties in the project root (" + this.getProjectRoot() + "): " + var3.getLocalizedMessage());
            this.log.error("To de-obfuscate your builds, you\'ll need to upload your mapping.txt manually.");
            return false;
        } catch (IOException var4) {
            this.log.error("Unable to read your newrelic.properties in the project root (" + this.getProjectRoot() + "): " + var4.getLocalizedMessage());
            this.log.error("To de-obfuscate your builds, you\'ll need to upload your mapping.txt manually.");
            return false;
        }
    }

    private void sendMapping(String mapping) {
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("proguard=" + URLEncoder.encode(mapping));
        requestBody.append("&buildId=" + this.buildId);

        try {
            String e = "mobile-symbol-upload.newrelic.com";
            if(this.mappingApiHost != null) {
                e = this.mappingApiHost;
            }

            URL url = new URL("https://" + e + "/symbol");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("X-APP-LICENSE-KEY", this.licenseKey);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", Integer.toString(requestBody.length()));
            DataOutputStream request = new DataOutputStream(connection.getOutputStream());
            request.writeBytes(requestBody.toString());
            request.close();
            int responseCode = connection.getResponseCode();
            InputStream inputStream;
            String response;
            if(responseCode == 400) {
                inputStream = connection.getErrorStream();
                response = convertStreamToString(inputStream);
                this.log.error("Unable to send your ProGuard/Dexguard mapping.txt to New Relic as the params are incorrect: " + response);
                this.log.error("To de-obfuscate your builds, you\'ll need to upload your mapping.txt manually.");
            } else if(responseCode > 400) {
                inputStream = connection.getErrorStream();
                response = convertStreamToString(inputStream);
                this.log.error("Unable to send your ProGuard/DexGuard mapping.txt to New Relic - received status " + responseCode + ": " + response);
                this.log.error("To de-obfuscate your builds, you\'ll need to upload your mapping.txt manually.");
            } else {
                this.log.info("Successfully sent mapping.txt to New Relic.");
            }

            connection.disconnect();
        } catch (IOException var10) {
            this.log.error("Encountered an error while uploading your ProGuard/Dexguard mapping to New Relic", var10);
            this.log.error("To de-obfuscate your builds, you\'ll need to upload your mapping.txt manually.");
        }

    }

    private static String convertStreamToString(InputStream is) {
        StringBuilder sb = new StringBuilder();
        if(is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;

            try {
                while((line = reader.readLine()) != null) {
                    sb.append(line + newLn);
                }
            } catch (IOException var13) {
                var13.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException var12) {
                    var12.printStackTrace();
                }

            }
        }

        return sb.toString();
    }
}
