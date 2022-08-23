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

    private static int TIMING ;
    private static  int MARBLES ;
    private static double RANGE ;
    private static long COOLDOWN ;
    private static double SPEED;
    private Permission perm;
    private Listener listener;

    private final Location location;
    private States state;
    private int a;
    private int b;

    private Slime slime;
    private boolean isSlimeTime;
    private int count;

    public AirMarbles(Player player) {
        super(player);
        location = player.getLocation();
        setFields();
        a=20;
        b = 0;
        count = 0;
        isSlimeTime = false;
        state = States.PRESTART;
        start();
    }

      private void setFields() {
        RANGE = ConfigManager.getConfig().getDouble("AirMarbles.RANGE");
        COOLDOWN = ConfigManager.getConfig().getLong("AirMarbles.COOLDOWN");
        SPEED = ConfigManager.getConfig().getDouble("AirMarbles.SPEED");
        MARBLES = ConfigManager.getConfig().getInt("AirMarbles.MARBLES");
        TIMING = ConfigManager.getConfig().getInt("AirMarbles.TIMING");
    }
    @Override
    public void progress() {
        if (this.player.isDead() || !this.player.isOnline()) {
            this.remove();
            return;
        }
        if (!CoreAbility.hasAbility(player, this.getClass())) {return;}
        if (bPlayer.isOnCooldown(this)){ remove(); return;}
        if(slime != null) {
            if (!this.slime.getPassengers().contains(this.player) && isSlimeTime) {
                player.setSprinting(true);
                player.setSneaking(true);
                slime.damage(20);
                bPlayer.addCooldown(this);
                remove();
            }
        }
        switch (state) {

            case PRESTART:
                Bukkit.getServer().broadcastMessage("prestart");

                Location handLocation = GeneralMethods.getMainHandLocation(player);
                Objects.requireNonNull(handLocation.getWorld()).spawnParticle(Particle.REDSTONE, handLocation, MARBLES, new Particle.DustOptions(Color.fromBGR(255, 255, 255), 1));
                state = States.START;
                break;
            case START:

                    Bukkit.getServer().broadcastMessage("Start");

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
//                            if (a >= 17) {
//                                Location handLocation = GeneralMethods.getMainHandLocation(player);
//                                Objects.requireNonNull(handLocation.getWorld()).spawnParticle(Particle.REDSTONE, handLocation, MARBLES, new Particle.DustOptions(Color.fromBGR(255, 255, 255), 1));
//
//                            } else {
                                    float a1 = (float) b / 20;
                                   Location location1 = bezierPoint(a1,handLocation3, handLocation3.add(0,0,0), livingEntities().get(0).getLocation());
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
                            //}


                            //TODO WHen done kill the slime in all places.
                        }
                    };
                    br.runTaskTimer(ProjectKorra.plugin, 0, 2);

                state = States.NULL;
                break;
            case MIDDLE:


                if(count == 0) {

                    this.flightHandler.createInstance(player, this.getName());
                    player.setAllowFlight(true);
                    player.setFlying(true);

                    player.setSprinting(false);
                    player.setSneaking(false);
                    this.slime = (Slime) player.getWorld().spawnEntity(player.getLocation(), EntityType.SLIME);
                    if (this.slime != null) {
                        this.slime.setSize(1);
                        this.slime.setSilent(true);
                        this.slime.setInvulnerable(true);
                        this.slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false));
                        this.slime.addPassenger(player);
                    }
                }


                Vector velocity = this.player.getEyeLocation().getDirection().clone().normalize();
                if(count % 2 == 0){
                    velocity.setY(-.25);
                    if(getFloor()){
                        bPlayer.addCooldown(this);
                        remove();
                        return;
                    }
                    Location location1 = player.getLocation();
                    for (double i = 0; i <= Math.PI; i += Math.PI / 10) {
                        double radius = Math.sin(i) ;
                        double y = Math.cos(i) ;
                        for (double j = 0; j < Math.PI * 2; j += Math.PI / 10) {
                            double x = Math.cos(j) * radius;
                            double z = Math.sin(j) * radius;
                            location1.add(x, y, z);
                            getAirbendingParticles().display(location1, 1);
                            location1.subtract(x, y, z);
                        }
                    }
                }
                velocity = velocity.clone().normalize().multiply(.5);
                GeneralMethods.setVelocity(this, this.slime, velocity);
                    count++;
                isSlimeTime = true;
                break;
            case END:



                break;

                }
        }

    private boolean getFloor() {
        boolean b1 = false;
        if(player.getLocation().add(0,-5,0).getBlock().getType() == Material.AIR){
            b1 = true;
            return b1;
        }

        return b1;
    }

    public Location bezierPoint(float t, Location p0, Location p1, Location p2){
        // pFinal[0] = Math.pow(1 - t, 2) * p0[0] + (1-t) * 2 * t * p1[0] + t * t * p2[0];
        // pFinal[1] = Math.pow(1 - t, 2) * p0[1] + (1-t) * 2 * t * p1[1] + t * t * p2[1];
        return p0.clone().multiply((1-t)*(1-t)).add(p1.clone().multiply((1-t) * 2 * t)).add(p2.clone().multiply(t*t));
    }

    public ArrayList<LivingEntity> livingEntities(){
        List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(player.getLocation(), 20);
        ArrayList<LivingEntity> entities1 = new ArrayList<>();
        for (Entity target : entities) {
            if (target.getUniqueId() == player.getUniqueId()) {
                continue;
            }
            if (target instanceof LivingEntity) {
                entities1.add((LivingEntity) target);
            }
        }
        return entities1;
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
        return 1000;
    }

    @Override
    public String getName() {
        return "AirMarbles";
    }

    @Override
    public String getDescription(){
        return "Elements Of The Avatar Addons:\n" +
                "As Demonstrated by Aang, you Manipulate the Air to form multi Marbles to blow at the Entities giving them a ride. ";
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
        config.addDefault("AirMarbles.RANGE", 40);
        config.addDefault("AirMarbles.COOLDOWN", 20000);
        config.addDefault("AirMarbles.SPEED",  .5);
        config.addDefault("AirMarbles.MARBLES",  6);
        config.addDefault("AirMarbles.TIMING",  20);
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
        //airwake.add(new ComboManager.AbilityInformation("AirSwipe", ClickType.SHIFT_DOWN));
        return airwake;
    }
}
