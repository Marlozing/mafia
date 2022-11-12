package mafia.mafia;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class game {
    static Plugin plugin = Mafia.getPlugin(Mafia.class);
    static ConsoleCommandSender consol = Mafia.consol;
    public static BukkitTask game;
    public static void day(){
        Bukkit.getWorld("world").setTime(6000);
        Mafia.시간 = 90;
        Mafia.time = 90;
        if(Mafia.직업선택.containsKey("마피아")){
            if(Mafia.직업선택.get("마피아").hasPotionEffect(PotionEffectType.BLINDNESS)){
                Mafia.직업선택.get("마피아").damage(100);
            }
        }
        for(Player p : Bukkit.getOnlinePlayers()){
            if(p.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                p.teleport(new Location(Bukkit.getWorld("world") ,-50 , -59, 10));
            }else{
                p.teleport(new Location(Bukkit.getWorld("world") ,-49 , -59, 32));
            }
        }
        final Boolean[] game1 = {true};
        Mafia.시간선택.clear();
        game = new BukkitRunnable() {
            @Override
            public void run() {
                if(game1[0]) {
                    Mafia.시간 -= 1;
                    if (Mafia.시간 <= 0) {
                        this.cancel();
                        game1[0] = false;
                        Bukkit.getWorld("world").setTime(7000);
                        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                        int num = 0;
                        Mafia.투표창.clear();
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendMessage("투표 시간입니다. 모두 투표해주세요.");
                            Mafia.time = 30;
                            if (!p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                                meta.setOwningPlayer(p);
                                meta.setDisplayName(ChatColor.WHITE + p.getDisplayName());
                                skull.setItemMeta(meta);
                                Mafia.투표창.setItem(num, skull);
                                num += 1;
                            }
                            Mafia.투표.clear();
                        }
                        game = new BukkitRunnable() {
                            @Override
                            public void run() {
                                HashMap<Player, Integer> open = new HashMap<>();
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    open.put(p, 0);
                                }
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    if (Mafia.투표.containsKey(p)) {
                                        open.put(Mafia.투표.get(p), open.get(Mafia.투표.get(p)) + 1);
                                    }
                                }
                                Player kill = null;
                                boolean none = false;
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    if (kill == null) {
                                        kill = p;
                                    } else {
                                        if (open.get(kill) < open.get(p)) {
                                            kill = p;
                                            none = false;
                                        } else if (Objects.equals(open.get(kill), open.get(p))) {
                                            none = true;
                                        }
                                    }
                                    for (Player p1 : Bukkit.getOnlinePlayers()) {
                                        p.sendMessage(p1.getName() + " : " + open.get(p1) + "표");
                                    }
                                }
                                if (none) {
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        p.sendMessage("동점으로 인해 무효되었습니다");
                                        Mafia.처형자 = null;
                                    }
                                    game = new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            night();
                                        }
                                    }.runTaskLater(plugin,20L);
                                }else{
                                    Mafia.처형자 = kill;
                                    Mafia.처형중.clear();
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        p.sendMessage("최후의 말을 듣겠습니다");
                                        p.sendMessage("좌클릭을 눌러 사형 찬성을, 우클릭을 눌러 반대를 선택하세요");
                                    }
                                }
                                Mafia.time = 30;
                                game = new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (Mafia.처형확인 >= 0) {
                                            Mafia.처형자.damage(100);
                                        }
                                        game = new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                night();
                                            }
                                        }.runTaskLater(plugin, 20L);
                                    }
                                }.runTaskLater(plugin, 20 * 30L);
                            }
                        }.runTaskLater(plugin, 20 * 30L);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
    public static void night(){
        Mafia.처형자 = null;
        int M = 0;
        int C = 0;
        int S = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if(!p.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                switch (Mafia.직업.get(p)) {
                    case "마피아":
                        M += 1;
                        break;
                    case "스파이":
                        S += 1;
                        break;
                    default:
                        C += 1;
                        break;
                }
            }

        }
        if (M >= C) {
            gameover();
            Mafia.직업.clear();
            for (Player p1 : Bukkit.getOnlinePlayers()) {
                p1.sendTitle(" ", ChatColor.RED.toString() + ChatColor.BOLD + "MAFIA WIN!", 1, 20, 5);
                p1.removePotionEffect(PotionEffectType.INVISIBILITY);
                p1.getInventory().clear();
            }
            Mafia.time = 0;
            return;
        }
        if (S == C) {
            gameover();
            Mafia.직업.clear();
            for (Player p1 : Bukkit.getOnlinePlayers()) {
                p1.sendTitle(" ", ChatColor.RED.toString() + ChatColor.BOLD + "MAFIA WIN!", 1, 20, 5);
                p1.removePotionEffect(PotionEffectType.INVISIBILITY);
                p1.getInventory().clear();
            }
            Mafia.time = 0;
            return;
        }
        if (M == 0 && S == 0) {
            gameover();
            Mafia.직업.clear();
            for (Player p1 : Bukkit.getOnlinePlayers()) {
                p1.sendTitle(" ", ChatColor.GREEN.toString() + ChatColor.BOLD + "CITIZEN WIN!", 1, 20, 5);
                p1.removePotionEffect(PotionEffectType.INVISIBILITY);
                p1.getInventory().clear();
            }
            Mafia.time = 0;
            return;
        }
        Location loc = new Location(Bukkit.getWorld("world"),15, -58, 10);
        for(Player p : Bukkit.getOnlinePlayers()){
            loc.setZ(loc.getZ() + 8);
            p.teleport(loc);
        }
        Mafia.time = 20;
        Bukkit.getWorld("world").setTime(18000);
        Mafia.직업선택.clear();
        loadlist(Mafia.Minv);
        loadlist(Mafia.Sinv);
        loadlist(Mafia.Dinv);
        loadlist(Mafia.Pinv);
        loadlist(Mafia.Winv);
        ArrayList spy = new ArrayList();
        ArrayList police = new ArrayList();
        for(Player p : Bukkit.getOnlinePlayers()){
            if(Mafia.직업.get(p).equals("스파이")) spy.add(p);
            if(Mafia.직업.get(p).equals("경찰")) police.add(p);
        }
        game = new BukkitRunnable() {
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()){
                    p.closeInventory();
                }
                if(Mafia.직업선택.containsKey("마피아")) {
                    Player p = Mafia.직업선택.get("마피아");
                    p.damage(0.1);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 220, 1, false,false,false));
                    p.sendTitle(" ", ChatColor.DARK_RED + "당신은 마피아에게 살해당했습니다", 4, 85, 4);
                }
                Mafia.time = 9;
            }
        }.runTaskLater(plugin,20 * 20L);
        game = new BukkitRunnable(){
            @Override
            public void run(){
                if(Mafia.직업선택.containsKey("스파이")){
                    if (Mafia.직업.get(Mafia.직업선택.get("스파이")).equals("마피아")) {
                        for (Object p : spy) {
                            ((Player) p).sendMessage(ChatColor.RED + "마피아와 접선했습니다");
                            Mafia.직업.put((Player) p, "마피아");
                        }
                    }else {
                        for (Object p : spy) {
                            ((Player) p).sendMessage(ChatColor.RED + Mafia.직업선택.get("스파이").getName() + "님은 " + Mafia.직업.get(Mafia.직업선택.get("스파이")) + "입니다");
                        }
                    }
                }
                if(Mafia.직업선택.containsKey("경찰")){
                    if (Mafia.직업.get(Mafia.직업선택.get("경찰")).equals("마피아")) {
                        for (Object p : police) {
                            ((Player) p).sendMessage(ChatColor.RED + Mafia.직업선택.get("경찰").getName() + "님은 마피아입니다");
                        }
                    } else {
                        for (Object p : police) {
                            ((Player) p).sendMessage(ChatColor.GREEN + Mafia.직업선택.get("경찰").getName() + "님은 마피아가 아닙니다");
                        }
                    }
                }
                if(Mafia.직업선택.containsKey("의사")){
                    Player p = Mafia.직업선택.get("의사");
                    if(p==Mafia.직업선택.get("마피아")){
                        p.removePotionEffect(PotionEffectType.BLINDNESS);
                        p.sendTitle(" ",ChatColor.GREEN+"당신은 의사에게 구원받았습니다",4,100,4);
                    }
                }

            }
        }.runTaskLater(plugin,20 * 23L);
        game = new BukkitRunnable() {
            @Override
            public void run() {
                if(Mafia.직업선택.containsKey("건달")) {
                    Player p = Mafia.직업선택.get("건달");
                    p.damage(0.1);
                    p.sendTitle(" ", ChatColor.RED + "당신은 건달에게 협박당했습니다.", 4, 100, 4);
                }
            }
        }.runTaskLater(plugin,20 * 26L);
        game = new BukkitRunnable() {
            @Override
            public void run() {
                day();
            }
        }.runTaskLater(plugin,20 * 29L);
    }
    public static void loadlist(Inventory inv){
        inv.clear();
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        int num = 0;
        for(Player p : Bukkit.getOnlinePlayers()){
            if(!p.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                meta.setOwningPlayer(p);
                meta.setDisplayName(ChatColor.WHITE+p.getDisplayName());
                skull.setItemMeta(meta);
                inv.setItem(num,skull);
                num += 1;
            }
        }
    }
    public static void gameover(){
        for(Player p : Bukkit.getOnlinePlayers()){
            for(Player p1 : Bukkit.getOnlinePlayers()) {
                if (p1.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    p.sendMessage(ChatColor.GRAY+"[" + p1.getName() + "] : " + Mafia.직업.get(p1));
                }else{
                    p.sendMessage("[" + p1.getName() + "] : " + Mafia.직업.get(p1));
                }
            }
        }
    }
}
