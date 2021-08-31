package reader;

import org.apache.jena.rdf.model.Model;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Reader {

    public static void readModelFromDirectory(String directoryName, Model model, String extension) {
        if (extension != null) {
            System.out.println("EXT : " + extension);
        }
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryName))) {
            for (Path path : directoryStream) {
                String pathStr = path.toString().replace("\\", "/");
                if (extension != null) {
                    model.read(pathStr, extension);
                }
                else {
                    model.read(pathStr);
                }
            }
        } catch (IOException ex) { }

    }



}
