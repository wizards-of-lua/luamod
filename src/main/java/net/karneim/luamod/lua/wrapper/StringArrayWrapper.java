package net.karneim.luamod.lua.wrapper;

import javax.annotation.Nullable;

import net.sandius.rembulan.impl.ImmutableTable;

public class StringArrayWrapper extends StructuredLuaWrapper<String[]> {
  public StringArrayWrapper(@Nullable String[] delegate) {
    super(delegate);
  }

  @Override
  protected void toLuaObject(ImmutableTable.Builder builder) {
    super.toLuaObject(builder);
    int idx = 0;
    for (String value : delegate) {
      idx++;
      builder.add(idx, value);
    }
  }
}
