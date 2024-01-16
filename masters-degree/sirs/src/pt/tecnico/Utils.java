package pt.tecnico;

import java.io.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import com.google.gson.*;



public class Utils {
    private static boolean DEBUG = false;

    public static void SetDebug(boolean debug) {
        Utils.DEBUG = debug;
    }

    public static void WriteJsonFile(String filename, JsonObject jsonObj) throws IOException {
        try (FileWriter fileWriter = new FileWriter(filename)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObj, fileWriter);
        }
        catch (IOException e) {
            throw new RuntimeException("IOException occurred", e);
        }
    }

    public static JsonObject ReadJsonFile(String path) throws FileNotFoundException, IOException {
        try (FileReader fileReader = new FileReader(path)) {
            JsonObject obj = null;

            return obj;
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("File {0} not found!", path), e);
        }
        catch (IOException e) {
            throw new RuntimeException("IOException occurred", e);
        }
    }

    public static void WriteKeyFile(String path, Key key) {

    }

    public static Key ReadKeyFile(String path, String mode) throws FileNotFoundException, IOException, IllegalArgumentException {
        if (!mode.equals("private") && !mode.equals("public")) {
            throw new IllegalArgumentException("Invalid mode. Mode must be either 'private' or 'public'.");
        }

        try (FileReader fileReader = new FileReader(path)) {
            if (mode == "public") {
                PublicKey pub = null;

                return pub;
            }
            else {
                PrivateKey priv = null;

                return priv;
            }
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("File {0} not found!", path), e);
        }
        catch (IOException e) {
            throw new RuntimeException("IOException occurred", e);
        }
    }

    public static JsonObject CreateJson() {
        JsonObject jsonObj = new JsonObject();
        
        JsonObject metadataObject = new JsonObject();
        jsonObj.add("metadata", metadataObject);
        
        JsonObject dataObject = new JsonObject();
        jsonObj.add("data", dataObject);

        return jsonObj;
    }

    public static byte[] Hash(String json) {
        byte[] bytes = null;

        return bytes;
    }   

    public static JsonObject ExtractJson(String path) throws IOException {
        try (FileReader fileReader = new FileReader(path)) {

            Gson gson = new Gson();
            return gson.fromJson(fileReader, JsonObject.class);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("File %s not found!", path), e);
        }
        catch (IOException e) {
            throw new RuntimeException("IOException occurred", e);
        }
    }
}
