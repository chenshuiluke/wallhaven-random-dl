package com.lukechenshui;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import java.net.URLConnection;

public class Main {
    private static int numberOfRetriesForRandomlySelectingId = 0;
    private static boolean checkIfImageAlreadyDownloaded(int id){
        File random = new File("random");
        File[] contents = random.listFiles();
        for(File file : contents){
            String fileName = file.getName();
            fileName = FilenameUtils.removeExtension(fileName);
            if(Integer.valueOf(fileName) == id){
                numberOfRetriesForRandomlySelectingId = 0;
                return true;
            }
        }
        numberOfRetriesForRandomlySelectingId++;
        return false;
    }
    private static ArrayList<Integer> getPictureIds(long number){
        String baseURL = "https://alpha.wallhaven.cc/latest?page=";
        //System.out.println("Connecting...");
        ArrayList<Integer> ids = new ArrayList<>();
        try {
            for (long counter = 0; counter < number; counter++) {

                Document page = Jsoup.connect(baseURL + String.valueOf(counter + 1)).
                        timeout(0)
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64; rv:47.0) Gecko/20100101 Firefox/47.0")
                        .get();
                Elements images = page.select("a.preview");
                for(Element element : images){
                    String href = element.attr("href");
                    int pictureId = 0;
                    Pattern pictureIdRegex = Pattern.compile("\\d+");
                    Matcher matcher = pictureIdRegex.matcher(href);
                    if(matcher.find()){
                        pictureId = Integer.valueOf(matcher.group());
                        ids.add(pictureId);
                    }
                }
            }
        }
        catch(IOException exc){
            exc.printStackTrace();
        }
        return ids;
    }
    public static String getCurrentDateTime(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }
    public static String createWallpaperSessionDirectory(){
        try{
            Path folder = Paths.get("wallpapers-" + getCurrentDateTime());
            if(!Files.exists(folder)){
                Files.createDirectory(folder);
            }
            if(!Files.exists(Paths.get("random"))){
                Files.createDirectory(Paths.get("random"));
            }
            return folder.toString();
        }
        catch(IOException exc){
            exc.printStackTrace();
        }
        return null;
    }
    public static void getPicturesFromIds(ArrayList ids, String path){
        for(int counter = 0; counter < ids.size(); counter++){
            System.out.printf("Downloading image :\t%s\n", ids.get(counter));
            String[] exts = {".jpg", ".png", ".bmp"};

            for(String extension : exts){
                try{
                    URL imageURL = new URL("https://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-" + ids.get(counter) + extension);
                    URLConnection conn = imageURL.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:47.0) Gecko/20100101 Firefox/47.0");
                    conn.connect();
                    //System.out.println(imageURL);
                    FileUtils.copyInputStreamToFile(conn.getInputStream(), Paths.get(path + File.separator + String.valueOf(ids.get(counter)) + extension).toFile());
                }
                catch(Exception exc){
                    //exc.printStackTrace();
                }
            }
        }
    }
    public static void main(String[] args) {
        long input = 0;
        String directory = createWallpaperSessionDirectory();
        if(args.length == 1){
            int iterations = Integer.valueOf(args[0]);

            for(int counter = 0; counter < iterations; counter++){
                System.out.printf("%d / %d ", counter + 1, iterations);
                ArrayList<Integer> ids = getPictureIds(1);
                int max = ids.get(0);
                int random;
                do{
                    random = ThreadLocalRandom.current().nextInt(1, max + 1);
                    if(numberOfRetriesForRandomlySelectingId > max){
                        System.out.println("All randomly generated image ids already exist in the random folder as images.");
                        System.exit(1);
                    }
                }while(checkIfImageAlreadyDownloaded(random));
                ids = new ArrayList<>();
                ids.add(random);
                getPicturesFromIds(ids, "random");
            }
            System.out.println("Done! Files are in 'random'");
        }
        else{
            System.out.println("wallhaven-random-dl <number_of_random_images>");
        }

    }
}
