package net.karneim.luamod.lua.util.wrapper;

import net.karneim.luamod.lua.util.table.DelegatingTable;
import net.sandius.rembulan.Table;

public abstract class DelegatingTableWrapper<D> implements LuaWrapper<D, DelegatingTable<D>> {
  @Override
  public final DelegatingTable<D> toLuaObject(D delegate) {
    DelegatingTable.Builder<D> builder = DelegatingTable.builder(delegate);
    addProperties(builder, delegate);
    builder.setMetatable(getMetatable());
    return builder.build();
  }

  protected abstract Table getMetatable();

  protected abstract void addProperties(DelegatingTable.Builder<D> builder, D delegate);
}
