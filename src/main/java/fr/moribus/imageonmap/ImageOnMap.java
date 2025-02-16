/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2021)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2021)
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */

package fr.moribus.imageonmap;


import fr.moribus.imageonmap.commands.Commands;
import fr.moribus.imageonmap.commands.maptool.DeleteCommand;
import fr.moribus.imageonmap.commands.maptool.ExploreCommand;
import fr.moribus.imageonmap.commands.maptool.GetCommand;
import fr.moribus.imageonmap.commands.maptool.GetRemainingCommand;
import fr.moribus.imageonmap.commands.maptool.GiveCommand;
import fr.moribus.imageonmap.commands.maptool.ListCommand;
import fr.moribus.imageonmap.commands.maptool.NewCommand;
import fr.moribus.imageonmap.commands.maptool.RenameCommand;
import fr.moribus.imageonmap.commands.maptool.UpdateCommand;
import fr.moribus.imageonmap.gui.Gui;
import fr.moribus.imageonmap.i18n.I18n;
import fr.moribus.imageonmap.image.MapInitEvent;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.ui.MapItemManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

public final class ImageOnMap extends JavaPlugin {

    private static ImageOnMap PLUGIN;

    private final Path mapsDirectory;
    private final Path imagesDirectory;

    public ImageOnMap() {
        PLUGIN = this;

        var folder = getDataFolder().toPath();
        mapsDirectory = folder.resolve("maps");
        imagesDirectory = folder.resolve("images");
    }

    public static ImageOnMap getPlugin() {
        return PLUGIN;
    }

    public Path getImagesDirectory() {
        return imagesDirectory;
    }

    public Path getMapsDirectory() {
        return mapsDirectory;
    }

    public Path getImageFile(int mapID) {
        return imagesDirectory.resolve("map" + mapID + ".png");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onEnable() {
        // Creating the images and maps directories if necessary
        try {
            checkPluginDirectory(mapsDirectory);
            checkPluginDirectory(imagesDirectory);
        } catch (final IOException ex) {
            getLogger().log(Level.SEVERE, "FATAL: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        Gui.clearOpenGuis();

        JarFile jarFile = getJarFile();

        try {
            I18n.onEnable(jarFile);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    ImageOnMap.getPlugin().getLogger().log(Level.SEVERE, "Unable to close JAR file " + getFile().getAbsolutePath(), e);
                }
            }
        }

        //Init all the things !
        I18n.setPrimaryLocale(PluginConfiguration.LANG.get());

        MapManager.init();
        MapInitEvent.init();
        MapItemManager.init();


        Commands.register(
                "maptool",
                NewCommand.class,
                ListCommand.class,
                GetCommand.class,
                RenameCommand.class,
                DeleteCommand.class,
                GiveCommand.class,
                GetRemainingCommand.class,
                ExploreCommand.class,
                UpdateCommand.class
        );

        Commands.registerShortcut("maptool", NewCommand.class, "tomap");
        Commands.registerShortcut("maptool", ExploreCommand.class, "maps");
        Commands.registerShortcut("maptool", GiveCommand.class, "givemap");
    }

    @Override
    public void onDisable() {
        MapManager.exit();
        MapItemManager.exit();

        Gui.clearOpenGuis();
    }

    private void checkPluginDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            Files.createDirectories(directory);
        }
    }

    /**
     * Gets the .jar file this plugin is loaded by, or null if it wasn't found.
     */
    public JarFile getJarFile() {
        try {
            return new JarFile(getFile());
        } catch (IOException e) {
            ImageOnMap.getPlugin().getLogger().log(Level.SEVERE, "Unable to load JAR file " + getFile().getAbsolutePath(), e);
            return null;
        }
    }
}
