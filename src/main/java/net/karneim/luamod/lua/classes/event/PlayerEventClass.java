package net.karneim.luamod.lua.classes.event;

import net.karneim.luamod.lua.classes.AbstractLuaType;
import net.karneim.luamod.lua.classes.Constants;
import net.karneim.luamod.lua.classes.ModulePackage;
import net.karneim.luamod.lua.classes.TypeName;
import net.karneim.luamod.lua.event.EventType;
import net.karneim.luamod.lua.event.Player2EventWrapper;
import net.karneim.luamod.lua.wrapper.Metatables;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@TypeName("PlayerEvent")
@ModulePackage(Constants.MODULE_PACKAGE)
public class PlayerEventClass extends AbstractLuaType {
  public Player2EventWrapper newInstance(PlayerEvent delegate, EventType eventType) {
    return new Player2EventWrapper(getRepo(), delegate, eventType,
        Metatables.get(getRepo().getEnv(), getTypeName()));
  }

  @Override
  protected void addFunctions() {}
}
