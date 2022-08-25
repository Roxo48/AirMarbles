package me.roxo.airmarbles;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.*;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AirMarbles extends AirAbility implements AddonAbility,ComboAbility {
    private static  int MARBLES ;
    private static long COOLDOWN ;
    private static double SPEED;
    private Permission perm;
    private Listener listener;

    private final Location location;
    private States state;
    private int a;
    private int b;

    private ArrayList<Slime> slimes;
    private boolean isSlimeTime;
    private int count;
    private ArrayList<LivingEntity> entities1 ;
    private int time;
    private static int TIME_OF_SCOOCTERS;

    public AirMarbles(Player player) {
        super(player);
        location = player.getLocation();
        setFields();
        a=20;
        b = 0;
        count = 0;
        isSlimeTime = false;
        state = States.PRESTART;
        entities1 = new ArrayList<>();
        slimes = new ArrayList<>();
        time = 0;
        if (player.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            this.isSlimeTime = true;
        }
        start();
    }

      private void setFields() {
        COOLDOWN = ConfigManager.getConfig().getLong("AirMarbles.COOLDOWN");
        SPEED = ConfigManager.getConfig().getDouble("AirMarbles.SPEED");
        MARBLES = ConfigManager.getConfig().getInt("AirMarbles.MARBLES");
        TIME_OF_SCOOCTERS = ConfigManager.getConfig().getInt("AirMarbles.TIME_OF_SCOOCTERS");
    }

    @Override
    public void progress() {
        if (this.player.isDead() || !this.player.isOnline()) {
            this.remove();
            return;
        }
        if (!CoreAbility.hasAbility(player, this.getClass())) {return;}
        if (bPlayer.isOnCooldown(this)){ remove(); return;}
        switch (state) {

            case PRESTART:
                livingEntities();
                Location handLocation = GeneralMethods.getMainHandLocation(player);
                Objects.requireNonNull(handLocation.getWorld()).spawnParticle(Particle.REDSTONE, handLocation, MARBLES, new Particle.DustOptions(Color.fromBGR(255, 255, 255), 1));
                state = States.START;
                break;
            case START:
                Location handLocation3 = GeneralMethods.getMainHandLocation(player);

                    BukkitRunnable br = new BukkitRunnable() {
                        @Override
                        public void run() {
                            a--;
                            b++;
                            if ( b >= 20) {
                                state = States.MIDDLE;
                                cancel();
                            }
                            for(int m = 0; m < MARBLES; m++) {
                                if(entities1 == null || m >= entities1.size()){
                                    continue;
                                }
                                    float a1 = (float) b / 20;
                                   Location location1 = bezierPoint(a1,handLocation3, handLocation3.add(0,0,0), entities1.get(m).getLocation());
                                    for (double i = 0; i <= Math.PI; i += Math.PI / 10) {
                                        double radius = Math.sin(i) / a;
                                        double y = Math.cos(i) / a;
                                        for (double j = 0; j < Math.PI * 2; j += Math.PI / 10) {
                                            double x = Math.cos(j) * radius;
                                            double z = Math.sin(j) * radius;
                                            location1.add(x, y, z);
                                            getAirbendingParticles().display(location1, 1);
                                            location1.subtract(x, y, z);
                                        }
                                    }
                            }
                        }
                    };
                    br.runTaskTimer(ProjectKorra.plugin, 0, 2);

                state = States.NULL;
                break;
            case MIDDLE:
                for(int m = 0; m < MARBLES; m++) {
                    if(entities1 == null || m >= entities1.size()){
                        continue;
                    }
                    Vector velocity = entities1.get(m).getEyeLocation().getDirection().clone().normalize();
                    if (count % 2 == 0) {
                        velocity.setY(-.25);
                        if (getFloor(entities1.get(m))) {

                           state = States.END;
                           return;
                        }
                        Location location1 = entities1.get(m).getLocation();
                        for (double i = 0; i <= Math.PI; i += Math.PI / 10) {
                            double radius = Math.sin(i);
                            double y = Math.cos(i) - 1;
                            for (double j = 0; j < Math.PI * 2; j += Math.PI / 10) {
                                double x = Math.cos(j) * radius;
                                double z = Math.sin(j) * radius;
                                location1.add(x, y, z);
                                getAirbendingParticles().display(location1, 1);
                                location1.subtract(x, y, z);
                            }
                        }
                    }
                    velocity = velocity.clone().normalize().multiply(SPEED);
                    if(entities1.get(m).getLocation().clone().add(0,-1.5,0).getBlock().getType() != Material.AIR){
                       velocity.setY(.25);
                    }
                    GeneralMethods.setVelocity(this, entities1.get(m), velocity);
                }

                isSlimeTime = true;
                if(count == 0) {
                    BukkitRunnable br2 = new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (time >= TIME_OF_SCOOCTERS) {
                                state = States.END;
                                cancel();
                            }

                            time++;

                        }
                    };
                    br2.runTaskTimer(ProjectKorra.plugin, 20, 20);
                }
                count++;
                break;
            case END:
                    bPlayer.addCooldown(this);
                    remove();
                state = States.NULL;
                break;

                }
        }

    private boolean getFloor(Entity entity) {
        return entity.getLocation().add(0, -5, 0).getBlock().getType() == Material.AIR;
    }

    public Location bezierPoint(float t, Location p0, Location p1, Location p2){
        /*
        *pFinal[0] = Math.pow(1 - t, 2) * p0[0] + (1-t) * 2 * t * p1[0] + t * t * p2[0];
        *pFinal[1] = Math.pow(1 - t, 2) * p0[1] + (1-t) * 2 * t * p1[1] + t * t * p2[1];
         */
        return p0.clone().multiply((1-t)*(1-t)).add(p1.clone().multiply((1-t) * 2 * t)).add(p2.clone().multiply(t*t));
    }

    public void  livingEntities() {
        for (int i = 0; i < MARBLES; i++) {
            List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(player.getLocation(), 20);
            entities1 = new ArrayList<>();
            for (Entity target : entities) {
                if (target.getUniqueId() == player.getUniqueId()) {
                    continue;
                }
                if (target.getType() == EntityType.SLIME) {
                    continue;
                }


                if (target instanceof LivingEntity) {
                    entities1.add((LivingEntity) target);
                }
            }
        }
        for (int i = 0; i < entities1.size(); i++) {
            for (int j = i + 1; j < entities1.size(); j++) {
                if (entities1.get(i).getUniqueId().equals(entities1.get(j).getUniqueId())) {
                    entities1.remove(entities1.get(i));

                }
            }
        }

    }


    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return COOLDOWN;
    }

    @Override
    public String getName() {
        return "AirMarbles";
    }

    @Override
    public String getDescription(){
        return "Elements Of The Avatar Addons:\n" +
                "As Demonstrated by Aang, you Manipulate the Air to form multi Marbles to blow at the Entities giving them a ride. \n" +
                "AirSuction (Left Click) > AirScooter (Left Click) > AirBreath (Hold Shift)";
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void load() {
        perm = new Permission("bending.ability.AirMarbles");
        ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
        listener = new Listener();
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);
        final FileConfiguration config = ConfigManager.defaultConfig.get();
        config.addDefault("AirMarbles.COOLDOWN", 20000);
        config.addDefault("AirMarbles.SPEED",  .2);
        config.addDefault("AirMarbles.MARBLES",  6);
        config.addDefault("AirMarbles.TIME_OF_SCOOCTERS", 10);
        ConfigManager.defaultConfig.save();
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(listener);
        ProjectKorra.plugin.getServer().getPluginManager().removePermission(perm);
    }

    @Override
    public String getAuthor() {
        return "Roxo";
    }

    @Override
    public String getVersion() {
        return "1.1";
    }

    @Override
    public Object createNewComboInstance(Player player) {
        return new AirMarbles(player);
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        final ArrayList<ComboManager.AbilityInformation> airwake = new ArrayList<>();
        airwake.add(new ComboManager.AbilityInformation("AirSuction", ClickType.LEFT_CLICK));
        airwake.add(new ComboManager.AbilityInformation("AirScooter", ClickType.LEFT_CLICK));
        airwake.add(new ComboManager.AbilityInformation("AirBreath", ClickType.SHIFT_DOWN));
        return airwake;
    }
}
