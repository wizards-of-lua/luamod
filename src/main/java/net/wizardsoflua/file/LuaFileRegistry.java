package net.wizardsoflua.file;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.entity.player.EntityPlayer;
import net.wizardsoflua.config.RestConfig;

public class LuaFileRegistry {

  public interface Context {
    File getPlayerLibDir(UUID playerId);

    RestConfig getRestConfig();
  }

  private Context context;

  public LuaFileRegistry(Context context) {
    this.context = context;
  }

  public List<String> getLuaFilenames(EntityPlayer player) {
    try {
      Path playerLibDir = Paths.get(context.getPlayerLibDir(player.getUniqueID()).toURI());
      try (Stream<Path> files = Files.walk(playerLibDir, FileVisitOption.FOLLOW_LINKS)) {
        return files.map(p -> playerLibDir.relativize(p).toString()).collect(Collectors.toList());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public URL getFileEditURL(EntityPlayer player, String filepath) {
    String hostname = context.getRestConfig().getHostname();
    int port = context.getRestConfig().getPort();

    String fileReference = getFileReferenceFor(player, filepath);
    try {
      URL result = new URL("http://" + hostname + ":" + port + "/wol/lua/" + fileReference);
      return result;
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public LuaFile loadLuaFile(String fileReference) {
    try {
      String filepath = getFilepathFor(fileReference);
      UUID playerId = getPlayerIdFor(fileReference);
      File file = new File(context.getPlayerLibDir(playerId), filepath);
      String name = file.getName();
      String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
      return new LuaFile(filepath, name, fileReference, content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void saveLuaFile(String fileReference, String content) {
    try {
      String filepath = getFilepathFor(fileReference);
      UUID playerId = getPlayerIdFor(fileReference);
      File file = new File(context.getPlayerLibDir(playerId), filepath);
      byte[] bytes = content.getBytes();
      Files.write(Paths.get(file.toURI()), bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getFileReferenceFor(EntityPlayer player, String filepath) {
    return player.getUniqueID().toString() + "/" + filepath;
  }

  private String getFilepathFor(String fileReference) {
    int index = fileReference.indexOf('/');
    String result = fileReference.substring(index + 1);
    return result;
  }

  private UUID getPlayerIdFor(String fileReference) {
    int index = fileReference.indexOf('/');
    String playerIdStr = fileReference.substring(0, index);
    UUID result = UUID.fromString(playerIdStr);
    return result;
  }

}
