package net.wizardsoflua.tests;

import org.junit.Test;
import org.junit.runner.RunWith;

import net.wizardsoflua.testenv.MinecraftJUnitRunner;
import net.wizardsoflua.testenv.WolTestBase;
import net.wizardsoflua.testenv.event.ServerLog4jEvent;
import net.wizardsoflua.testenv.event.TestPlayerReceivedChatEvent;
import net.wizardsoflua.testenv.net.ChatAction;

@RunWith(MinecraftJUnitRunner.class)
public class LuaCommandTest extends WolTestBase {

  // /test net.wizardsoflua.tests.LuaCommandTest test_player_can_print_some_text
  @Test
  public void test_player_can_print_some_text() throws Exception {
    // Given:
    String text = "some text";

    // When:
    mc().player().chat("/lua print('%s')", text);

    // Then:
    TestPlayerReceivedChatEvent act = mc().waitFor(TestPlayerReceivedChatEvent.class);
    assertThat(act.getMessage()).isEqualTo(text);
  }

  // /test net.wizardsoflua.tests.LuaCommandTest test_player_can_print_some_calculation
  @Test
  public void test_player_can_print_some_calculation() throws Exception {
    // Given:
    String text = "13 * 7";

    // When:
    mc().player().chat("/lua print(%s)", text);

    // Then:
    TestPlayerReceivedChatEvent act = mc().waitFor(TestPlayerReceivedChatEvent.class);
    assertThat(act.getMessage()).isEqualTo("91");
  }

  // /test net.wizardsoflua.tests.LuaCommandTest test_player_can_use_for_loop
  @Test
  public void test_player_can_use_for_loop() throws Exception {
    // Given:

    // When:
    mc().player().chat("/lua for i=1,10 do print(i); end");

    // Then:
    for (int i = 0; i < 10; ++i) {
      TestPlayerReceivedChatEvent act = mc().waitFor(TestPlayerReceivedChatEvent.class);
      assertThat(act.getMessage()).isEqualTo(String.valueOf(i + 1));
    }
  }

  // /test net.wizardsoflua.tests.LuaCommandTest test_server_can_print_some_text
  @Test
  public void test_server_can_print_some_text() throws Exception {
    // Given:
    String text = "hello server!";

    // When:
    mc().executeCommand("/lua print('%s')", text);

    // Then:
    ServerLog4jEvent act = mc().waitFor(ServerLog4jEvent.class);
    assertThat(act.getMessage()).isEqualTo(text);
  }

}
