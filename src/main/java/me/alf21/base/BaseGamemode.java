package me.alf21.base;

import net.gtaun.shoebill.constant.PlayerMarkerMode;
import net.gtaun.shoebill.entities.*;
import net.gtaun.shoebill.resource.Gamemode;
import net.gtaun.util.event.EventManager;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;

public class BaseGamemode extends Gamemode {

    private PlayerController playerController;
    private Timer timer;

    @Override
    protected void onEnable() throws Throwable {
        EventManager eventManager = getEventManager();

        Server server = Server.get();
        World world = World.get();

        server.setGamemodeText(getDescription().getName());
        world.showPlayerMarkers(PlayerMarkerMode.GLOBAL);
        world.showNameTags(true);
        world.enableStuntBonusForAll(false);

        timer = Timer.create(5000, (factualInterval) ->
        {
            for (Player player : Player.get()) player.setScore(player.getMoney());
        });
        timer.start();

        Pickup.create(371, 15, 1710.3359f, 1614.3585f, 10.1191f, 0);
        Pickup.create(371, 15, 1964.4523f, 1917.0341f, 130.9375f, 0);
        Pickup.create(371, 15, 2055.7258f, 2395.8589f, 150.4766f, 0);
        Pickup.create(371, 15, 2265.0120f, 1672.3837f, 94.9219f, 0);
        Pickup.create(371, 15, 2265.9739f, 1623.4060f, 94.9219f, 0);

        playerController = new PlayerController(eventManager);

        File playerClassFile = new File(getDataDir(), "class.txt");
        loadPlayerClass(playerClassFile); //Load player classes from a file called class.txt that is located in the data directory.

        File vehicleFilesDir = new File(getDataDir(), "vehicles/");
        //Load all vehicles from all files that are located in the vehicles directory in the data directory.
        if (vehicleFilesDir.isDirectory()) loadVehicle(vehicleFilesDir);
    }

    @Override
    protected void onDisable() throws Throwable {
        playerController.destroy();
    }

    private void loadPlayerClass(File file) {
        logger().info("loading " + file);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")))) {
            int count = 0;
            while (reader.ready()) {
                String line = reader.readLine().trim();
                if (StringUtils.isBlank(line)) continue;

                line = StringUtils.split(line, ';')[0];

                String[] paramArray = line.split("[, ]");
                if (paramArray.length != 11) continue;

                Queue<String> params = new ArrayDeque<>();
                Collections.addAll(params, paramArray);

                try {
                    int modelId = Integer.parseInt(params.poll());
                    float x = Float.parseFloat(params.poll());
                    float y = Float.parseFloat(params.poll());
                    float z = Float.parseFloat(params.poll());
                    float angle = Float.parseFloat(params.poll());
                    int weapon1 = Integer.parseInt(params.poll());
                    int ammo1 = Integer.parseInt(params.poll());
                    int weapon2 = Integer.parseInt(params.poll());
                    int ammo2 = Integer.parseInt(params.poll());
                    int weapon3 = Integer.parseInt(params.poll());
                    int ammo3 = Integer.parseInt(params.poll());
                    World.get().addPlayerClass(modelId, x, y, z, angle, weapon1, ammo1, weapon2, ammo2, weapon3, ammo3);

                    count++;
                } catch (NumberFormatException e) {
                    logger().info("Skip: " + line);
                }
            }

            logger().info("Created " + count + " classes.");
        } catch (IOException e) {
            logger().info("Can't initialize classes, please check your " + file);
        }
    }

    private void loadVehicle(File dir) {
        File files[] = dir.listFiles();

        int count = 0;
        if (files != null) {
            for (File file : files) {
                logger().info("loading " + file);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")))) {
                    while (reader.ready()) {
                        String line = reader.readLine();
                        if (StringUtils.isBlank(line)) continue;

                        line = StringUtils.split(line, ';')[0];

                        String[] paramArray = line.split("[, ]");
                        if (paramArray.length != 7) continue;

                        Queue<String> params = new ArrayDeque<>();
                        Collections.addAll(params, paramArray);

                        try {
                            int modelId = Integer.parseInt(params.poll());
                            float x = Float.parseFloat(params.poll());
                            float y = Float.parseFloat(params.poll());
                            float z = Float.parseFloat(params.poll());
                            float angle = Float.parseFloat(params.poll());
                            int color1 = Integer.parseInt(params.poll());
                            int color2 = Integer.parseInt(params.poll());
                            Vehicle.create(modelId, x, y, z, (int) angle, color1, color2, 0);

                            count++;
                        } catch (NumberFormatException e) {
                            logger().info("Skip: " + line);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Can't initialize vehicles, please check your " + file + " file.");
                }
            }
        }

        System.out.println("Created " + count + " vehicles.");
    }
}
