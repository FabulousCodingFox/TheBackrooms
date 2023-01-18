package engine;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import utils.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {
    int texture;

    public static ByteBuffer resourceToByteBuffer(String path) throws IOException {
        byte[] bytes;
        path = path.trim();

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

        if (stream == null) {
            throw new FileNotFoundException(path);
        }

        bytes = IOUtils.toByteArray(stream);

        ByteBuffer data = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder()).put(bytes);
        data.flip();
        return data;
    }

    public Texture(String path, int texChannel) {
        glActiveTexture(texChannel);
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);

        // set the texture wrapping parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        // set texture filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // load image, create texture and generate mipmaps
        //String absolutePath = FileUtils.class.getClassLoader().getResource(path).getPath().substring(1);
        //if(!System.getProperty("os.name").contains("Windows")){
        //    absolutePath = File.separator + absolutePath;
        //}
        stbi_set_flip_vertically_on_load(true);
        IntBuffer x = BufferUtils.createIntBuffer(1);
        IntBuffer y = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        ByteBuffer imageBuffer = null;
        try {
            imageBuffer = resourceToByteBuffer(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ByteBuffer image = stbi_load_from_memory(imageBuffer, x, y, channels, STBI_rgb);
        if (image == null) {
            throw new IllegalStateException("Could not decode image file ["+ path +"]: ["+ stbi_failure_reason() +"]");
        }
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, x.get(), y.get(), 0, GL_RGB, GL_UNSIGNED_BYTE, image);
        glGenerateMipmap(GL_TEXTURE_2D);
        stbi_image_free(image);
    }

    public int get() {
        return texture;
    }
}
