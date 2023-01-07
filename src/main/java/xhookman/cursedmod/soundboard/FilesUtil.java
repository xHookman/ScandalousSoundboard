package xhookman.cursedmod.soundboard;

import xhookman.cursedmod.ModLauncher;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static xhookman.cursedmod.Cursedmod.MOD_ID;

public class FilesUtil {
    private static File soundboardDir;
    private static String newJarName;
    private static int soundsCount;
    private static ArrayList<String> soundsName;
    
    public static void checkFilesName(File dir){
        for (File file : dir.listFiles()) {
            if(file.getName().endsWith(".ogg")) {
                String soundFileName = file.getName().split(".ogg")[0];
                System.out.println("Ajout de " + soundFileName);
                if (!file.getName().matches("[a-z0-9_]")) {
                    file.renameTo(new File(dir, soundFileName.replaceAll("[^a-z0-9_]+", "")+".ogg"));
                }
            }
        }
    }
    ;
    public static void createFiles(){
        soundboardDir = new File("soundboard/");
        if (!soundboardDir.exists()) {
            soundboardDir.mkdir();
        }
    }
    public static File getDir(){
        return soundboardDir;
    }

    public static void addFileJson(JarOutputStream jos, String name, String data) throws IOException {
        jos.putNextEntry(new JarEntry(name));
        InputStream is = new ByteArrayInputStream(data.getBytes());
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            jos.write(buffer, 0, bytesRead);
        }
        is.close();
    }
    public static void addFile(JarOutputStream jos, String name, String path) throws IOException {
        jos.putNextEntry(new JarEntry(name));
        InputStream is = Files.newInputStream(Paths.get(path));
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            jos.write(buffer, 0, bytesRead);
        }
        is.close();
    }

    public static void generateFiles(File folder) {
        // Open the original .jar file for reading
        try {
            JarFile originalJar = new JarFile(getJarName());

            Random r = new Random();

            // Create a new .jar file for writing
            newJarName = "soundboard" + r.nextLong() + ".jar";
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(newJarName));
            // Iterate through all the entries in the original .jar file
            Enumeration<JarEntry> entries = originalJar.entries();

            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                // If the entry is the file you want to add, don't copy it
                // from the original .jar. Instead, add it to the new .jar
                // using the addFile() method
                if (!entry.getName().startsWith("assets/cursedmod/")) {
                    // copy the entry from the original .jar to the new .jar
                    jos.putNextEntry(new JarEntry(entry.getName()));
                    InputStream is = originalJar.getInputStream(entry);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        jos.write(buffer, 0, bytesRead);
                    }
                    is.close();
                }
            }
            for(int i=0; i<folder.listFiles().length; i++)
                if(folder.listFiles()[i].getName().endsWith(".ogg"))
                    addFile(jos, "assets/cursedmod/sounds/" + soundboardDir.listFiles()[i].getName(), soundboardDir.getPath() + "/" + soundboardDir.listFiles()[i].getName());

            addFileJson(jos, "assets/cursedmod/sounds.json", generateSoundsJson(folder));

            // Close the streams
            originalJar.close();
            jos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String generateSoundsJson(File folder) {
                String content = "{";

                for(int i=0; i<folder.listFiles().length; i++){
                    if(folder.listFiles()[i].getName().endsWith(".ogg")) {
                        String fileName = folder.listFiles()[i].getName().split(".ogg")[0];
                        content += "\"" + fileName + "\": {\n" +
                                "    \"sounds\": [\n" +
                                "      \"cursedmod:" + fileName + "\"\n" +
                                "    ]\n";
                        if (i + 1 < folder.listFiles().length)
                            content += "  },\n";
                        else
                            content += "  }\n";
                    }
                }
                content+="}";
                return content;
    }

    public static int getSoundsCount(){
        return soundsCount;
    }

    public static ArrayList<String> getSoundsName(){
        return soundsName;
    }

    public static void readSoundsJson() throws IOException {
        soundsName = new ArrayList<>();
        InputStream inputStream = SoundboardServer.class.getResourceAsStream("/assets/"+ MOD_ID + "/sounds.json");
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        for(int i=0; (line = reader.readLine()) != null; i++){
            if(line.contains(MOD_ID + ":")){
                soundsCount++;
                soundsName.add(line.split(":")[1].split("\"")[0]);
            }
        }

        reader.close();
        inputStreamReader.close();
        inputStream.close();
    }
    public static String getJarName() {
        return ModLauncher.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    }
}
