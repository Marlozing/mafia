package mafia.mafia;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class Mafia extends JavaPlugin implements Listener {
    static ConsoleCommandSender consol = Bukkit.getConsoleSender();
    FileConfiguration config = getConfig();
    HashMap<String, Integer> jobnumber = new HashMap<>();
    static HashMap<Player, String> 직업 = new HashMap<>();
    static Integer time = 0;
    static Player 처형자 = null;
    static Integer 처형확인 = 0;
    static Integer 시간 = 0;
    static Inventory Minv = Bukkit.createInventory(null, 54, "능력 실행창");
    static Inventory Sinv = Bukkit.createInventory(null, 54, "능력 실행창");
    static Inventory Dinv = Bukkit.createInventory(null, 54, "능력 실행창");
    static Inventory Pinv = Bukkit.createInventory(null, 54, "능력 실행창");
    static Inventory Winv = Bukkit.createInventory(null, 54, "능력 실행창");
    static Inventory 투표창 = Bukkit.createInventory(null, 54, "투표창");
    Inventory 낮 = Bukkit.createInventory(null, 9, "시간 선택");
    static HashMap<String, Boolean> 설정 = new HashMap<>();
    static HashMap<Player, Boolean> 처형중 = new HashMap<>();
    static HashMap<Player, Player> 투표 = new HashMap<>();
    static HashMap<String, Player> 직업선택 = new HashMap<>();
    static HashMap<Player, Boolean> 시간선택 = new HashMap<>();
    @Override
    public void onEnable() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW+"시간 연장");
        item.setItemMeta(meta);
        낮.setItem(2,item);
        meta.setDisplayName(ChatColor.GRAY+"시간 단축");
        item.setItemMeta(meta);
        낮.setItem(6,item);
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(this.getCommand("게임")).setExecutor(new command());
        consol.sendMessage("서버가 켜졌습니다");
        설정.put("직업",false);
        new BukkitRunnable() {
            @Override
            public void run() {
                if(time>0){
                    time -= 1;
                    for(Player p : Bukkit.getOnlinePlayers()){
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("남은 시간 : " + time+"초"));
                    }
                }
            }
        }.runTaskTimer(this,20L,20L);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void join(PlayerJoinEvent a){
        new BukkitRunnable(){
            @Override
            public void run() {
                a.getPlayer().teleport(new Location(Bukkit.getWorld("world") ,-49 , -59, 32));
                a.getPlayer().setGameMode(GameMode.ADVENTURE);
                a.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 999999999, 1, false,false,false));
            }
        }.runTaskLater(this,1L);
    }
    class command implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            Player p;
            if (sender instanceof Player) {
                TextComponent msg;
                p = (Player) sender;
                ArrayList list = new ArrayList();
                if(args.length==0){
                    list.add("설정");
                    list.add("설명");
                    list.add("시작");
                    list.add("중지");
                    for(Object str : list){
                        msg = new TextComponent("/게임 " + str);
                        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/게임 " + str));
                        p.spigot().sendMessage(msg);
                    }
                }else{
                    switch (args[0]) {
                        case "설정": {
                            jobnumber.remove("시민");
                            load(list);
                            if(args.length>=2){
                                if(args[1].equals("직업")){
                                    if(설정.get(args[1])){
                                        설정.put(args[1],false);
                                    }else{
                                        설정.put(args[1],true);
                                    }
                                }
                                for (ChatColor chatColor : Arrays.asList(ChatColor.RED, ChatColor.GREEN)) {
                                    if(list.contains(chatColor +args[1])) {
                                        if(Integer.valueOf(args[2])<0){
                                            p.sendMessage(ChatColor.RED+"0 이하로 설정할 수 없습니다");
                                            return false;
                                        }
                                        jobnumber.put(args[1], Integer.valueOf(args[2]));
                                        int num = 0;
                                        for(Object ob : jobnumber.keySet()){
                                            num = num + jobnumber.get(ob);
                                        }
                                        if (num>Bukkit.getOnlinePlayers().size()){
                                            p.sendMessage(ChatColor.RED+"게임 인원은 총 인원수를 초과 할 수 없습니다");
                                            return false;
                                        }
                                    }
                                }
                            }
                            config = getConfig();
                            TextComponent msg1;
                            TextComponent msg2;
                            for(Player p1 : Bukkit.getOnlinePlayers()) {
                                for(int i = 0; i < 10; i++){
                                    p1.sendMessage("");
                                }
                            }
                            for(Object ob : list) {
                                msg1 = new TextComponent(ChatColor.RED + "-");
                                msg2 = new TextComponent(ChatColor.GREEN + "+");
                                String str = ChatColor.stripColor(String.valueOf(ob));
                                if (jobnumber.get(str) == null) jobnumber.put(str, 0);
                                msg = new TextComponent(ob + "(" + jobnumber.get(str) + ")");
                                Integer jobnumbers = jobnumber.get(str);
                                msg2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/게임 설정 " + str + " " + (jobnumbers + 1)));
                                msg1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/게임 설정 " + str + " " + (jobnumbers - 1)));
                                msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(config.get(ChatColor.stripColor(String.valueOf(str))))).create()));
                                msg1.addExtra(msg);
                                msg1.addExtra(msg2);
                                p.spigot().sendMessage(msg1);
                                p.sendMessage("");
                            }
                            TextComponent option = new TextComponent(ChatColor.RED + "죽은 직업");
                            option.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.WHITE+"죽은 사람의 직업을 알 수 있다").create()));
                            TextComponent tf;
                            if(설정.get("직업")) {
                                tf = new TextComponent(ChatColor.YELLOW + " [ON]");
                            }else{
                                tf = new TextComponent(ChatColor.GRAY + " [OFF]");
                            }
                            tf.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/게임 설정 직업"));
                            option.addExtra(tf);
                            p.spigot().sendMessage(option);
                            option = new TextComponent(ChatColor.YELLOW + " [게임 시작]");
                            option.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/게임 시작"));
                            p.spigot().sendMessage(option);
                            option = new TextComponent(ChatColor.RED + "죽은 직업");
                            option.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.WHITE+"죽은 사람의 직업을 알 수 있다").create()));
                            if(설정.get("직업")) {
                                tf = new TextComponent(ChatColor.YELLOW + " [ON]");
                            }else{
                                tf = new TextComponent(ChatColor.GRAY + " [OFF]");
                            }
                            option.addExtra(tf);
                            for(Player p1 : Bukkit.getOnlinePlayers()) {
                                if (p1 != p) {
                                    for (Object ob : list) {
                                        String str = ChatColor.stripColor(String.valueOf(ob));
                                        msg = new TextComponent(ob + "(" + jobnumber.get(str) + ")");
                                        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(config.get(ChatColor.stripColor(String.valueOf(str))))).create()));
                                        p1.spigot().sendMessage(msg);
                                        p1.sendMessage("");
                                    }
                                    p1.spigot().sendMessage(option);
                                }
                            }
                            break;
                        }
                        case "설명": {
                            p.sendMessage(ChatColor.RED+"마피아\n" +
                                    "\n \n" +
                                    "일반 마피아: 밤마다 사람을 죽일 수 있다.\n" +
                                    "다른 마피아는 추후 추가\n" +
                                    "\n \n" +
                                    "마피아 승리 조건: 마피아가 시민을 전부 죽이면 승리\n" +
                                    "\n \n" +
                                    "스파이: 밤에 누군가를 지목했을때 그 사람의 직업을 알고\n마피아면 스파이에서 마피아로 직업이" +
                                    " 바뀌어진다.\n하지만 접선을 못하고 시민과 똑같은 숫자로 남았을때는 마피아 승리\n" +
                                    "\n" +
                                    "스파이가 접선을 하지 못하면 경찰조사로도 시민으로 나온다.\n" +
                                    "스파이가 접선을 하면 마피아팀으로 직업이 바뀌어져 경찰조사에 마피아로 나온다.\n" +
                                    "\n \n" +
                                    ChatColor.GREEN+"시민\n" +
                                    "\n" +
                                    "일반시민: 무직이다. 아주 선량하다\n" +
                                    "\n \n" +
                                    "경찰: 밤마다 어느 한 사람을 지목하면 그 사람이 마피아인지 시민인지 알아 낼 수 있다.\n" +
                                    "\n \n" +
                                    "의사: 밤에 어느 한 사람을 지목했을때 그 사람을 치료할 수 있다. 만약 그 사람이 마피아의 타겟이 되었다면 그 사람을 죽어도 다시 살릴 수 있다. (자힐가능)\n" +
                                    "\n \n" +
                                    "건달: 밤에 누군가를 지목하면 그 사람의 입을 막고 투표를 하지 못하게 할 수 있다.\n" +
                                    "\n \n" +
                                    "다른 시민직업도 추후에 추가\n" +
                                    "버그 및 제보는 거너아나 및 청월에게 문의해주세요.\n");
                            break;
                        }
                        case "시작": {
                            load(list);
                            start();
                            break;
                        }
                        case "중지": {
                            game.gameover();
                            직업.clear();
                            for(Player p1 : Bukkit.getOnlinePlayers()) {
                                p1.sendTitle(" ", ChatColor.RED.toString () +ChatColor.BOLD + "게임이 종료되었습니다", 1, 20, 5);
                                p1.removePotionEffect(PotionEffectType.INVISIBILITY);
                                p1.getInventory().clear();
                                p1.teleport(new Location(Bukkit.getWorld("world") ,-49 , -59, 32));
                            }
                            time = 0;
                            if(game.game != null){
                                game.game.cancel();
                            }
                            break;
                        }
                        default: {
                            p.sendMessage("등록되지 않은 명령어를 입력했습니다");
                            break;
                        }
                    }
                }
                return true;
            }else{
                return false;
            }

        }
    }
    public void start(){
        int mafia = 0;
        HashMap<Integer,Player> map = new HashMap<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            map.put(mafia,p);
            mafia = mafia + 1;
        }
        mafia = jobnumber.get("마피아") + jobnumber.get("스파이");
        Integer jobcitizen = jobnumber.get("의사");
        jobcitizen = jobcitizen + jobnumber.get("경찰");
        jobcitizen = jobcitizen + jobnumber.get("건달");
        int citizen = mafia + jobcitizen;
        jobnumber.put("시민",Bukkit.getOnlinePlayers().size() - citizen);
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);
        List<String> strings = new ArrayList<>();
        for (String str : Arrays.asList("마피아", "스파이", "의사", "경찰", "건달", "시민")) {
            if(jobnumber.get(str)>0) {
                for (int i = 0; i <= jobnumber.get(str) - 1; i++) {
                    strings.add(str);
                }
            }
        }
        int in = 0;
        for(Player p : players){
            p.getInventory().setItem(0,new ItemStack(Material.NETHER_STAR));
            p.getInventory().setItem(8,new ItemStack(Material.CLOCK));
            직업.put(p,strings.get(in));
            p.sendMessage("당신은 " + strings.get(in) + "입니다");
            in += 1;
        }
        game.night();
    }
    public void load(ArrayList list){
        list.add(ChatColor.RED+"마피아");
        list.add(ChatColor.RED+"스파이");
        list.add(ChatColor.GREEN+"의사");
        list.add(ChatColor.GREEN+"경찰");
        list.add(ChatColor.GREEN+"건달");
        for(Object ob : list){
            String str = ChatColor.stripColor(String.valueOf(ob));
            if(jobnumber.get(str)==null) jobnumber.put(str, 0);
        }
    }
    @EventHandler
    public void click(PlayerInteractEvent a){
        Player p = a.getPlayer();
        if(직업.containsKey(p)){
            if(처형자!=null) {
                if (!처형중.containsKey(p)) {
                    if (a.getAction() == Action.RIGHT_CLICK_AIR || a.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        처형확인 -= 1;
                        처형중.put(p, true);
                        p.sendMessage("반대에 투표하였습니다");
                    }
                    if (a.getAction() == Action.LEFT_CLICK_AIR || a.getAction() == Action.LEFT_CLICK_BLOCK) {
                        처형확인 += 1;
                        처형중.put(p, false);
                        p.sendMessage("찬성에 투표하였습니다");
                    }
                }
            }
            if(a.getAction()== Action.RIGHT_CLICK_BLOCK||a.getAction()== Action.RIGHT_CLICK_AIR){
                if(p.getInventory().getItemInHand().getType()==Material.NETHER_STAR){
                    if(p.getWorld().getTime()==6000){
                        p.openInventory(낮);
                    }
                    if(p.getWorld().getTime()==7000){
                        if (!투표.containsKey(p)){
                            p.openInventory(투표창 );
                        }else{
                            p.sendMessage(ChatColor.RED+"이미 투표하셨습니다");
                        }
                    }
                    if(p.getWorld().getTime()==18000){
                        if(invre(직업.get(p))!=null){
                            p.openInventory(invre(직업.get(p)));
                        }
                    }
                }
            }
        }
    }
    public Inventory invre(String s){
        Inventory inv2 = null;
        switch (s) {
            case "마피아":
                inv2 = Minv;
                break;
            case "스파이":
                inv2 = Sinv;
                break;
            case "의사":
                inv2 = Dinv;
                break;
            case "경찰":
                inv2 = Pinv;
                break;
            case "건달":
                inv2 = Winv;
                break;
        }
        return inv2;
    }
    @EventHandler
    public void invclick(InventoryClickEvent a){
        Player p = (Player) a.getWhoClicked();
        if (a.getRawSlot() >= a.getInventory().getSize()) {
            return;
        }
        a.setCancelled(true);
        if(직업.containsKey(p)){
            if(a.getCurrentItem()!=null&&a.getCurrentItem()!= new ItemStack(Material.AIR)) {
                if(p.getOpenInventory().getTitle().equals("능력 실행창")){
                    p.openInventory(invre(직업.get(p)));
                    ItemStack item = a.getCurrentItem();
                    ItemMeta meta = item.getItemMeta();
                    SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                    ArrayList<String> lore = new ArrayList<>();
                    lore.add(ChatColor.WHITE+"선택됨");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    직업선택.put(직업.get(p),(Player) skullMeta.getOwningPlayer());
                }
                if(p.getOpenInventory().getTitle().equals("시간 선택")) {
                    if(!시간선택.containsKey(p)){
                        시간선택.put(p,true);
                        if(a.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.YELLOW+"시간 연장")){
                            시간 += 30;
                            time += 30;
                            for(Player p1 : Bukkit.getOnlinePlayers()){
                                p1.sendMessage(p.getName()+"가 시간을 늘렸습니다");
                            }
                        }else{
                            시간 -= 30;
                            time -= 30;
                            for(Player p1 : Bukkit.getOnlinePlayers()){
                                p1.sendMessage(p.getName()+"가 시간을 줄였습니다");
                            }
                        }
                    }
                }
                if(p.getOpenInventory().getTitle().equals("투표창")) {
                    if(직업선택.get("건달")!=p){
                        ItemStack item = a.getCurrentItem();
                        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                        투표.put(p,(Player) skullMeta.getOwningPlayer());
                        p.closeInventory();
                        p.sendMessage(skullMeta.getOwningPlayer().getName()+"님을 투표하셨습니다");
                    }else{
                        p.sendMessage("건달에게 당해 투표할 수 없습니다");
                    }
                }
            p.closeInventory();
            }
        }
    }
    @EventHandler
    public void chat(AsyncPlayerChatEvent a){
        Player p = a.getPlayer();
        if(처형자==p){
            a.setCancelled(true);
            for(Player p1 : Bukkit.getOnlinePlayers()){
                p1.sendMessage("");
                p1.sendMessage("");
                p1.sendMessage("");
                p1.sendMessage(p.getName() + " : " + a.getMessage());
                p1.sendMessage("");
                p1.sendMessage("");
                p1.sendMessage("");
            }
            return;
        }
        if(p.getWorld().getTime()==18000){
            a.setCancelled(true);
            if(직업.containsKey(p)) {
                if(직업.get(p).equals("마피아")) {
                    if(!p.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                        for (Player p1 : Bukkit.getOnlinePlayers()) {
                            if (직업.get(p1).equals("마피아")) {
                                p1.sendMessage(ChatColor.RED + "<" + p.getName() + "> " + a.getMessage());
                            }
                        }
                    }
                }
            }else{
                p.sendMessage("방송 채팅으로 얘기해주세요");
                return;
            }
        }
        if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)){
            a.setCancelled(true);
            for(Player p1 : Bukkit.getOnlinePlayers()){
                if(p1.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                    p1.sendMessage(ChatColor.GRAY+"<관전자 "+p.getName()+"> " + a.getMessage());
                }
            }
        }else{
            a.setCancelled(true);
            for(Player p1 : Bukkit.getOnlinePlayers()){
                if(p1.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                    if(직업.containsKey(p)) {
                        p1.sendMessage(ChatColor.GRAY+"< " + 직업.get(p) + " " +p.getName()+"> " + a.getMessage());
                    }else{
                        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 1, false,false,false));
                    }
                }else{
                    p1.sendMessage("<"+p.getName()+"> " + a.getMessage());
                }
            }
        }
    }
    @EventHandler
    public void death(EntityDamageEvent a){
        a.setCancelled(true);
        if(a.getEntityType()== EntityType.PLAYER){
            Player p = (Player) a.getEntity();
            if(a.getDamage()==100){
                if(설정.get("직업")){
                    for(Player p1 : Bukkit.getOnlinePlayers()){
                        p1.sendMessage("");
                        p1.sendMessage("");
                        p1.sendMessage(p.getName()+"은 "+직업.get(p)+"였습니다");
                        p1.sendMessage("");
                        p1.sendMessage("");
                    }
                }
                p.getInventory().clear();
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 1, false,false,false));
            }
        }
    }
}

