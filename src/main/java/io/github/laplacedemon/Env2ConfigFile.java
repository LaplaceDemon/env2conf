package io.github.laplacedemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class Env2ConfigFile {

    private final static String key = "\\$\\{.*?\\}";
    private final static Pattern pattern = Pattern.compile(key);

    public static void main(String[] args) throws IOException {
        int startIndex = 0;
        if (args.length == 0) {
            System.err.println("Please give the path parameter.");
            return;
        }

        if (args[0].equals("--version")) {
            InputStream in = Env2ConfigFile.class.getClassLoader()
                    .getResourceAsStream("META-INF/maven/io.github.laplacedemon/env2conf/pom.properties");
            Properties prop = new Properties();
            prop.load(in);
            System.out.println(prop.get("artifactId") + "-" + prop.get("version"));
            return;
        }

        if (args[0].equals("--print-conf")) {
            startIndex++;
            {
                Map<String, String> envMap = System.getenv();
                Set<Entry<String, String>> entrySet = envMap.entrySet();
                System.out.println("[System environment variable]:");
                for (Entry<String, String> entry : entrySet) {
                    System.out.println(entry.getKey() + "=" + entry.getValue());
                }
            }
            System.out.println();
            {
                Properties envMap = System.getProperties();
                Set<Entry<Object, Object>> entrySet = envMap.entrySet();
                System.out.println("[Program properties variable]:");
                for (Entry<Object, Object> entry : entrySet) {
                    System.out.println(entry.getKey() + "=" + entry.getValue());
                }
            }
            System.out.println();
            return;
        }

        if (args[0].equals("--print-env")) {
            startIndex++;
            Map<String, String> envMap = System.getenv();
            Set<Entry<String, String>> entrySet = envMap.entrySet();
            System.out.println("System environment variable:");
            for (Entry<String, String> entry : entrySet) {
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }
            System.out.println();
            return;
        }

        if (args[0].equals("--print-properties")) {
            startIndex++;
            Properties envMap = System.getProperties();
            Set<Entry<Object, Object>> entrySet = envMap.entrySet();
            System.out.println("Program properties variable:");
            for (Entry<Object, Object> entry : entrySet) {
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }
            System.out.println();
            return;
        }

//        StringBuilder dirPathStringBuilder = new StringBuilder();
//        for (int i = startIndex; i < args.length; i++) {
//            dirPathStringBuilder.append(args[i]);
//        }
//        
//        String dirPaths = dirPathStringBuilder.toString();
//        
        String dirPaths = args[startIndex];
        String[] splitDirPaths = dirPaths.split(";");

        for (String dirPath : splitDirPaths) {
            File file = new File(dirPath);
            if (!file.exists()) {
                System.err.println("The path '" + dirPath + "' does not exist");
                continue;
            }

            if (checkTemplateFileName(file)) {
                repalceTemplateFile(file);
            } else if (file.isDirectory()) {
                // dir
                File[] fs = file.listFiles();
                for (File f : fs) {
                    if (checkTemplateFileName(f)) {
                        repalceTemplateFile(f);
                    }
                }
            }
        }
    }

    public static boolean checkTemplateFileName(File file) throws IOException {
        return file.isFile() && file.getName().endsWith(".conftemp");
    }

    public static void repalceTemplateFile(File file) throws IOException {
        System.out.println("replace file " + file.getAbsolutePath());
        // file
        String fileName = file.getAbsolutePath();
        StringBuilder fileContentBuilder = new StringBuilder();
        String oldFileName = fileName.substring(0, fileName.length() - ".conftemp".length());
        File oldFile = new File(oldFileName);
        if (oldFile.exists()) {
            long currentTimeMillis = System.currentTimeMillis();
            File destFile = new File(oldFileName + ".bak." + currentTimeMillis);
            oldFile.renameTo(destFile);
        }

        Map<String, String> tempalteMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));) {
            while (true) {
                String lineStr = br.readLine();
                if (lineStr == null) {
                    break;
                }

                // match template
                Matcher matcher = pattern.matcher(lineStr);
                while (matcher.find()) {
                    int count = matcher.groupCount();
                    for (int i = 0; i <= count; i++) {
                        String templateKey = matcher.group(i);
                        String key = templateKey.substring(2, templateKey.length() - 1);
                        String envValue = System.getenv(key);
                        if (envValue == null) {
                            String propertyValue = System.getProperty(key);
                            if (propertyValue != null) {
                                tempalteMap.put(templateKey, propertyValue);
                            }
                        } else if (envValue != null) {
                            tempalteMap.put(templateKey, envValue);
                        }
                    }
                }
                
                fileContentBuilder.append(lineStr);
                fileContentBuilder.append('\n');
            }
            
//            System.out.println("tempalteMap:" + tempalteMap);
            String fileContent = fileContentBuilder.toString();
            if (!fileContent.isEmpty()) {
                // 替换
                Set<Entry<String, String>> entrySet = tempalteMap.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    fileContent = StringUtils.replace(fileContent, entry.getKey(), entry.getValue());
                }
                
                // 写新文件
                File newFile = new File(oldFileName);
                if (newFile.createNewFile()) {
                    try (FileOutputStream fos = new FileOutputStream(newFile);) {
                        fos.write(fileContent.getBytes("UTF-8"));
                    }
                } else {
                    System.err.println("Failed to create file '" + newFile.getAbsolutePath() + "'");
                }
            }
        }
        
        System.out.println();
    }

}
