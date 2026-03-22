package com.sobblemc.sobbleggwave;

import org.bukkit.configuration.file.FileConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for WaveManager.getColorForGG().
 *
 * Strategy: use reflection to set private waveSize, and refreshConfig() to
 * reload cached colors from mocked config.
 *
 * Color index formula:  colorIndex = ((ggNumber - 1) * numColors) / waveSize
 * Clamp:               if (colorIndex >= numColors) colorIndex = numColors - 1
 */
@RunWith(MockitoJUnitRunner.class)
public class WaveManagerColorTest {

    @Mock
    private SobbleGGWavePlugin mockPlugin;

    @Mock
    private FileConfiguration mockConfig;

    private WaveManager waveManager;

    @Before
    public void setUp() throws Exception {
        Mockito.when(mockPlugin.getConfig()).thenReturn(mockConfig);

        // Stub defaults so refreshConfig() in constructor doesn't get nulls
        Mockito.when(mockConfig.getString("trigger-word", "gg")).thenReturn("gg");
        Mockito.when(mockConfig.getStringList("colors")).thenReturn(Collections.<String>emptyList());
        Mockito.when(mockConfig.getString("prefix", "")).thenReturn("");
        Mockito.when(mockConfig.getString("gg-format", "{color}{player}: GG!")).thenReturn("{color}{player}: GG!");
        Mockito.when(mockConfig.getString("messages.wave-start", "")).thenReturn("");
        Mockito.when(mockConfig.getString("messages.wave-end-count", "")).thenReturn("");
        Mockito.when(mockConfig.getString("messages.wave-end-timeout", "")).thenReturn("");
        Mockito.when(mockConfig.getString("messages.first-gg", "")).thenReturn("");
        Mockito.when(mockConfig.getString("messages.last-gg", "")).thenReturn("");
        Mockito.when(mockConfig.getString("messages.cooldown", "")).thenReturn("");

        waveManager = new WaveManager(mockPlugin);
    }

    private void setWaveSize(int size) throws Exception {
        Field field = WaveManager.class.getDeclaredField("waveSize");
        field.setAccessible(true);
        field.set(waveManager, size);
    }

    private void setCachedColors(List<String> colors) {
        Mockito.when(mockConfig.getStringList("colors")).thenReturn(colors);
        waveManager.refreshConfig();
    }

    private List<String> colorList(int count) {
        String[] colors = new String[count];
        for (int i = 0; i < count; i++) {
            colors[i] = "&color" + i;
        }
        return Arrays.asList(colors);
    }

    /**
     * wave=20, colors=6, gg=1
     * colorIndex = (0 * 6) / 20 = 0
     */
    @Test
    public void getColorForGG_firstGG_returnsFirstColor() throws Exception {
        setCachedColors(colorList(6));
        setWaveSize(20);

        Assert.assertEquals("&color0", waveManager.getColorForGG(1));
    }

    /**
     * wave=20, colors=6, gg=20
     * colorIndex = (19 * 6) / 20 = 5
     */
    @Test
    public void getColorForGG_lastGG_returnsLastColor() throws Exception {
        setCachedColors(colorList(6));
        setWaveSize(20);

        Assert.assertEquals("&color5", waveManager.getColorForGG(20));
    }

    /**
     * wave=20, colors=6, gg=10
     * colorIndex = (9 * 6) / 20 = 2
     */
    @Test
    public void getColorForGG_middleGG_returnsCorrectColor() throws Exception {
        setCachedColors(colorList(6));
        setWaveSize(20);

        Assert.assertEquals("&color2", waveManager.getColorForGG(10));
    }

    /**
     * Single color — every GG should return the same color.
     */
    @Test
    public void getColorForGG_singleColor_alwaysReturnsSame() throws Exception {
        setCachedColors(Collections.singletonList("&aSingleColor"));
        setWaveSize(20);

        for (int gg = 1; gg <= 20; gg++) {
            Assert.assertEquals("Expected single color for gg=" + gg,
                    "&aSingleColor", waveManager.getColorForGG(gg));
        }
    }

    /**
     * Empty colors list — must return hard-coded default "&f&l".
     */
    @Test
    public void getColorForGG_emptyColors_returnsDefault() throws Exception {
        setCachedColors(Collections.<String>emptyList());
        setWaveSize(20);

        Assert.assertEquals("&f&l", waveManager.getColorForGG(1));
    }

    /**
     * More colors than wave size — indices must stay valid.
     * wave=3, colors=6: gg1→0, gg2→2, gg3→4
     */
    @Test
    public void getColorForGG_moreColorsThanWaveSize_clampsCorrectly() throws Exception {
        setCachedColors(colorList(6));
        setWaveSize(3);

        Assert.assertEquals("&color0", waveManager.getColorForGG(1));
        Assert.assertEquals("&color2", waveManager.getColorForGG(2));
        Assert.assertEquals("&color4", waveManager.getColorForGG(3));
    }

    /**
     * Equal colors and wave size — one color per GG, sequential.
     * wave=6, colors=6: gg=N → index N-1
     */
    @Test
    public void getColorForGG_equalColorsAndWaveSize_onePerGG() throws Exception {
        setCachedColors(colorList(6));
        setWaveSize(6);

        for (int gg = 1; gg <= 6; gg++) {
            Assert.assertEquals("Expected index " + (gg - 1) + " for gg=" + gg,
                    "&color" + (gg - 1), waveManager.getColorForGG(gg));
        }
    }
}
