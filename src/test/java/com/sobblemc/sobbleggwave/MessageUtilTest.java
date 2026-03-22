package com.sobblemc.sobbleggwave;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for MessageUtil.
 * No Bukkit server needed — ChatColor.translateAlternateColorCodes is pure string logic.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageUtilTest {

    // -------------------------------------------------------------------------
    // colorize()
    // -------------------------------------------------------------------------

    @Test
    public void colorize_withNull_returnsEmpty() {
        String result = MessageUtil.colorize(null);
        Assert.assertEquals("", result);
    }

    @Test
    public void colorize_withColorCodes_translates() {
        // &c (red) must become §c
        String result = MessageUtil.colorize("&cHello");
        Assert.assertEquals("\u00a7cHello", result);
    }

    @Test
    public void colorize_withNoColorCodes_returnsUnchanged() {
        String result = MessageUtil.colorize("Hello World");
        Assert.assertEquals("Hello World", result);
    }

    // -------------------------------------------------------------------------
    // replace()
    // -------------------------------------------------------------------------

    @Test
    public void replace_withNull_returnsEmpty() {
        String result = MessageUtil.replace(null, "player", "Steve");
        Assert.assertEquals("", result);
    }

    @Test
    public void replace_withPlaceholder_replaces() {
        String result = MessageUtil.replace("Hello {player}!", "player", "Steve");
        Assert.assertEquals("Hello Steve!", result);
    }

    @Test
    public void replace_withMissingPlaceholder_returnsUnchanged() {
        String result = MessageUtil.replace("Hello {player}!", "name", "Steve");
        Assert.assertEquals("Hello {player}!", result);
    }
}
