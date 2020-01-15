package ru.maklas.azure;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.IntArray;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Nullable;
import ru.maklas.http.Header;
import ru.maklas.http.Http;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {

    public static Charset utf8 = Charset.forName("UTF-8");

    public static String ff(float f, int numbersAfterComma){
        return String.format(Locale.ENGLISH, "%.0"+ numbersAfterComma + "f", f);
    }
    public static String df(double d, int numbersAfterComma){
        return String.format(Locale.ENGLISH, "%.0"+ numbersAfterComma + "f", d);
    }

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    public static String format(LocalDateTime dt) {
        return formatter.format(dt);
    }

    public static String fileSizeToString(File file){
        return byteSizeToString(fileSize(file));
    }

    public static long fileSize(File file){
        if (!file.exists()) return 0;
        if (file.isFile()) return file.length();
        if (file.isDirectory()) {
            AtomicLong count = new AtomicLong();
            forEachFile(file, f -> {
                count.addAndGet(f.length());
            });
            return count.get();
        }
        return 0;
    }

    public static String byteSizeToString(long bytes){
        if (bytes < 1024L) return bytes + " bytes";
        if (bytes < 1024L * 1024L) return Utils.df(bytes / 1024d, 2) + " KB";
        if (bytes < 1024L * 1024L * 1024L) return Utils.df(bytes / (1024d * 1024d), 2) + " MB";
        if (bytes < 1024L * 1024L * 1024L * 1024L) return Utils.df(bytes / (1024d * 1024d * 1024d), 2) + " GB";
        if (bytes < 1024L * 1024L * 1024L * 1024L * 1024L) return Utils.df(bytes / (1024d * 1024d * 1024d * 1024d), 2) + " TB";
        return bytes + " bytes";
    }

    public static String timeToString(long ms){
        if (ms < 10_000) return ms + " ms";
        long sec = ms / 1000;
        if (sec < 60) return sec + " sec";
        if (sec < 60 * 60) return (sec / 60) + " min " + (sec % 60) + " sec";
        return (sec / 3600) + "hrs " + (sec / 60) + " min " + (sec % 60) + " sec";
    }

    public static final void initHttp(){
        Http.setDefaultCookieHandlerByJFX(false);
        Http.setSystemUserAgent(Header.UserAgent.def.value);
        Http.setAutoAddHostHeader(true);
        Http.fetchJavaHeaders(true);
    }

    public static File getFileOnDesktop(String fileName){
        File desktop = new File(System.getProperty("user.home") + "/Desktop");
        return new File(desktop, fileName);
    }

    public static File getFileFromResources(String fileName){
        return new File(Resources.getResource(fileName).getPath());
    }

    private static GsonBuilder prettyPrintGsonBuilder = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping();

    private static Gson gson = prettyPrintGsonBuilder.create();
    public static Gson gson(){
        return gson;
    }

    public static Gson newGson(){
        return prettyPrintGsonBuilder.create();
    }

    public static String prettyPrint(String json){
        return prettyPrint(json != null && json.length() > 0 ? JsonParser.parseString(json) : null);
    }
    public static String prettyPrint(JsonElement e){
        if (e == null) return "null";
        return gson().toJson(e);
    }


    public static File resource(String path){
        if (path.startsWith("/")){
            path = path.substring(1);
        }
        return new File(Resources.getResource(path).getFile());
    }

    /** Папка Resources в сурсе кода, а не в target **/
    public static File resourceSource(String path){
        if (path.startsWith("/")){
            path = path.substring(1);
        }
        return new File(".", "src/main/resources/" + path);
    }

    /** Файл в проекте **/
    public static File getProjectFile(String path){
        if (path.startsWith("/")){
            path = path.substring(1);
        }
        return new File(".", path);
    }

    public static String read(String resourcePath) throws IOException {
        return read(resource(resourcePath));
    }

    public static String read(File file) throws IOException {
        if (!file.exists()) {
            return "";
        }
        return FileUtils.readFileToString(file, Charsets.UTF_8);
    }

    public static String readString(File file, long offset, int length, Charset charset) throws IOException {
        return new String(readBytes(file, offset, length), charset);
    }

    public static byte[] readFirstBytes(File file, int length) throws IOException{
        if (!file.exists() || length == 0) {
            return new byte[0];
        }
        byte[] buffer = new byte[length];
        try (FileInputStream fis = FileUtils.openInputStream(file)) {
            int count = IOUtils.read(fis, buffer);
            if (count >= length) {
                return buffer;
            }
            byte[] bytes = new byte[count];
            System.arraycopy(buffer, 0, bytes, 0, count);
            return bytes;
        }
    }

    public static byte[] readBytes(File file, long offset, int length) throws IOException {
        if (!file.exists() || length == 0) {
            return new byte[0];
        }
        ByteArray bar = new ByteArray(length);
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(offset);
            byte[] buffer = new byte[length > 8192 ? 8192 : length];
            while (length > 0) {
                int bytesRead = raf.read(buffer);
                length -= bytesRead;
                bar.addAll(buffer, 0, bytesRead);
            }
        }
        if (length < 0) {
            bar.removeRange(bar.size + length, bar.size - 1);
        }
        return bar.toArray();
    }

    public static void write(File file, String data) throws IOException {
        if (!file.exists()){
            try {
                File parent = file.getParentFile();
                if (!parent.exists()){
                    parent.mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileUtils.write(file, data, Charsets.UTF_8);
    }

    public static JsonElement parse(String jsonString){
        return JsonParser.parseString(jsonString);
    }

    public static JsonElement parse(File file) throws IOException {
    	if (file.exists()) {
    		return parse(read(file));
		}
    	return null;
    }

    public static String getString(JsonObject o, String field){
        return getString(o, field, null);
    }

    public static String getString(JsonObject o, String field, String def){
        JsonElement jsonElement = o.get(field);
        if (jsonElement != null) return jsonElement.getAsString();
        return def;
    }

    public static Long getLong(JsonObject o, String field){
        return getLong(o, field, null);
    }

    public static Long getLong(JsonObject o, String field, Long def){
        JsonElement jsonElement = o.get(field);
        if (jsonElement != null) return jsonElement.getAsLong();
        return def;
    }

    public static boolean getBool(JsonObject o, String field){
        return getBool(o, field, false);
    }

    public static boolean getBool(JsonObject o, String field, boolean def){
        JsonElement jsonElement = o.get(field);
        if (jsonElement != null) return jsonElement.getAsBoolean();
        return def;
    }

    public static JsonObject getObject(JsonObject o, String field, JsonObject def){
        JsonElement jsonElement = o.get(field);
        if (jsonElement != null) return jsonElement.getAsJsonObject();
        return def;
    }

    public static JsonArray getArray(JsonObject o, String field, JsonArray def){
        JsonElement jsonElement = o.get(field);
        if (jsonElement != null) return jsonElement.getAsJsonArray();
        return def;
    }

    public static JsonElement getByPath(JsonElement o, String... path){
        JsonElement e = o;
        for (String s : path) {
            if (o == null || o.isJsonNull()) {
                return null;
            }
            if (!o.isJsonObject() && !o.isJsonArray()){
                throw new RuntimeException("Bad type " + o.getClass().getSimpleName());
            }
            if (e.isJsonObject()){
                e = e.getAsJsonObject().get(s);
            } else {
                JsonArray arr = e.getAsJsonArray();
                int index = Integer.parseInt(s.trim());
                e = arr.get(index);
            }
        }
        return e;
    }

    public static void add(JsonObject o, String name, Integer primitive) {
        if (primitive != null) {
            o.add(name, new JsonPrimitive(primitive));
        }
    }

    public static void add(JsonObject o, String name, String primitive) {
        if (primitive != null) {
            o.add(name, new JsonPrimitive(primitive));
        }
    }

    public static void add(JsonObject o, String name, Boolean primitive) {
        if (primitive != null) {
            o.add(name, new JsonPrimitive(primitive));
        }
    }

    public static void add(JsonObject o, String name, Float primitive) {
        if (primitive != null) {
            o.add(name, new JsonPrimitive(primitive));
        }
    }

    public static void add(JsonObject o, String name, Double primitive) {
        if (primitive != null) {
            o.add(name, new JsonPrimitive(primitive));
        }
    }

    public static void add(JsonObject o, String name, Long primitive) {
        if (primitive != null) {
            o.add(name, new JsonPrimitive(primitive));
        }
    }

    public static void add(JsonObject o, String name, JsonElement e) {
        if (e != null) {
            o.add(name, e);
        }
    }

    public static BasicFileAttributes getFileAttributes(File file) throws IOException {
        return Files.readAttributes(Paths.get(file.getPath()), BasicFileAttributes.class);
    }
    public static long lastModificationTime(File file, long def) {
        try {
            return lastModificationTime(file);
        } catch (IOException e) {}
        return def;
    }

    public static long lastModificationTime(File file) throws IOException {
        if (!file.exists()) return -1;
        return getFileAttributes(file).lastModifiedTime().toMillis();
    }

    @SuppressWarnings("all")
    public static String findUnique(String source, @Language("RegExp") String regexp, @Nullable Integer group){
        Matcher matcher = Pattern.compile(regexp).matcher(source);

        if (matcher.find()){
            String result = group == null ? matcher.group() : matcher.group(group);

            while (matcher.find()){
                String duplicate = group == null ? matcher.group() : matcher.group(group);
                if (!duplicate.equals(result)){
                    throw new RuntimeException(String.format("Result is not unique {regex=%s, result=%s, duplicate=%s}", regexp, result, duplicate));
                }
            }
            return result;
        } else {
            throw new RuntimeException("Not found");
        }
    }

    public static String findFirst(String source, @Language("RegExp") String regexp, @Nullable Integer group){
        Matcher matcher = Pattern.compile(regexp).matcher(source);

        if (matcher.find()){
            return group == null ? matcher.group() : matcher.group(group);
        }
        return null;
    }

    public static List<String> collectMatch(String text, @RegExp String regex, int group) {
        ArrayList<String> results = new ArrayList<>();
        Matcher matcher = Pattern.compile(regex).matcher(text);
        while (matcher.find()) {
            String g = matcher.group(group);
            results.add(g);
        }
        return results;
    }

    public static List<String[]> collectMatches(String text, @RegExp String regex, int... groups) {
        ArrayList<String[]> results = new ArrayList<>();
        Matcher matcher = Pattern.compile(regex).matcher(text);
        while (matcher.find()) {
            String[] matches = new String[groups.length];
            for (int i = 0; i < groups.length; i++) {
                String g = matcher.group(groups[i]);
                matches[i] = g;
            }
            results.add(matches);
        }
        return results;
    }

    public static String wrapString(String s, int maxCharactersPerLine){
        if (s.indexOf('\n') == -1) {
            return wrapInlineString(s, maxCharactersPerLine);
        }
        String[] lines = s.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (line.length() > maxCharactersPerLine) {
                line = wrapInlineString(line, maxCharactersPerLine);
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    /**
     * Делит строку на линии по пробелам. В строке не должно содержаться \n
     * @param maxCharactersPerLine Максимальное количество символов в строке
     */
    public static String wrapInlineString(String s, int maxCharactersPerLine){
        if (s == null) return null;
        if (s.length() < maxCharactersPerLine){
            return s;
        }

        String[] split = StringUtils.splitPreserveAllTokens(s, ' ');
        if (split.length < 2){
            return s;
        }
        StringBuilder builder = new StringBuilder();
        int currentLineSize = 0;
        boolean first = true;
        int i = 0;
        while (i < split.length){
            String word = split[i];

            if (first){
                builder.append(word);
                currentLineSize = word.length();
                i++;
                first = false;
                continue;
            }

            if (currentLineSize + word.length() >= maxCharactersPerLine){
                builder.append('\n');
                currentLineSize = 0;
                first = true;
            } else {
                builder.append(" ").append(word);
                currentLineSize += word.length() + 1;
                i++;
            }


        }

        return builder.toString();

    }

	public static void createFile(File file) throws IOException {
        if (file.exists()) return;
        file.getParentFile().mkdirs();
        file.createNewFile();
	}

    public static File createTempFolder() {
        return createTempFolder("");
    }

    public static File createTempFolder(String prefix) {
        File tempDir = FileUtils.getTempDirectory();
        File tempFolder = getNonTakenFolder(new File(tempDir, prefix + String.valueOf(System.currentTimeMillis())));
        if (!tempFolder.exists()){
            tempFolder.mkdirs();
        }
        return tempFolder;
    }

    public static void forEachFile(File file, Consumer<File> fileConsumer) {
        forEachFileAndDirs(file, f -> {
            if (f.isFile()) {
                fileConsumer.accept(f);
            }
        });
    }

    public static void forEachFileAndDirs(File file, Consumer<File> fileConsumer) {
        if (!file.exists()) return;
        fileConsumer.accept(file);
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    forEachFileAndDirs(subFile, fileConsumer);
                }
            }
        }
    }

    public static File getNonTakenFolder(File folder) {
        if (!folder.exists()) return folder;
        File parent = folder.getParentFile();
        if (parent != null) {
            while (folder.exists()) {
                folder = new File(parent, incrementFileName(folder.getName()));
            }
        } else throw new RuntimeException("Failed to get non taken folder for " + folder.getAbsolutePath());
        return folder;
    }

    public static File getNonTakenFile(File file) {
        String nameNoExt = StringUtils.substringBefore(file.getName(), ".");
        String ext = StringUtils.substringAfterLast(file.getName(), ".");
        File parent = file.getParentFile();
        while (file.exists()) {
            nameNoExt = incrementFileName(nameNoExt);
            String fullName = nameNoExt + (StringUtils.isEmpty(ext) ? "" : "." + ext);
            file = new File(parent, fullName);
        }
        return file;
    }

    public static String getExtension(File f) {
        return getExtension(f.getName());
    }

    public static String getExtension(String fileName) {
        return StringUtils.substringAfterLast(fileName, ".").toLowerCase();
    }

    public static String getExtensionForPath(String filePath) {
        if (filePath.contains("/")) {
            return getExtension(StringUtils.substringAfterLast(filePath, "/"));
        } else if (filePath.contains("\\")) {
            return getExtension(StringUtils.substringAfterLast(filePath, "\\"));
        }
        return getExtension(filePath);
    }

    /**
     * "name" -> "name(1)"
     * "name(1)" -> "name(2)"
     */
    private static String incrementFileName(String fileNameNoExt) {
        Matcher matcher = Pattern.compile("^(.*)\\((-?\\d+)\\)$").matcher(fileNameNoExt);

        if (matcher.find()) {
            String name = matcher.group(1);
            int number = Integer.parseInt(matcher.group(2));
            return name + "(" + (number + 1) + ")";
        } else {
            return fileNameNoExt + "(1)";
        }
    }

    public static String combinePaths(String parent, String child, String divider) {
        if (parent.length() == 0 || parent.endsWith(divider)) {
            return parent + (child.startsWith(divider) ? child.substring(1) : child);
        } else {
            return parent + (child.startsWith(divider) ? child : divider + child);
        }
    }

    public static <T> T random(T... variants) {
        if (variants == null || variants.length == 0) return null;
        return variants[MathUtils.random(variants.length - 1)];
    }

    public interface ArchiveEntryConsumer {

        /**
         * @param archiveName name of the archive we're in.
         * @param path Path to current archive relative to the top-most archive.
         * @param entry The entry
         * To get full path for the entry, you must combine all of these. Just use {@link #getFullPath(File, String, ZipEntry)}
         */
        default boolean filter(String archiveName, String path, ZipEntry entry) {
            return true;
        }

        /**
         * @param archiveName name of the archive we're currently in
         * @param path Path to current archive relative to the top-most archive
         * @param entry The entry
         * @param fileName the name of the entry without pathing
         * @param content content of the entry
         */
        void consume(String archiveName, String path, ZipEntry entry, String fileName, byte[] content);

        default String getFullPath(File topArchive, String path, ZipEntry entry){
            return Utils.combinePaths(topArchive.getAbsolutePath(), Utils.combinePaths(path, entry.getName(), "/").replaceAll("/", "\\\\"), "\\");
        }

    }

    public static void forEachFileAndDir(File file, FileIterator iterator){
        if (!file.exists()) return;

        iterator.progressChanged(0.0);
        iterator.accept(file);
        if (file.isDirectory()) {
            IntArray total = new IntArray();
            IntArray currentPosition = new IntArray();
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                int dirs = 0;
                for (File subFile : subFiles) {
                    if (subFile.isDirectory()) {
                        dirs++;
                    }
                }
                total.add(dirs);
                currentPosition.add(0);
                int lastIndex = currentPosition.size - 1;
                for (File subFile : subFiles) {
                    forEachFileAndDir(subFile, iterator, total, currentPosition, false);
                    if (subFile.isDirectory()){
                        currentPosition.incr(lastIndex, 1);
                        iterator.progressChanged(getCurrentProgress(total, currentPosition));
                    }
                }
            }
        }
    }

    private static void forEachFileAndDir(File file, FileIterator iterator, IntArray total, IntArray currentPosition, boolean parenthasFiles) {
        iterator.accept(file);

        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                int dirs = 0;
                for (File subFile : subFiles) {
                    if (subFile.isDirectory()) {
                        dirs++;
                    }
                }
                boolean hasFiles = dirs < subFiles.length;
                total.add(hasFiles ? dirs + 1 : dirs);
                currentPosition.add(0);
                int lastIndex = currentPosition.size - 1;
                if (dirs > 0){
                    for (File subFile : subFiles) {
                        if (subFile.isDirectory()){
                            forEachFileAndDir(subFile, iterator, total, currentPosition, hasFiles);
                            currentPosition.incr(lastIndex, 1);
                            if (!hasFiles && currentPosition.get(lastIndex) != total.get(lastIndex)){
                                iterator.progressChanged(getCurrentProgress(total, currentPosition));
                            }
                        }
                    }
                }
                if (hasFiles) {
                    for (File subFile : subFiles) {
                        if (subFile.isFile()) {
                            forEachFileAndDir(subFile, iterator, total, currentPosition, hasFiles);
                        }
                    }
                    if (parenthasFiles) {
                        iterator.progressChanged(getCurrentProgress(total, currentPosition));
                    }
                }
                total.pop();
                currentPosition.pop();
            }
        }
    }


    private static double getCurrentProgress(IntArray totalTree, IntArray currentPosition) {
        return getCurrentProgress(totalTree, currentPosition, 0, 100.0, 0);
    }

    private static double getCurrentProgress(IntArray totalTree, IntArray currentPosition, int index, double range, double current) {
        if (index >= totalTree.size) {
            return current;
        }
        int curr = currentPosition.get(index);
        int total = totalTree.get(index);
        if (total == 0) return current;
        double valueToAdd = (((double) curr) / total) * range;
        double childRange = range / total;
        return getCurrentProgress(totalTree, currentPosition, index + 1, childRange, current + valueToAdd);
    }

    public interface FileIterator {

        void accept(File file);

        /** How much progress is done 0..100. Called first with 0. **/
        void progressChanged(double progress);

    }
}
