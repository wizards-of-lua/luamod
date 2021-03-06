package net.wizardsoflua;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.wizardsoflua.config.GeneralConfig;
import net.wizardsoflua.config.RestApiConfig;
import net.wizardsoflua.config.WizardConfig;
import net.wizardsoflua.config.WolConfig;
import net.wizardsoflua.event.WolEventHandler;
import net.wizardsoflua.file.LuaFile;
import net.wizardsoflua.file.LuaFileRepository;
import net.wizardsoflua.file.SpellPack;
import net.wizardsoflua.filesystem.RestrictedFileSystem;
import net.wizardsoflua.gist.GistRepo;
import net.wizardsoflua.lua.ExtensionLoader;
import net.wizardsoflua.lua.LuaCommand;
import net.wizardsoflua.lua.SpellProgramFactory;
import net.wizardsoflua.lua.extension.InjectionScope;
import net.wizardsoflua.lua.module.searcher.LuaFunctionBinaryCache;
import net.wizardsoflua.permissions.Permissions;
import net.wizardsoflua.profiles.Profiles;
import net.wizardsoflua.rest.WolRestApiServer;
import net.wizardsoflua.spell.ChunkLoaderTicketSupport;
import net.wizardsoflua.spell.SpellEntityFactory;
import net.wizardsoflua.spell.SpellRegistry;
import net.wizardsoflua.startup.Startup;
import net.wizardsoflua.wol.WolCommand;

@Mod(modid = WizardsOfLua.MODID, version = WizardsOfLua.VERSION, acceptableRemoteVersions = "*",
    updateJSON = "https://raw.githubusercontent.com/wizards-of-lua/wizards-of-lua/master/versions.json")
public class WizardsOfLua {
  public static final String MODID = "wol";
  public static final String NAME = "Wizards of Lua";
  public static final String CONFIG_NAME = "wizards-of-lua";
  public static final String VERSION = "@MOD_VERSION@";
  public static final String URL = "http://www.wizards-of-lua.net";

  @Instance(MODID)
  public static WizardsOfLua instance;

  public Logger logger;

  private final SpellRegistry spellRegistry = new SpellRegistry();
  private final LuaFunctionBinaryCache luaFunctionCache = new LuaFunctionBinaryCache();
  private final GistRepo gistRepo = new GistRepo();

  // TODO move these lazy instances into a new state class
  private Path tempDir;
  private WolConfig config;
  private AboutMessage aboutMessage;
  private WolEventHandler eventHandler;
  private SpellEntityFactory spellEntityFactory;
  private SpellProgramFactory spellProgramFactory;
  private Profiles profiles;
  private LuaFileRepository fileRepository;
  private WolRestApiServer restApiServer;

  private MinecraftServer server;
  private GameProfiles gameProfiles;
  private Startup startup;
  private Permissions permissions;
  private FileSystem worldFileSystem;

  /**
   * Clock used for RuntimeModule
   */
  private Clock clock = getDefaultClock();
  private InjectionScope rootScope = new InjectionScope();

  public WizardsOfLua() {}

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) throws Exception {
    logger = event.getModLog();
    ExtensionLoader.initialize(logger);
    tempDir = Files.createTempDirectory("wizards-of-lua");
    config = WolConfig.create(event, CONFIG_NAME);
    aboutMessage = new AboutMessage(new AboutMessage.Context() {

      @Override
      public boolean shouldShowAboutMessage() {
        return getConfig().getGeneralConfig().isShowAboutMessage();
      }

      @Override
      public String getVersion() {
        return VERSION;
      }

      @Override
      public String getUrl() {
        return URL;
      }

      @Override
      public @Nullable String getRecommendedVersion() {
        String result = null;
        for (ModContainer mod : Loader.instance().getModList()) {
          if (mod.getModId().equals(MODID)) {
            CheckResult checkResult = ForgeVersion.getResult(mod);
            Status status = checkResult.status;
            if (status == Status.OUTDATED || status == Status.BETA_OUTDATED) {
              result = checkResult.target.toString();
            }
          }
        }
        return result;
      }
    });
    spellProgramFactory = new SpellProgramFactory(logger, new SpellProgramFactory.Context() {
      @Override
      public Clock getClock() {
        return clock;
      }

      @Override
      public long getLuaTicksLimit() {
        return getConfig().getGeneralConfig().getLuaTicksLimit();
      }

      @Override
      public long getEventListenerLuaTicksLimit() {
        return config.getGeneralConfig().getEventListenerLuaTicksLimit();
      }

      @Override
      public @Nullable String getLuaPathElementOfPlayer(String nameOrUuid) {
        UUID uuid = getUUID(nameOrUuid);
        return getConfig().getOrCreateWizardConfig(uuid).getLibDirPathElement();
      }

      private UUID getUUID(String nameOrUuid) {
        try {
          return UUID.fromString(nameOrUuid);
        } catch (IllegalArgumentException e) {
          GameProfile profile = gameProfiles.getGameProfileByName(nameOrUuid);
          if (profile != null) {
            return profile.getId();
          } else {
            throw new IllegalArgumentException(
                format("Player not found with name '%s'", nameOrUuid));
          }
        }
      }

      @Override
      public String getSharedLuaPath() {
        return getConfig().getSharedLuaPath();
      }

      @Override
      public Profiles getProfiles() {
        return profiles;
      }

      @Override
      public LuaFunctionBinaryCache getLuaFunctionBinaryCache() {
        return luaFunctionCache;
      }

      @Override
      public boolean isScriptGatewayEnabled() {
        return getConfig().getScriptGatewayConfig().isEnabled();
      }

      @Override
      public Path getScriptDir() {
        return getConfig().getScriptGatewayConfig().getDir();
      }

      @Override
      public long getScriptTimeoutMillis() {
        return getConfig().getScriptGatewayConfig().getTimeoutMillis();
      }

      @Override
      public SpellRegistry getSpellRegistry() {
        return WizardsOfLua.this.getSpellRegistry();
      }

      @Override
      public InjectionScope getRootScope() {
        return rootScope;
      }

      @Override
      public FileSystem getWorldFileSystem() {
        return WizardsOfLua.this.getWorldFileSystem();
      }

    });
    spellEntityFactory = new SpellEntityFactory(spellRegistry, spellProgramFactory);
    profiles = new Profiles(new Profiles.Context() {

      @Override
      public GeneralConfig getGeneralConfig() {
        return getConfig().getGeneralConfig();
      }

      @Override
      public WizardConfig getWizardConfig(EntityPlayer player) {
        return getConfig().getOrCreateWizardConfig(player.getUniqueID());
      }

    });
    eventHandler = new WolEventHandler(() -> spellRegistry.getAll());
    fileRepository = new LuaFileRepository(new LuaFileRepository.Context() {
      @Override
      public File getPlayerLibDir(UUID playerId) {
        return getConfig().getOrCreateWizardConfig(playerId).getLibDir();
      }

      @Override
      public RestApiConfig getRestApiConfig() {
        return getConfig().getRestApiConfig();
      }

      @Override
      public File getSharedLibDir() {
        return getConfig().getSharedLibDir();
      }

      @Override
      public String getPlayerRestApiKey(UUID playerId) {
        return getConfig().getOrCreateWizardConfig(playerId).getRestApiKey();
      }

      @Override
      public boolean isOperator(UUID playerId) {
        return permissions.hasOperatorPrivileges(playerId);
      }

      @Override
      public Path getTempDir() {
        return tempDir;
      }
    });

    restApiServer = new WolRestApiServer(new WolRestApiServer.Context() {
      @Override
      public LuaFile getLuaFileByReference(String fileReference) {
        return getFileRepository().loadLuaFile(fileReference);
      }

      @Override
      public RestApiConfig getRestApiConfig() {
        return getConfig().getRestApiConfig();
      }

      @Override
      public void saveLuaFileByReference(String fileReference, String content) {
        getFileRepository().saveLuaFile(fileReference, content);
      }

      @Override
      public boolean isValidLoginToken(UUID playerId, String token) {
        return getFileRepository().isValidLoginToken(playerId, token);
      }

      @Override
      public SpellPack createSpellPackByReference(String fileReference) {
        return getFileRepository().createSpellPack(fileReference);
      }

    });
    startup = new Startup(new Startup.Context() {

      @Override
      public Path getSharedLibDir() {
        return getConfig().getSharedLibDir().toPath();
      }

      @Override
      public MinecraftServer getServer() {
        return server;
      }

      @Override
      public Logger getLogger() {
        return logger;
      }
    });
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    logger.info("Initializing Wizards-of-Lua, Version " + VERSION);
    MinecraftForge.EVENT_BUS.register(getSpellRegistry());
    MinecraftForge.EVENT_BUS.register(aboutMessage);
    MinecraftForge.EVENT_BUS.register(eventHandler);
  }

  @EventHandler
  public void serverStarting(FMLServerStartingEvent event) throws IOException {
    server = event.getServer();
    worldFileSystem = createWorldFileSystem(server.getDataDirectory(), server.getFolderName());
    gameProfiles = new GameProfiles(server);
    permissions = new Permissions(server);
    event.registerServerCommand(new WolCommand(this, logger));
    event.registerServerCommand(new LuaCommand());
    ChunkLoaderTicketSupport.enableTicketSupport(instance);
    restApiServer.start();
  }

  private FileSystem createWorldFileSystem(File serverDir, String worldFolderName) {
    Path worldDirectory =
        new File(serverDir, worldFolderName).toPath().normalize().toAbsolutePath();
    return new RestrictedFileSystem(FileSystems.getDefault(), worldDirectory);
  }

  @EventHandler
  public void serverStopping(FMLServerStoppingEvent event) {
    restApiServer.stop();
  }

  @EventHandler
  public void serverStarted(FMLServerStartedEvent event) {
    logger.info(aboutMessage);
    runStartupSequence(server);
  }

  public void runStartupSequence(ICommandSender sender) {
    startup.runStartupSequence(sender);
  }

  public WolConfig getConfig() {
    return checkNotNull(config, "config==null!");
  }

  public Profiles getProfiles() {
    return checkNotNull(profiles, "profiles==null!");
  }

  public SpellEntityFactory getSpellEntityFactory() {
    return checkNotNull(spellEntityFactory, "spellEntityFactory==null!");
  }

  public SpellRegistry getSpellRegistry() {
    return checkNotNull(spellRegistry, "spellRegistry==null!");
  }

  public Clock getClock() {
    return clock;
  }

  public void setClock(Clock clock) {
    this.clock = checkNotNull(clock, "clock==null!");
  }

  public Clock getDefaultClock() {
    return Clock.systemDefaultZone();
  }

  public void clearLuaFunctionCache() {
    luaFunctionCache.clear();
  }

  public LuaFileRepository getFileRepository() {
    return checkNotNull(fileRepository, "fileRepository==null!");
  }

  public WolRestApiServer getRestServer() {
    return checkNotNull(restApiServer, "restApiServer==null!");
  }

  public GistRepo getGistRepo() {
    return checkNotNull(gistRepo, "gistRepo==null!");
  }

  public FileSystem getWorldFileSystem() {
    return checkNotNull(worldFileSystem, "worldFileSystem==null!");
  }

  public Permissions getPermissions() {
    return checkNotNull(permissions, "permissions==null!");
  }

}
