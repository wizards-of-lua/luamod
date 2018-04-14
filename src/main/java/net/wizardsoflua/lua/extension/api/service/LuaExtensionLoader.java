package net.wizardsoflua.lua.extension.api.service;

import javax.annotation.Nullable;

import net.wizardsoflua.lua.extension.spi.LuaExtension;

public interface LuaExtensionLoader {
  @Nullable
  <M extends LuaExtension> M getLuaExtension(Class<M> moduleType);
}