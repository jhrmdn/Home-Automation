package mdn.jh.automation.webserver;

import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.json.JSONObject;

/** Validated raster uploads addressed by content hash, shared by dashboard layouts. */
public final class DashboardImageStore {
    private final Path directory;
    public DashboardImageStore(Path directory) { this.directory=directory; }
    public static boolean validId(String id) { return id!=null && id.matches("[a-f0-9]{64}\\.(png|jpg)"); }
    public JSONObject upload(String encoded) throws Exception {
        if(encoded.length()>5_600_000)throw new IllegalArgumentException("Image must be at most 4 MiB");
        byte[] bytes=Base64.getDecoder().decode(encoded);
        if(bytes.length>4*1024*1024)throw new IllegalArgumentException("Image must be at most 4 MiB");
        int width,height;String extension;
        try(var input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers=ImageIO.getImageReaders(input);
            if(!readers.hasNext())throw new IllegalArgumentException("Use PNG or JPEG images");
            var reader=readers.next();
            try {
                reader.setInput(input);String format=reader.getFormatName();
                if(!format.equalsIgnoreCase("PNG")&&!format.equalsIgnoreCase("JPEG"))throw new IllegalArgumentException("Use PNG or JPEG images");
                extension=format.equalsIgnoreCase("PNG")?"png":"jpg";
                width=reader.getWidth(0);height=reader.getHeight(0);
                if(width<1||height<1||(long)width*height>16_000_000)throw new IllegalArgumentException("Image may contain at most 16 megapixels");
                if(reader.read(0)==null)throw new IllegalArgumentException("Invalid image");
            } finally { reader.dispose(); }
        }
        String id=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))+"."+extension;
        Files.createDirectories(directory);
        Path target=directory.resolve(id);
        if(!Files.exists(target)) {
            Path temporary=Files.createTempFile(directory,"upload-",".tmp");
            try { Files.write(temporary,bytes);Files.move(temporary,target,StandardCopyOption.REPLACE_EXISTING); }
            finally { Files.deleteIfExists(temporary); }
        }
        return new JSONObject().put("id",id).put("width",width).put("height",height);
    }
    public String read(String id) throws Exception {
        if(!validId(id))throw new IllegalArgumentException("Invalid image ID");
        Path file=directory.resolve(id);
        if(Files.size(file)>4*1024*1024)throw new IllegalArgumentException("Image exceeds size limit");
        return "data:image/"+(id.endsWith(".png")?"png":"jpeg")+";base64,"+Base64.getEncoder().encodeToString(Files.readAllBytes(file));
    }
}
